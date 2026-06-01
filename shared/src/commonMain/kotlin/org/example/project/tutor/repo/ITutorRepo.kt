package org.example.project.tutor.repo

import org.example.project.myProgress.networkModel.ExerciseAnswerProgressDto
import org.example.project.tutor.models.domainModels.file.UploadFile
import org.example.project.tutor.networkModel.chatHistory.AddMoreExercisesResponse
import org.example.project.tutor.networkModel.chatHistory.AskTutorRequest
import org.example.project.tutor.networkModel.chatHistory.AskTutorResponse
import org.example.project.tutor.networkModel.chatHistory.CreateChatFromDocumentRequest
import org.example.project.tutor.networkModel.chatHistory.CreateChatFromDocumentResponse
import org.example.project.tutor.networkModel.chatHistory.UserChatDto
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
        token: String,
    ): CreateChatFromDocumentResponse
    suspend fun addMoreExercises(
        chatId: String,
        userId: String,
        unitIndex: Int,
        token: String
    ): AddMoreExercisesResponse

    suspend fun getChats(userId: String): List<UserChatDto>

    suspend fun getChat(
        chatId: String,
        userId: String,
    ): UserChatDto

    suspend fun askTutor(
        chatId: String,
        userId: String,
        request: AskTutorRequest,
        token: String,
    ): AskTutorResponse

    suspend fun saveExerciseAnswer(
        answer: ExerciseAnswerProgressDto,
        userId: String,
        token: String
    )

    suspend fun markUnitAsRead(
        userId: String,
        chatId: String,
        unitIndex: Int,
        token: String,
    )
    suspend fun deleteChat(
        chatId: String,
        userId: String,
        token: String,
    ) }