package org.example.project.tutor.repo

import org.example.project.tutor.networkModel.chatHistory.ChatMessageDto
import org.example.project.tutor.networkModel.chatHistory.UserChatDto
import org.example.project.tutor.networkModel.uploadPdf.ExerciseDto

interface IChatRepository {
    fun createOrGetChatFromDocument(
        userId: String,
        documentId: String,
        mode: String,
    ): UserChatDto

    fun getChats(userId: String): List<UserChatDto>

    fun getChat(userId: String, chatId: String): UserChatDto?

    fun getMessages(userId: String, chatId: String): List<ChatMessageDto>
    suspend fun appendExercisesToUnit(
        userId: String,
        chatId: String,
        unitIndex: Int,
        exercises: List<ExerciseDto>
    ): Boolean

    fun addMessage(
        userId: String,
        chatId: String,
        message: ChatMessageDto
    )

    fun deleteChat(userId: String, chatId: String)
    fun markUnitAsRead(
        userId: String,
        chatId: String,
        unitIndex: Int,
    ): Boolean
}