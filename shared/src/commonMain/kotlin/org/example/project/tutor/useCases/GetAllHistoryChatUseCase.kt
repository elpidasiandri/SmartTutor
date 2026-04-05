package org.example.project.tutor.useCases

import org.example.project.tutor.repo.ITutorRepo

class GetAllHistoryChatUseCase(
    private val repo: ITutorRepo
) {
    suspend operator fun invoke(userId: String) = repo.getChats(userId)
}