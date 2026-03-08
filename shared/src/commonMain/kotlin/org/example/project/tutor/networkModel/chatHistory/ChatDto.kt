package org.example.project.tutor.networkModel.chatHistory

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.example.project.tutor.networkModel.uploadPdf.LearningUnitsPrototypeDto

enum class ChatSender(val value: String) {
    User("user"),
    Ai("ai"),
    System("system")
}

@Serializable
data class CreateChatFromDocumentRequest(
    @SerialName("userId") val userId: String,
    @SerialName("documentId") val documentId: String,
)

@Serializable
data class UserChatDto(
    @SerialName("chatId") val chatId: String,
    @SerialName("userId") val userId: String,
    @SerialName("documentId") val documentId: String,
    @SerialName("title") val title: String,
    @SerialName("language") val language: String? = null,
    @SerialName("status") val status: String,
    @SerialName("partialResult") val partialResult: LearningUnitsPrototypeDto? = null,
    @SerialName("result") val result: LearningUnitsPrototypeDto? = null,
    @SerialName("createdAtEpochMillis") val createdAtEpochMillis: Long,
    @SerialName("updatedAtEpochMillis") val updatedAtEpochMillis: Long,
)

@Serializable
data class CreateChatFromDocumentResponse(
    @SerialName("chat") val chat: UserChatDto
)

@Serializable
data class ChatMessageDto(
    @SerialName("messageId") val messageId: String,
    @SerialName("chatId") val chatId: String,
    @SerialName("sender") val sender: String, // user | ai | system
    @SerialName("text") val text: String,
    @SerialName("unitNumber") val unitNumber: Int? = null,
    @SerialName("unitTitle") val unitTitle: String? = null,
    @SerialName("createdAtEpochMillis") val createdAtEpochMillis: Long,
)

@Serializable
data class SendChatMessageRequest(
    @SerialName("userId") val userId: String,
    @SerialName("text") val text: String,
    @SerialName("unitNumber") val unitNumber: Int? = null,
    @SerialName("unitTitle") val unitTitle: String? = null,
)

@Serializable
data class ChatDetailsResponse(
    @SerialName("chat") val chat: UserChatDto,
    @SerialName("messages") val messages: List<ChatMessageDto> = emptyList(),
)