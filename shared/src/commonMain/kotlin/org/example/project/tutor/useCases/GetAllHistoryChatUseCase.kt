package org.example.project.tutor.useCases

import org.example.project.tutor.networkModel.chatHistory.UserChatDto
import org.example.project.tutor.repo.ITutorRepo

class GetAllHistoryChatUseCase(
    private val repo: ITutorRepo
) {
    suspend operator fun invoke(userId: String): List<UserChatDto> {
        require(userId.isNotBlank()) {
            "EMPTY userId before /v1/chats"
        }

        return repo.getChats(userId)
    }
}