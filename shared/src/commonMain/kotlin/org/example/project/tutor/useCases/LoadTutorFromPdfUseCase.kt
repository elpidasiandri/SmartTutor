package org.example.project.tutor.useCases

import kotlinx.coroutines.delay
import org.example.project.tutor.models.domainModels.file.PickedFile
import org.example.project.tutor.models.domainModels.learningUnits.LearningUnitsPrototypeDomain
import org.example.project.tutor.mappers.learningUnits.toDomain
import org.example.project.tutor.models.domainModels.file.UploadFile
import org.example.project.tutor.networkModel.chatHistory.CreateChatFromDocumentRequest
import org.example.project.tutor.useCases.getIdToken.GetIdTokenSuspendUseCase

data class TutorLoadResult(
    val documentId: String,
    val fileName: String,
    val language: String,
    val chatId: String,
    val prototype: LearningUnitsPrototypeDomain
)

class LoadTutorFromPdfUseCase(
    private val pdfUpload: PdfUploadServiceUseCase,
    private val getToken: GetIdTokenSuspendUseCase,
) {
    suspend fun loadTemplateForPickedPdf(
        picked: PickedFile,
        userId: String,
        mode: String,
        onProgress: (Int) -> Unit = {}
    ): TutorLoadResult {
        val token = getToken()

        val uploadResponse = pdfUpload(
            file = UploadFile(
                bytes = picked.bytes,
                fileName = picked.name,
                contentType = picked.mimeType.ifBlank {
                    "application/pdf"
                }
            ),
            token = token
        )

        val documentId = uploadResponse.documentId

        var completedResult:
                org.example.project.tutor.networkModel.uploadPdf.LearningUnitsPrototypeDto? = null

        var attempts = 0

        while (
            completedResult == null &&
            attempts < 240
        ) {
            attempts++

            val status = pdfUpload.getPdfJobStatus(
                documentId = documentId,
                token = token
            )
            status.progress?.let { progress ->
                onProgress(progress)
            }
            when (
                status.status
                    .trim()
                    .lowercase()
            ) {
                "done" -> {
                    completedResult =
                        status.result
                            ?: status.partialResult
                                    ?: error(
                                "PDF processing completed without learning units"
                            )
                }

                "error",
                "failed" -> {
                    error(
                        status.error
                            ?: "PDF processing failed"
                    )
                }

                else -> {
                    delay(2_000)
                }
            }
        }

        val dto = completedResult
            ?: error("PDF processing timed out")

        val chatResponse = pdfUpload.createChatFromDocument(
            request = CreateChatFromDocumentRequest(
                userId = userId,
                documentId = documentId,
                mode = mode
            ),
            token = token
        )

        val chat = chatResponse.chat

        val chatDto = chat.result
            ?: chat.partialResult
            ?: dto

        val language = chatDto.language
            .orEmpty()
            .ifBlank {
                dto.language
                    .orEmpty()
                    .ifBlank { "en" }
            }

        return TutorLoadResult(
            documentId = documentId,
            fileName = picked.name,
            language = language,
            chatId = chat.chatId,
            prototype = chatDto.toDomain()
        )
    }

}
