package org.example.project.tutor.useCases

import org.example.project.myProgress.networkModel.ExerciseAnswerProgressDto
import org.example.project.myProgress.repo.IProgressRepository
import org.example.project.tutor.repo.ITutorRepo

class SaveExerciseAnswerUseCase(
    private val progressRepo: ITutorRepo
) {

    suspend operator fun invoke(
        userId: String,
        token: String,
        answer: ExerciseAnswerProgressDto,
    ) {
        progressRepo.saveExerciseAnswer(
            answer = answer,
            userId = userId,
            token = token,
        )
    }
}