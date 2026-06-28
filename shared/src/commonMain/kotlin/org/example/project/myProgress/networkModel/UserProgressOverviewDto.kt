package org.example.project.myProgress.networkModel

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.example.project.tutor.networkModel.uploadPdf.ExerciseAttemptDto

@Serializable
data class UserProgressOverviewDto(
    @SerialName("userId") val userId: String = "",
    @SerialName("totalChats") val totalChats: Int = 0,
    @SerialName("totalDocuments") val totalDocuments: Int = 0,
    @SerialName("totalMessagesSent") val totalMessagesSent: Int = 0,
    @SerialName("lastActivityEpochMillis") val lastActivityEpochMillis: Long = 0L,
    @SerialName("streakDays") val streakDays: Int = 0,
)

@Serializable
data class ChatProgressDto(
    @SerialName("chatId") val chatId: String = "",
    @SerialName("userId") val userId: String = "",
    @SerialName("documentId") val documentId: String = "",
    @SerialName("title") val title: String = "",
    @SerialName("totalUnits") val totalUnits: Int = 0,
    @SerialName("currentUnitIndex") val currentUnitIndex: Int = 0,
    @SerialName("selectedTab") val selectedTab: String = "Learning",
    @SerialName("messagesSentCount") val messagesSentCount: Int = 0,
    @SerialName("availableExercisesCount") val availableExercisesCount: Int = 0,
    @SerialName("lastActivityEpochMillis") val lastActivityEpochMillis: Long = 0L,
    @SerialName("progressPercent") val progressPercent: Int = 0,
    @SerialName("units")
    val units: List<UnitReadProgressDto> = emptyList(),
)

@Serializable
data class UnitReadProgressDto(
    @SerialName("unitIndex") val unitIndex: Int = 0,
    @SerialName("unitTitle") val unitTitle: String = "",
    @SerialName("isRead") val isRead: Boolean = false,
)

@Serializable
data class ExerciseAnswerProgressDto(
    @SerialName("chatId") val chatId: String,
    @SerialName("userId") val userId: String,
    @SerialName("unitIndex") val unitIndex: Int,
    @SerialName("unitTitle") val unitTitle: String,
    @SerialName("exerciseIndex") val exerciseIndex: Int,
    @SerialName("exerciseType") val exerciseType: String,
    @SerialName("question") val question: String,
    @SerialName("selectedAnswer") val selectedAnswer: String,
    @SerialName("correctAnswer") val correctAnswer: String,
    @SerialName("isCorrect") val isCorrect: Boolean,
    @SerialName("timeToAnswerMs") val timeToAnswerMs: Long,
    @SerialName("answeredAtEpochMillis") val answeredAtEpochMillis: Long,
    @SerialName("maxPoints") val maxPoints: Double?,
    @SerialName("attempts") val attempts: List<ExerciseAttemptDto> = emptyList(),
)

@Serializable
data class LearningUnitExerciseStatsDto(
    @SerialName("unitIndex") val unitIndex: Int,
    @SerialName("unitTitle") val unitTitle: String,
    @SerialName("correctCount") val correctCount: Int,
    @SerialName("wrongCount") val wrongCount: Int,
    @SerialName("totalAnswered") val totalAnswered: Int,
    @SerialName("questions") val questions: List<ExerciseQuestionStatsDto> = emptyList(),
)

@Serializable
data class ExerciseQuestionStatsDto(
    @SerialName("exerciseIndex") val exerciseIndex: Int,
    @SerialName("question") val question: String,
    @SerialName("attemptsCount") val attemptsCount: Int,
    @SerialName("correctCount") val correctCount: Int,
    @SerialName("wrongCount") val wrongCount: Int,
    @SerialName("lastSelectedAnswer") val lastSelectedAnswer: String,
    @SerialName("correctAnswer") val correctAnswer: String,
)
