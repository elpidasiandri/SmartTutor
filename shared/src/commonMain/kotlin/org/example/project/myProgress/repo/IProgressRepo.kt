package org.example.project.myProgress.repo

import org.example.project.myProgress.networkModel.ChatProgressDto
import org.example.project.myProgress.networkModel.LearningUnitExerciseStatsDto
import org.example.project.myProgress.networkModel.UserProgressOverviewDto

interface IProgressRepo {
    suspend fun getOverview(userId: String): UserProgressOverviewDto
    suspend fun getChatProgressList(userId: String): List<ChatProgressDto>
    suspend fun upsertChatProgress(progress: ChatProgressDto, userId: String, token: String)
    suspend fun deleteChatProgress(chatId: String, userId: String, token: String)
    suspend fun getExerciseStats(userId: String): List<LearningUnitExerciseStatsDto>

}