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
import org.example.project.tutor.networkModel.uploadPdf.ExerciseDto
import org.example.project.tutor.networkModel.uploadPdf.LearningUnitDto
import org.example.project.tutor.networkModel.uploadPdf.LearningUnitsPrototypeDto
import java.io.File
import java.util.UUID
import kotlin.random.Random

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
                    t.printStackTrace()
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
            val chunks = PdfChunker.splitPdfByTocChapters(
                input = pdf,
                outDir = chunksDir,
                tocStartPage = 1,
                tocEndPageInclusive = 6,
                pageNumberOffset = 0,
                overlapPages = 1,
                fallbackPagesPerChunk = 12
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

                val p = 5 + ((i + 1).toDouble() / chunks.size.toDouble() * 65.0).toInt()
                repo.setProgress(job.documentId, p)

                val preview = try {
                    llm.generatePreviewUnitFromPdfChunkWithCarry(
                        pdfChunkFile = chunk,
                        language = job.language,
                        fileName = job.fileName,
                        unitNumber = unitNumber,
                        carry = carry
                    )
                } catch (t: Throwable) {
                    println("Chunk preview failed (chunk=${chunk.name}): ${t.message}")
                    t.printStackTrace()
                    continue
                }

                carry = preview.carry

                val unitOnly = preview.unit ?: continue

                units.add(unitOnly)
                repo.savePartialResult(
                    documentId = job.documentId,
                    partial = LearningUnitsPrototypeDto(
                        prototypeTitle = job.fileName.removeSuffix(".pdf"),
                        language = job.language,
                        totalUnits = units.size,
                        learningUnits = units.toList()
                    ),
                    progress = p.coerceAtMost(85)
                )

                repo.setProgress(job.documentId, (p + 2).coerceAtMost(90))

                val exercises: List<ExerciseDto> = run {
                    var lastErr: Throwable? = null
                    repeat(3) { attempt ->
                        try {
                            return@run llm.generateExercisesForUnit(
                                unit = unitOnly,
                                language = job.language,
                                fileName = job.fileName,
                                targetMinutes = 55
                            )
                        } catch (t: Throwable) {
                            lastErr = t
                            println("Exercises failed (unit=$unitNumber attempt=${attempt + 1}): ${t.message}")
                            t.printStackTrace()
                            delay(1500L + Random.nextLong(0, 900))
                        }
                    }
                    error("Exercises failed permanently (unit=$unitNumber): ${lastErr?.message}")
                }

                var unitFull = unitOnly.copy(exercises = exercises)

                run {
                    val maxImgs = 2
                    val imgs = unitFull.images.take(maxImgs)
                    if (imgs.isNotEmpty()) {
                        val newImgs = unitFull.images.toMutableList()
                        for ((idx, img) in imgs.withIndex()) {
                            val prompt = img.prompt?.trim().orEmpty()
                            if (prompt.isBlank()) continue

                            val rel: String? = try {
                                llm.generateAndSaveImage(
                                    documentId = job.documentId,
                                    unitNumber = unitNumber,
                                    imageIndex = idx + 1,
                                    prompt = prompt
                                )
                            } catch (t: Throwable) {
                                println("Image gen failed (unit=$unitNumber idx=${idx + 1}): ${t.message}")
                                t.printStackTrace()
                                null
                            }

                            if (!rel.isNullOrBlank()) {
                                newImgs[idx] = newImgs[idx].copy(relativePath = rel)
                            }
                        }
                        unitFull = unitFull.copy(images = newImgs)
                    }
                }

                unitFull = unitFull.copy(
                    images = unitFull.images.filter { !it.relativePath.isNullOrBlank() }
                )

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

            if (carry.isNotBlank()) {
                val lastChunk = chunks.last()

                val preview = try {
                    llm.generatePreviewUnitFromPdfChunkWithCarry(
                        pdfChunkFile = lastChunk,
                        language = job.language,
                        fileName = job.fileName,
                        unitNumber = unitNumber,
                        carry = carry
                    )
                } catch (t: Throwable) {
                    println("Final carry preview failed: ${t.message}")
                    t.printStackTrace()
                    null
                }

                val unitOnly = preview?.unit
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

                    val exercises: List<ExerciseDto> = run {
                        var lastErr: Throwable? = null
                        repeat(3) { attempt ->
                            try {
                                return@run llm.generateExercisesForUnit(
                                    unit = unitOnly,
                                    language = job.language,
                                    fileName = job.fileName,
                                    targetMinutes = 55
                                )
                            } catch (t: Throwable) {
                                lastErr = t
                                println("Final exercises failed (unit=$unitNumber attempt=${attempt + 1}): ${t.message}")
                                t.printStackTrace()
                                delay(1500L + Random.nextLong(0, 900))
                            }
                        }
                        error("Final exercises failed permanently (unit=$unitNumber): ${lastErr?.message}")
                    }

                    var unitFull = unitOnly.copy(exercises = exercises)

                    run {
                        val maxImgs = 2
                        val imgs = unitFull.images.take(maxImgs)
                        if (imgs.isNotEmpty()) {
                            val newImgs = unitFull.images.toMutableList()
                            for ((idx, img) in imgs.withIndex()) {
                                val prompt = img.prompt?.trim().orEmpty()
                                if (prompt.isBlank()) continue

                                val rel: String? = try {
                                    llm.generateAndSaveImage(
                                        documentId = job.documentId,
                                        unitNumber = unitNumber,
                                        imageIndex = idx + 1,
                                        prompt = prompt
                                    )
                                } catch (t: Throwable) {
                                    println("Final image gen failed (unit=$unitNumber idx=${idx + 1}): ${t.message}")
                                    t.printStackTrace()
                                    null
                                }

                                if (!rel.isNullOrBlank()) {
                                    newImgs[idx] = newImgs[idx].copy(relativePath = rel)
                                }
                            }
                            unitFull = unitFull.copy(images = newImgs)
                        }
                    }

                    unitFull = unitFull.copy(
                        images = unitFull.images.filter { !it.relativePath.isNullOrBlank() }
                    )

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
