package org.example.project.routes

import io.ktor.http.*
import io.ktor.http.content.PartData
import io.ktor.http.content.forEachPart
import io.ktor.http.content.streamProvider
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.request.receiveMultipart
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import org.example.project.pdf.PromptLlmClient
import org.example.project.repo.PdfJobRepository
import org.example.project.storage.PdfStorage
import org.example.project.tutor.networkModel.uploadPdf.JobStatus
import org.example.project.tutor.networkModel.uploadPdf.UnitExercisesOnlyDto
import org.example.project.tutor.networkModel.uploadPdf.UploadPdfResponse
import java.io.File
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap
import org.example.project.storage.ExperimentContentStorage
private const val MAX_PDF_SIZE_BYTES = 100L * 1024L * 1024L
fun Route.pdfRoutes(
    repo: PdfJobRepository,
    llm: PromptLlmClient,
    pdfStorage: PdfStorage
) {
    val exerciseLocks = ConcurrentHashMap<String, Mutex>()

    route("/v1/pdfs") {

        post("/upload") {
            val multipart = call.receiveMultipart()

            var fileName = "upload.pdf"
            var savedFile: File? = null
            var badRequest: String? = null
            var responseLanguage = "en"
            val documentId = "doc_" + UUID.randomUUID().toString().replace("-", "")

            multipart.forEachPart { part ->
                try {
                    when (part) {
                        is PartData.FormItem -> {
                            if (part.name == "responseLanguage") {
                                responseLanguage = part.value
                            }
                        }

                        is PartData.FileItem -> {
                            if (part.name == "file") {

                                val originalName = part.originalFileName?.substringAfterLast('/')?.substringAfterLast('\\')?.trim()?.takeIf { it.isNotBlank() } ?: "upload.pdf"

                                fileName = originalName

                                val dir = File("uploads", documentId).apply {
                                    require(mkdirs() || exists()) {
                                        "Could not create upload directory"
                                    }
                                }

                                val file = File(dir, "source.pdf")

                                part.streamProvider().use { input ->
                                    file.outputStream().use { out ->
                                        val buffer = ByteArray(DEFAULT_BUFFER_SIZE)
                                        var totalBytes = 0L

                                        while (true) {
                                            val read = input.read(buffer)
                                            if (read <= 0) break

                                            totalBytes += read

                                            if (totalBytes > MAX_PDF_SIZE_BYTES) {
                                                badRequest = "PDF exceeds the maximum allowed size of 100 MB"
                                                break
                                            }

                                            out.write(buffer, 0, read)
                                        }
                                    }
                                }

                                if (badRequest != null) {
                                    file.delete()
                                    return@forEachPart
                                }

                                if (!file.exists() || file.length() == 0L) {
                                    badRequest = "Uploaded PDF could not be saved"
                                    file.delete()
                                    return@forEachPart
                                }

                                savedFile = file
                            }
                        }

                        else -> {}
                    }
                } finally {
                    part.dispose()
                }
            }

            if (badRequest != null) {
                call.respond(HttpStatusCode.BadRequest, mapOf("error" to badRequest))
                return@post
            }

            val pdf = savedFile ?: run {
                call.respond(
                    HttpStatusCode.BadRequest,
                    mapOf("error" to "Missing multipart field 'file'")
                )
                return@post
            }

            val cloudStoragePath = try {
                pdfStorage.upload(documentId = documentId, localFile = pdf)
            } catch (t: Throwable) {
                t.printStackTrace()

                println("PDF STORAGE UPLOAD FAILED")
                println("Bucket: ${System.getenv("PDF_BUCKET")}")
                println("Error: ${t.message}")

                pdf.parentFile?.deleteRecursively()

                call.respond(
                    HttpStatusCode.InternalServerError,
                    mapOf(
                        "error" to (t.message ?: "Could not store uploaded PDF"),
                        "type" to t.javaClass.name
                    )
                )
                return@post
            }
            pdf.parentFile?.deleteRecursively()

            repo.createAndEnqueue(
                documentId = documentId,
                fileName = fileName,
                storagePath = cloudStoragePath,
                responseLanguage = responseLanguage
            )

            call.respond(
                UploadPdfResponse(
                    documentId = documentId,
                    status = JobStatus.Queued.value,
                    storagePath = cloudStoragePath
                )
            )
        }

        get("/{documentId}") {

            val documentId = call.parameters["documentId"]
                ?: return@get call.respond(
                    HttpStatusCode.BadRequest,
                    mapOf("error" to "Missing documentId")
                )
            val status = repo.getStatus(documentId)
                ?: return@get call.respond(HttpStatusCode.NotFound, mapOf("error" to "Not found"))

            call.respond(status)
        }
        post("/{documentId}/units/{unitNumber}/exercises") {

            val documentId = call.parameters["documentId"]
                ?: return@post call.respond(
                    HttpStatusCode.BadRequest,
                    mapOf("error" to "Missing documentId")
                )

            val unitNumber = call.parameters["unitNumber"]
                ?.toIntOrNull()
                ?: return@post call.respond(
                    HttpStatusCode.BadRequest,
                    mapOf("error" to "Invalid unitNumber")
                )

            if (unitNumber <= 0) {
                call.respond(
                    HttpStatusCode.BadRequest,
                    mapOf("error" to "unitNumber must be greater than zero")
                )
                return@post
            }

            val initialResult = repo.getCompletedResult(documentId)
                ?: return@post call.respond(
                    HttpStatusCode.NotFound,
                    mapOf("error" to "PDF result not found")
                )

            val initialUnit = initialResult.learningUnits.firstOrNull {
                it.numbering == unitNumber
            } ?: return@post call.respond(
                HttpStatusCode.NotFound,
                mapOf("error" to "Learning unit not found")
            )

            /*
             * Fast path:
             * exercises were already generated earlier.
             */
            if (initialUnit.exercises.isNotEmpty()) {
                call.respond(
                    UnitExercisesOnlyDto(
                        exercises = initialUnit.exercises
                    )
                )
                return@post
            }

            if (
                documentId ==
                ExperimentContentStorage.EXPERIMENT_DOCUMENT_ID
            ) {
                call.respond(
                    UnitExercisesOnlyDto(
                        exercises = initialUnit.exercises
                    )
                )
                return@post
            }

            /*
             * Prevent two simultaneous requests from generating
             * the same 20 exercises twice.
             */
            val lockKey = "$documentId:$unitNumber"

            val lock = exerciseLocks.computeIfAbsent(lockKey) {
                Mutex()
            }

            val exercises = try {
                lock.withLock {
                    val currentResult = repo.getCompletedResult(documentId)
                        ?: error("PDF result disappeared")

                    val currentUnit = currentResult.learningUnits.firstOrNull {
                        it.numbering == unitNumber
                    } ?: error("Learning unit disappeared")
                    val responseLanguage = resolveTutorLanguage(
                        call.request.queryParameters["responseLanguage"]
                    )
                    currentUnit.exercises.ifEmpty {
                        val generated = llm.generateExercisesForUnit(
                            unit = currentUnit,
                            language = responseLanguage,
                            fileName = currentResult.prototypeTitle
                                .orEmpty()
                                .ifBlank { documentId },
                            targetMinutes = 55
                        )
                        if (generated.isEmpty()) {
                            error(
                                "Exercise generation returned no exercises for unit $unitNumber"
                            )
                        }
                        val savedUnit = repo.replaceUnitExercises(
                            documentId = documentId,
                            unitNumber = unitNumber,
                            exercises = generated
                        ) ?: error(
                            "Could not save exercises for unit $unitNumber"
                        )

                        savedUnit.exercises
                    }
                }
            } catch (t: Throwable) {
                t.printStackTrace()

                call.respond(
                    HttpStatusCode.InternalServerError,
                    mapOf("error" to (t.message ?: "Exercise generation failed"))
                )
                return@post
            } finally {
                exerciseLocks.remove(lockKey, lock)
            }

            call.respond(
                UnitExercisesOnlyDto(
                    exercises = exercises
                )
            )
        }
        get("/{documentId}/debug/last") {
            val documentId = call.parameters["documentId"]
                ?: return@get call.respond(
                    HttpStatusCode.BadRequest,
                    mapOf("error" to "Missing documentId")
                )

            val raw = repo.getLastLlmRaw(documentId) ?: ""
            call.respond(mapOf("documentId" to documentId, "lastLlmRaw" to raw))
        }
    }
}
