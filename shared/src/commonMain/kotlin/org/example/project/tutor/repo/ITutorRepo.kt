package org.example.project.tutor.repo

import org.example.project.tutor.domainModels.file.UploadFile
import org.example.project.tutor.networkModel.chatHistory.CreateChatFromDocumentRequest
import org.example.project.tutor.networkModel.chatHistory.CreateChatFromDocumentResponse
import org.example.project.tutor.networkModel.learningUnits.PdfJobStatusRawResponse
import org.example.project.tutor.networkModel.uploadPdf.PdfJobStatusResponse
import org.example.project.tutor.networkModel.uploadPdf.UploadPdfResponse

interface ITutorRepo {
    suspend fun uploadFile(file: UploadFile, idToken: String): UploadPdfResponse
    fun getIdToken(onResult: (Boolean, String?) -> Unit)
    suspend fun getIdTokenSuspend(forceRefresh: Boolean = true): String
    suspend fun getPdfJobStatus(documentId: String, idToken: String): PdfJobStatusResponse
    suspend fun getPdfJobRaw(documentId: String, idToken: String): PdfJobStatusRawResponse

    suspend fun createChatFromDocument(
        request: CreateChatFromDocumentRequest,
        token: String
    ): CreateChatFromDocumentResponse
}