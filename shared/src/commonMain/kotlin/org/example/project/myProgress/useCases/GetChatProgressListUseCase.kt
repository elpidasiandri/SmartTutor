package org.example.project.myProgress.useCases

import org.example.project.myProgress.mappers.toDomain
import org.example.project.myProgress.repo.IProgressRepo
import org.example.project.myProgress.models.ChatProgressDomain


class GetChatProgressListUseCase(
    private val repo: IProgressRepo
) {
    suspend operator fun invoke(userId: String): List<ChatProgressDomain> {
        return repo.getChatProgressList(userId).map { it.toDomain() }
    }
}