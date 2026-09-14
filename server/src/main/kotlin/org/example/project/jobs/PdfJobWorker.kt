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
import org.example.project.storage.PdfStorage

class PdfJobWorker(
    private val repo: PdfJobRepository,
    private val llm: PromptLlmClient,
    private val pdfStorage: PdfStorage,
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

            val pdf = pdfStorage.downloadToTemp(
                documentId = job.documentId,
                objectPath = job.storagePath,
            )

            val documentText = PDDocument.load(pdf).use { document ->
                PDFTextStripper().getText(document)
            }

            val detectedLanguage =
                if (documentText.isBlank()) {
                    "en"
                } else {
                    LanguageDetector.detect(documentText)
                }

            repo.setProgress(job.documentId, 3)
            repo.heartbeat(job.documentId, workerId)

            val chunksDir = File(
                pdf.parentFile,
                "chunks"
            ).apply {
                mkdirs()
            }

            val chunks = PdfChunker.splitPdfIntoChunks(
                input = pdf,
                outDir = chunksDir,
                pagesPerChunk = 6,
                overlapPages = 0
            )

            if (chunks.isEmpty()) {
                repo.completeError(
                    job.documentId,
                    "No PDF chunks could be created"
                )
                return
            }

            var carry = ""
            var prototypeTitle: String? = null
            val units = mutableListOf<LearningUnitDto>()
            var unitNumber = 1


            for ((i, chunk) in chunks.withIndex()) {

                repo.heartbeat(
                    job.documentId,
                    workerId
                )

                val progress =
                    5 + (
                            ((i + 1).toDouble() / chunks.size) * 65.0
                            ).toInt()

                repo.setProgress(
                    job.documentId,
                    progress
                )

                val isLastChunk =
                    i == chunks.lastIndex

                /*
                 * A chunk may occasionally fail because the LLM returns
                 * incomplete output, invalid JSON, a temporary API error,
                 * or another transient response.
                 *
                 * Do NOT immediately fail the whole PDF.
                 *
                 * Retry this SAME chunk up to 3 times.
                 */
                val resolvedPreview = run {

                    var lastError: Throwable? = null

                    for (attempt in 1..3) {

                        try {
                            val result = llm.generatePreviewUnitsFromPdfChunkWithCarry(
                                pdfChunkFile = chunk,
                                language = job.responseLanguage,
                                fileName = job.fileName,
                                unitNumber = unitNumber,
                                carry = carry,
                                isLastChunk = isLastChunk
                            )

                            /*
                             * Success.
                             * Exit the retry block immediately.
                             */
                            return@run result

                        } catch (t: Throwable) {

                            if (t is kotlinx.coroutines.CancellationException) {
                                throw t
                            }

                            lastError = t

                            println(
                                "PDF chunk ${i + 1}/${chunks.size} " +
                                        "failed on attempt $attempt/3: " +
                                        (t.message ?: t::class.simpleName)
                            )

                            t.printStackTrace()

                            /*
                             * Retry only when another attempt remains.
                             */
                            if (attempt < 3) {
                                kotlinx.coroutines.delay(
                                    1500L * attempt
                                )

                                repo.heartbeat(
                                    job.documentId,
                                    workerId
                                )
                            }
                        }
                    }

                    /*
                     * IMPORTANT:
                     *
                     * We do NOT skip this chunk.
                     *
                     * If all 3 complete generation attempts fail,
                     * we fail the job because otherwise part of the PDF
                     * would silently disappear from the learning material.
                     */
                    val causeMessage =
                        lastError?.message
                            ?.takeIf { it.isNotBlank() }
                            ?: lastError
                                ?.javaClass
                                ?.simpleName
                            ?: "Unknown error"

                    throw IllegalStateException(
                        "Failed to process PDF chunk " +
                                "${i + 1}/${chunks.size} " +
                                "after 3 attempts: $causeMessage",
                        lastError
                    )
                }

                carry = resolvedPreview.carry

                val candidateTitle =
                    resolvedPreview.prototypeTitle
                        ?.trim()
                        ?.takeIf { it.isNotBlank() }

                if (
                    prototypeTitle.isNullOrBlank() &&
                    candidateTitle != null
                ) {
                    prototypeTitle = candidateTitle
                }

                val generatedUnits = resolvedPreview.units.orEmpty().map { generatedUnit ->
                    generatedUnit.copy(
                        isRead = false,
                        exercises = emptyList()
                    )
                }

                /*
                 * A chunk is allowed to produce zero units.
                 *
                 * Example:
                 * - bibliography
                 * - contents
                 * - administrative pages
                 * - continuation of an incomplete concept stored in carry
                 *
                 * Therefore zero units from ONE chunk is not an error.
                 */
                if (generatedUnits.isNotEmpty()) {

                    generatedUnits.forEach { generatedUnit ->

                        units.add(
                            generatedUnit.copy(
                                numbering = unitNumber
                            )
                        )

                        unitNumber++
                    }

                    val partialResult =
                        LearningUnitsPrototypeDto(
                            prototypeTitle = prototypeTitle?.takeIf { it.isNotBlank() }
                                ?: job.fileName.substringBeforeLast("."),
                            language = detectedLanguage,
                            totalUnits = units.size,
                            learningUnits = units.toList()
                        )

                    repo.savePartialResult(
                        documentId = job.documentId,
                        partial = partialResult,
                        progress = progress
                    )
                }
            }

            /*
             * If the whole PDF finished but not even one educational
             * learning unit was produced, something went wrong.
             */
            if (units.isEmpty()) {
                repo.completeError(
                    job.documentId,
                    "No learning units could be generated from the PDF"
                )
                return
            }

            val result =
                LearningUnitsPrototypeDto(
                    prototypeTitle = prototypeTitle?.takeIf { it.isNotBlank() }
                        ?: job.fileName.substringBeforeLast("."),
                    language = detectedLanguage,
                    totalUnits = units.size,
                    learningUnits = units.toList()
                )

            repo.setProgress(
                job.documentId,
                100
            )

            repo.completeSuccess(
                job.documentId,
                result
            )

        } catch (t: Throwable) {

            if (t is kotlinx.coroutines.CancellationException) {
                println(
                    "Worker cancelled while processing ${job.documentId}. " +
                            "Leaving job recoverable."
                )
                throw t
            }

            t.printStackTrace()

            val message =
                t.message
                    ?.takeIf { it.isNotBlank() }
                    ?: t.javaClass.simpleName

            repo.completeError(
                job.documentId,
                message
            )
        }
    }
}
