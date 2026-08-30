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
import org.apache.pdfbox.pdmodel.PDDocument
import org.apache.pdfbox.text.PDFTextStripper
import org.example.project.pdf.LanguageDetector

class PdfJobWorker(
    private val repo: PdfJobRepository,
    private val llm: PromptLlmClient,
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
            val documentText = PDDocument.load(pdf).use { document ->
                PDFTextStripper().getText(document)
            }

            if (documentText.isBlank()) {
                repo.completeError(
                    job.documentId,
                    "Could not extract text from PDF. Scanned PDFs are not supported yet."
                )
                return
            }

            val detectedLanguage = LanguageDetector.detect(documentText)

            repo.setProgress(job.documentId, 3)

            val chunksDir = File("uploads/${job.documentId}/chunks")
            val chunks = PdfChunker.splitPdfIntoChunks(
                input = pdf,
                outDir = chunksDir,
                pagesPerChunk = 6,
                overlapPages = 0
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

                val p = 5 + (
                        (i + 1).toDouble() /
                                chunks.size.toDouble() * 65.0
                        ).toInt()

                repo.setProgress(
                    job.documentId,
                    p
                )
                val isLastChunk = i == chunks.lastIndex
                val preview = try {
                    llm.generatePreviewUnitsFromPdfChunkWithCarry(
                        pdfChunkFile = chunk,
                        language = detectedLanguage,
                        fileName = job.fileName,
                        unitNumber = unitNumber,
                        carry = carry,
                        isLastChunk = isLastChunk
                    )
                } catch (t: Throwable) {
                    throw IllegalStateException(
                        "Failed to process PDF chunk ${i + 1}/${chunks.size}",
                        t
                    )
                }

                carry = preview.carry

                val generatedUnits = preview.units
                    .orEmpty()
                    .map { unit ->
                        unit.copy(
                            isRead = false,
                            exercises = emptyList()
                        )
                    }

                if (generatedUnits.isEmpty()) {
                    continue
                }

                generatedUnits.forEach { generatedUnit ->
                    val fixedUnit = generatedUnit.copy(
                        numbering = unitNumber
                    )

                    units.add(fixedUnit)
                    unitNumber++
                }

                repo.savePartialResult(
                    documentId = job.documentId,
                    partial = LearningUnitsPrototypeDto(
                        prototypeTitle = job.fileName.removeSuffix(".pdf"),
                        language = detectedLanguage,
                        totalUnits = units.size,
                        learningUnits = units.toList()
                    ),
                    progress = p.coerceAtMost(85)
                )
            }
            if (units.isEmpty()) {
                repo.completeError(
                    job.documentId,
                    "No learning units could be generated from the PDF"
                )
                return
            }

            val result = LearningUnitsPrototypeDto(
                prototypeTitle = job.fileName.removeSuffix(".pdf"),
                language = detectedLanguage,
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
