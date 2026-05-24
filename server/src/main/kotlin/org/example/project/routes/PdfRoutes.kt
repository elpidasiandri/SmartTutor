package org.example.project.routes

import io.ktor.http.*
import io.ktor.http.content.PartData
import io.ktor.http.content.forEachPart
import io.ktor.http.content.streamProvider
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.request.receiveMultipart
import org.example.project.repo.PdfJobRepository
import org.example.project.tutor.networkModel.uploadPdf.JobStatus
import org.example.project.tutor.networkModel.uploadPdf.UploadPdfResponse
import java.io.File
import java.util.UUID

fun Route.pdfRoutes(repo: PdfJobRepository) {
    route("/v1/pdfs") {

        post("/upload") {
            val multipart = call.receiveMultipart()

            var language = "el"
            var fileName = "upload.pdf"
            var savedFile: File? = null
            var badRequest: String? = null

            val documentId = "doc_" + UUID.randomUUID().toString().replace("-", "")

            multipart.forEachPart { part ->
                try {
                    when (part) {
                        is PartData.FormItem -> {
                            if (part.name == "language") {
                                language = part.value.trim().ifBlank { "el" }
                            }
                        }

                        is PartData.FileItem -> {
                            if (part.name == "file") {
                                fileName = part.originalFileName ?: "upload.pdf"
                                if (!fileName.lowercase().endsWith(".pdf")) {
                                    badRequest = "Only .pdf allowed"
                                    return@forEachPart
                                }

                                val dir = File("uploads/$documentId").apply { mkdirs() }
                                val file = File(dir, "source.pdf")

                                part.streamProvider().use { input ->
                                    file.outputStream().use { out -> input.copyTo(out) }
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

            repo.createAndEnqueue(
                documentId = documentId,
                fileName = fileName,
                language = language,
                storagePath = pdf.absolutePath
            )

            call.respond(
                UploadPdfResponse(
                    documentId = documentId,
                    status = JobStatus.Queued.value,
                    storagePath = pdf.absolutePath
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
