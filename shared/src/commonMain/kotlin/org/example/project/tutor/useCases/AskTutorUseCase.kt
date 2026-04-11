package org.example.project.tutor.useCases

import org.example.project.tutor.networkModel.chatHistory.AskTutorRequest
import org.example.project.tutor.networkModel.chatHistory.AskTutorResponse
import org.example.project.tutor.repo.ITutorRepo

class AskTutorUseCase(
    private val repo: ITutorRepo
) {
    suspend operator fun invoke(
        chatId: String,
        userId: String,
        unitIndex: Int,
        message: String,
        token: String,
        chatMode: String,
    ): AskTutorResponse {
        return repo.askTutor(
            chatId = chatId,
            userId = userId,
            request = AskTutorRequest(
                unitIndex = unitIndex,
                message = message,
                tab = chatMode
            ),
            token = token
        )
    }
}