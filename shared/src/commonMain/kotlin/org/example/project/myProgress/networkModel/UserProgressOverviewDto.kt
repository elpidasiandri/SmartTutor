package org.example.project.myProgress.networkModel

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

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
)