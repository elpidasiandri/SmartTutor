package org.example.project.tutor.useCases

import org.example.project.tutor.repo.IChatRepository

class MarkUnitAsReadUseCase(
    private val repo: IChatRepository,
) {
    suspend operator fun invoke(
        userId: String,
        chatId: String,
        unitIndex: Int,
    ) = repo.markUnitAsRead(
        userId = userId,
        chatId = chatId,
        unitIndex = unitIndex,
    )
}