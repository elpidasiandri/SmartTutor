package org.example.project.myProgress.models

data class ProgressOverviewDomain(
    val userId: String = "",
    val totalChats: Int = 0,
    val totalDocuments: Int = 0,
    val totalMessagesSent: Int = 0,
    val lastActivityEpochMillis: Long = 0L,
    val streakDays: Int = 0
)

data class ChatProgressDomain(
    val chatId: String = "",
    val userId: String = "",
    val documentId: String = "",
    val title: String = "",
    val totalUnits: Int = 0,
    val currentUnitIndex: Int = 0,
    val selectedTab: String = "",
    val messagesSentCount: Int = 0,
    val availableExercisesCount: Int = 0,
    val lastActivityEpochMillis: Long = 0L,
    val progressPercent: Int = 0,
    val units: List<UnitReadProgressDomain> = emptyList()
)

data class UnitReadProgressDomain(
    val unitIndex: Int = 0,
    val unitTitle: String = "",
    val isRead: Boolean = false
)