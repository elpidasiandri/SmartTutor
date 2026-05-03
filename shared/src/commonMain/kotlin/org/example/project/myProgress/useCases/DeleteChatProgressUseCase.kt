package org.example.project.myProgress.useCases

import org.example.project.myProgress.repo.IProgressRepo

class DeleteChatProgressUseCase(
    private val repo: IProgressRepo
) {
    suspend operator fun invoke(
        userId: String,
        token: String,
        chatId: String
    ) {
        repo.deleteChatProgress(
            userId = userId,
            token = token,
            chatId = chatId
        )
    }
}