package org.example.project.tutor.useCases

import org.example.project.tutor.networkModel.chatHistory.UserChatDto
import org.example.project.tutor.repo.ITutorRepo

class GetChatByIdUseCase(
    private val tutorRepo: ITutorRepo
) {
    suspend operator fun invoke(chatId: String, userId: String): UserChatDto {
        return tutorRepo.getChat(chatId, userId)
    }
}