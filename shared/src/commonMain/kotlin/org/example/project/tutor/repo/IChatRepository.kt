package org.example.project.tutor.repo

import org.example.project.tutor.networkModel.chatHistory.ChatMessageDto
import org.example.project.tutor.networkModel.chatHistory.UserChatDto

interface IChatRepository {
    fun createOrGetChatFromDocument(
        userId: String,
        documentId: String
    ): UserChatDto

    fun getChats(userId: String): List<UserChatDto>

    fun getChat(userId: String, chatId: String): UserChatDto?

    fun getMessages(userId: String, chatId: String): List<ChatMessageDto>

    fun addMessage(
        userId: String,
        chatId: String,
        message: ChatMessageDto
    )

    fun deleteChat(userId: String, chatId: String)
}