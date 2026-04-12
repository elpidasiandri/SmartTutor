package org.example.project.tutor.useCases

import org.example.project.tutor.repo.ITutorRepo

class DeleteChatUseCase(
    private val repo: ITutorRepo,
) {
    suspend operator fun invoke(
        chatId: String,
        userId: String,
        token: String,
    ) {
        repo.deleteChat(chatId, userId, token)
    }
}