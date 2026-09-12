package org.example.project.myProgress.useCases

import org.example.project.myProgress.networkModel.LearningUnitExerciseStatsDto
import org.example.project.myProgress.repo.IProgressRepo

class GetExerciseStatsUseCase(
    private val repo: IProgressRepo,
) {
    suspend operator fun invoke(
        userId: String,
        chatId: String,
    ): List<LearningUnitExerciseStatsDto> {
        return repo.getExerciseStats(
            userId = userId,
            chatId = chatId,
        )
    }
}
