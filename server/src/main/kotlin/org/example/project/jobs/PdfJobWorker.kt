package org.example.project.jobs

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import org.example.project.pdf.PromptLlmClient
import org.example.project.pdfChunker.PdfChunker
import org.example.project.repo.PdfJob
import org.example.project.repo.PdfJobRepository
import org.example.project.tutor.networkModel.uploadPdf.LearningUnitDto
import org.example.project.tutor.networkModel.uploadPdf.LearningUnitsPrototypeDto
import java.io.File
import java.util.UUID

class PdfJobWorker(
    private val repo: PdfJobRepository,
    private val llm: PromptLlmClient
) {
    private val workerId = "worker_" + UUID.randomUUID().toString().take(8)

    fun start(scope: CoroutineScope) {
        scope.launch(Dispatchers.IO) {
            while (isActive) {
                try {
                    val job = repo.claimNextJob(workerId)
                    if (job == null) {
                        delay(800)
                        continue
                    }
                    process(job)
                } catch (t: Throwable) {
                    println("Worker loop error: ${t.message}")
                    delay(1000)
                }
            }
        }
    }

    private suspend fun process(job: PdfJob) {
        try {
            repo.setProgress(job.documentId, 1)
            repo.heartbeat(job.documentId, workerId)

            val pdf = File(job.storagePath)
            repo.setProgress(job.documentId, 3)
            val chunksDir = File("uploads/${job.documentId}/chunks")
            val chunks = PdfChunker.splitPdfIntoChunks(
                input = pdf,
                outDir = chunksDir,
                pagesPerChunk = 12,
                overlapPages = 1
            )
            if (chunks.isEmpty()) {
                repo.completeError(job.documentId, "PDF split produced 0 chunks")
                return
            }

            var carry = ""
            val units = mutableListOf<LearningUnitDto>()
            var unitNumber = 1

            for ((i, chunk) in chunks.withIndex()) {
                repo.heartbeat(job.documentId, workerId)

                // progress mapping: 5..70 for preview loop
                val p = 5 + ((i + 1).toDouble() / chunks.size.toDouble() * 65.0).toInt()
                repo.setProgress(job.documentId, p)

                // 1) preview from chunk + carry
                val preview = llm.generatePreviewUnitFromPdfChunkWithCarry(
                    pdfChunkFile = chunk,
                    language = job.language,
                    fileName = job.fileName,
                    unitNumber = unitNumber,
                    carry = carry
                )

                carry = preview.carry

                val unitOnly = preview.unit
                if (unitOnly == null) {
                    // Not enough to form a good unit yet; continue to next chunk with carry
                    continue
                }

                // 2) immediately append unit (without exercises yet) and save partial
                units.add(unitOnly)

                repo.savePartialResult(
                    documentId = job.documentId,
                    partial = LearningUnitsPrototypeDto(
                        prototypeTitle = job.fileName.removeSuffix(".pdf"),
                        language = job.language,
                        totalUnits = units.size,
                        learningUnits = units.toList()
                    ),
                    progress = (p.coerceAtMost(85))
                )

                // 3) exercises for this unit
                repo.setProgress(job.documentId, (p + 2).coerceAtMost(90))
                val exercises = llm.generateExercisesForUnit(
                    unit = unitOnly,
                    language = job.language,
                    fileName = job.fileName
                )

                var unitFull = unitOnly.copy(exercises = exercises)

                // 4) optional image generation
                val img0 = unitFull.images.firstOrNull()
                val prompt = img0?.prompt?.trim().orEmpty()
                if (prompt.isNotBlank()) {
                    val rel = llm.generateAndSaveImage(
                        documentId = job.documentId,
                        unitNumber = unitNumber,
                        imageIndex = 1,
                        prompt = prompt
                    )
                    unitFull = unitFull.copy(
                        images = unitFull.images.mapIndexed { idx, it ->
                            if (idx == 0) it.copy(relativePath = rel) else it
                        }
                    )
                }

                // 5) replace last unit with full version and save partial again
                units[units.lastIndex] = unitFull
                repo.savePartialResult(
                    documentId = job.documentId,
                    partial = LearningUnitsPrototypeDto(
                        prototypeTitle = job.fileName.removeSuffix(".pdf"),
                        language = job.language,
                        totalUnits = units.size,
                        learningUnits = units.toList()
                    ),
                    progress = (p + 4).coerceAtMost(92)
                )

                unitNumber++
            }

// 6) If still have carry at end, try to finalize one last unit (optional but recommended)
            if (carry.isNotBlank()) {
                // Reuse the last chunk to help model anchor, but mainly consume carry
                val lastChunk = chunks.last()

                val preview = llm.generatePreviewUnitFromPdfChunkWithCarry(
                    pdfChunkFile = lastChunk,
                    language = job.language,
                    fileName = job.fileName,
                    unitNumber = unitNumber,
                    carry = carry
                )

                val unitOnly = preview.unit
                if (unitOnly != null) {
                    units.add(unitOnly)

                    repo.savePartialResult(
                        documentId = job.documentId,
                        partial = LearningUnitsPrototypeDto(
                            prototypeTitle = job.fileName.removeSuffix(".pdf"),
                            language = job.language,
                            totalUnits = units.size,
                            learningUnits = units.toList()
                        ),
                        progress = 94
                    )

                    val exercises = llm.generateExercisesForUnit(
                        unit = unitOnly,
                        language = job.language,
                        fileName = job.fileName
                    )
                    var unitFull = unitOnly.copy(exercises = exercises)

                    val img0 = unitFull.images.firstOrNull()
                    val prompt = img0?.prompt?.trim().orEmpty()
                    if (prompt.isNotBlank()) {
                        val rel = llm.generateAndSaveImage(job.documentId, unitNumber, 1, prompt)
                        unitFull = unitFull.copy(
                            images = unitFull.images.mapIndexed { idx, it ->
                                if (idx == 0) it.copy(relativePath = rel) else it
                            }
                        )
                    }

                    units[units.lastIndex] = unitFull
                    repo.savePartialResult(
                        documentId = job.documentId,
                        partial = LearningUnitsPrototypeDto(
                            prototypeTitle = job.fileName.removeSuffix(".pdf"),
                            language = job.language,
                            totalUnits = units.size,
                            learningUnits = units.toList()
                        ),
                        progress = 97
                    )
                }
            }

// 7) finalize success
            val result = LearningUnitsPrototypeDto(
                prototypeTitle = job.fileName.removeSuffix(".pdf"),
                language = job.language,
                totalUnits = units.size,
                learningUnits = units.toList()
            )

            repo.setProgress(job.documentId, 100)
            repo.completeSuccess(job.documentId, result)

        } catch (t: Throwable) {
            t.printStackTrace()
            repo.completeError(job.documentId, t.message ?: "Unknown error")
        }
    }
}
