package org.example.project.tutor.useCases

import org.example.project.strings.AppLanguage
import org.example.project.strings.LanguageManager
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
        saveToChat: Boolean,
        isExerciseEvaluation: Boolean = false,
        exerciseIndex: Int? = null,
        exerciseQuestion: String? = null,
        suggestedSolution: String? = null,
        maxPoints: Double? = null,
    ): AskTutorResponse {
        return repo.askTutor(
            chatId = chatId,
            userId = userId,
            request = AskTutorRequest(
                unitIndex = unitIndex,
                message = message,
                tab = chatMode,
                saveToChat = saveToChat,
                isExerciseEvaluation = isExerciseEvaluation,
                exerciseIndex = exerciseIndex,
                responseLanguage = when (LanguageManager.language.value) {
                    AppLanguage.GREEK -> "el"
                    AppLanguage.ENGLISH -> "en"
                },
                exerciseQuestion = exerciseQuestion,
                suggestedSolution = suggestedSolution,
                maxPoints = maxPoints,
            ),
            token = token
        )
    }
}