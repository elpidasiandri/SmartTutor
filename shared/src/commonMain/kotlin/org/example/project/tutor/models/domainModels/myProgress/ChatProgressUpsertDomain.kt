package org.example.project.tutor.models.domainModels.myProgress

import org.example.project.myProgress.models.UnitReadProgressDomain

data class ChatProgressUpsertDomain(
    val chatId: String,
    val userId: String,
    val documentId: String,
    val title: String,
    val totalUnits: Int,
    val currentUnitIndex: Int,
    val selectedTab: String,
    val messagesSentCount: Int,
    val availableExercisesCount: Int,
    val lastActivityEpochMillis: Long,
    val progressPercent: Int,
    val units: List<UnitReadProgressDomain> = emptyList()
)