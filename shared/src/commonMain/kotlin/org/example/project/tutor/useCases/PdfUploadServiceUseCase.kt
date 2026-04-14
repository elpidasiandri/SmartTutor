package org.example.project.tutor.useCases

import org.example.project.tutor.models.domainModels.file.UploadFile
import org.example.project.tutor.networkModel.chatHistory.CreateChatFromDocumentRequest
import org.example.project.tutor.networkModel.chatHistory.CreateChatFromDocumentResponse
import org.example.project.tutor.networkModel.uploadPdf.UploadPdfResponse
import org.example.project.tutor.repo.ITutorRepo

class PdfUploadServiceUseCase(private val repo :ITutorRepo) {
    suspend operator fun invoke(file: UploadFile, token: String): UploadPdfResponse =
        repo.uploadFile(file, token)

    suspend fun createChatFromDocument(
        request: CreateChatFromDocumentRequest,
        token: String
    ): CreateChatFromDocumentResponse =
        repo.createChatFromDocument(request, token)
}