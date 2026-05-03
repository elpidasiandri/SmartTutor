package org.example.project.myProgress.mappers

import org.example.project.myProgress.models.ChatProgressDomain
import org.example.project.myProgress.models.ProgressOverviewDomain
import org.example.project.myProgress.networkModel.ChatProgressDto
import org.example.project.myProgress.networkModel.UserProgressOverviewDto
import org.example.project.tutor.models.domainModels.myProgress.ChatProgressUpsertDomain

fun UserProgressOverviewDto.toDomain() = ProgressOverviewDomain(
    userId = userId,
    totalChats = totalChats,
    totalDocuments = totalDocuments,
    totalMessagesSent = totalMessagesSent,
    lastActivityEpochMillis = lastActivityEpochMillis,
    streakDays = streakDays
)

fun ChatProgressDto.toDomain() = ChatProgressDomain(
    chatId = chatId,
    userId = userId,
    documentId = documentId,
    title = title,
    totalUnits = totalUnits,
    currentUnitIndex = currentUnitIndex,
    selectedTab = selectedTab,
    messagesSentCount = messagesSentCount,
    availableExercisesCount = availableExercisesCount,
    lastActivityEpochMillis = lastActivityEpochMillis,
    progressPercent = progressPercent
)

fun ChatProgressUpsertDomain.toDto(): ChatProgressDto {
    return ChatProgressDto(
        chatId = chatId,
        userId = userId,
        documentId = documentId,
        title = title,
        totalUnits = totalUnits,
        currentUnitIndex = currentUnitIndex,
        selectedTab = selectedTab,
        messagesSentCount = messagesSentCount,
        availableExercisesCount = availableExercisesCount,
        lastActivityEpochMillis = lastActivityEpochMillis,
        progressPercent = progressPercent
    )
}