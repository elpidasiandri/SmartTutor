package org.example.project.myProgress.repo

import org.example.project.myProgress.networkModel.ChatProgressDto
import org.example.project.myProgress.networkModel.ExerciseAnswerProgressDto
import org.example.project.myProgress.networkModel.LearningUnitExerciseStatsDto
import org.example.project.myProgress.networkModel.UserProgressOverviewDto

interface IProgressRepository {
    fun upsertChatProgress(
        userId: String,
        progress: ChatProgressDto
    )

    fun incrementMessageCount(
        userId: String,
        chatId: String,
        delta: Int = 1
    )

    fun updateOverview(
        userId: String
    )

    fun getOverview(userId: String): UserProgressOverviewDto?

    fun getChatProgressList(userId: String): List<ChatProgressDto>

    fun deleteChatProgress(userId: String, chatId: String)
    suspend fun saveExerciseAnswer(userId: String, answer: ExerciseAnswerProgressDto)

    suspend fun getExerciseStats(userId: String): List<LearningUnitExerciseStatsDto>
}