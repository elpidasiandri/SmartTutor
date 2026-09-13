package org.example.project.tutor.networkModel.chatHistory

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.example.project.tutor.networkModel.uploadPdf.ExerciseDto
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
    @SerialName("mode") val mode: String,
)

@Serializable
data class UserChatDto(
    @SerialName("chatId") val chatId: String,
    @SerialName("userId") val userId: String,
    @SerialName("documentId") val documentId: String,
    @SerialName("title") val title: String,
    @SerialName("language") val language: String? = null,
    @SerialName("status") val status: String,
    @SerialName("mode") val mode: String,
    @SerialName("partialResult") val partialResult: LearningUnitsPrototypeDto? = null,
    @SerialName("result") val result: LearningUnitsPrototypeDto? = null,
    @SerialName("messages") val messages: List<ChatMessageDto> = emptyList(),
    @SerialName("createdAtEpochMillis") val createdAtEpochMillis: Long,
    @SerialName("updatedAtEpochMillis") val updatedAtEpochMillis: Long,
)

@Serializable
data class CreateChatFromDocumentResponse(
    @SerialName("chat") val chat: UserChatDto,
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
data class AskTutorRequest(
    @SerialName("unitIndex")
    val unitIndex: Int?,
    @SerialName("responseLanguage")
    val responseLanguage: String,
    @SerialName("message")
    val message: String?,

    @SerialName("tab")
    val tab: String,

    @SerialName("contextText")
    val contextText: String? = null,

    @SerialName("saveToChat")
    val saveToChat: Boolean = true,

    @SerialName("isExerciseEvaluation")
    val isExerciseEvaluation: Boolean = false,

    @SerialName("exerciseIndex")
    val exerciseIndex: Int? = null,
    @SerialName("exerciseQuestion") val exerciseQuestion: String? = null,
    @SerialName("suggestedSolution") val suggestedSolution: String? = null,
    @SerialName("maxPoints") val maxPoints: Double? = null,
)

@Serializable
data class OpenExerciseEvaluationDto(
    @SerialName("awardedPoints")
    val awardedPoints: Double,

    @SerialName("maxPoints")
    val maxPoints: Double,

    @SerialName("gradingStatus")
    val gradingStatus: String,

    @SerialName("isCorrect")
    val isCorrect: Boolean,

    @SerialName("feedback")
    val feedback: String,

    @SerialName("improvementSuggestion")
    val improvementSuggestion: String? = null,

    @SerialName("exampleAnswer")
    val exampleAnswer: String? = null,
)

@Serializable
data class AskTutorResponse(
    @SerialName("userMessage") val userMessage: ChatMessageDto?,
    @SerialName("aiMessage") val aiMessage: ChatMessageDto?,
    @SerialName("evaluation") val evaluation: OpenExerciseEvaluationDto? = null,
)

@Serializable
data class AddMoreExercisesResponse(
    @SerialName("exercises")
    val exercises: List<ExerciseDto>,
)

