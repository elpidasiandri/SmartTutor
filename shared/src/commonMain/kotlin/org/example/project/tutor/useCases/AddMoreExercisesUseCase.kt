package org.example.project.tutor.useCases

import org.example.project.tutor.networkModel.chatHistory.AddMoreExercisesResponse
import org.example.project.tutor.repo.ITutorRepo

class AddMoreExercisesUseCase(private val repo: ITutorRepo) {
    suspend operator fun invoke(
        chatId: String,
        userId: String,
        unitIndex: Int,
        token: String
    ): AddMoreExercisesResponse {
        return repo.addMoreExercises(
            chatId = chatId,
            userId = userId,
            unitIndex = unitIndex,
            token = token
        )
    }
}