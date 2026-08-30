package org.example.project.tutor.useCases

import org.example.project.tutor.networkModel.uploadPdf.UnitExercisesOnlyDto
import org.example.project.tutor.repo.ITutorRepo

class LoadInitialExercisesUseCase(
    private val repo: ITutorRepo,
) {
    suspend operator fun invoke(
        documentId: String,
        unitNumber: Int,
        token: String,
    ): UnitExercisesOnlyDto {
        return repo.getOrGenerateUnitExercises(
            documentId = documentId,
            unitNumber = unitNumber,
            token = token,
        )
    }
}
