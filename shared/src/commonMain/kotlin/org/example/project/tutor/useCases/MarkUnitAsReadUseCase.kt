package org.example.project.tutor.useCases

import org.example.project.tutor.repo.ITutorRepo

class MarkUnitAsReadUseCase(
    private val repo: ITutorRepo,
) {
    suspend operator fun invoke(
        userId: String,
        chatId: String,
        unitIndex: Int,
        token: String,
    ) = repo.markUnitAsRead(
        userId = userId,
        chatId = chatId,
        unitIndex = unitIndex,
        token = token
    )
}