package org.example.project.myProgress.useCases

import org.example.project.myProgress.networkModel.ChatProgressDto
import org.example.project.myProgress.repo.IProgressRepo

class UpsertChatProgressUseCase(
    private val repo: IProgressRepo
) {
    suspend operator fun invoke(
        userId: String,
        token: String,
        progress: ChatProgressDto
    ) {
        repo.upsertChatProgress(
            userId = userId,
            token = token,
            progress = progress
        )
    }
}