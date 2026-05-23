package org.example.project.tutor.useCases

import org.example.project.tutor.networkModel.uploadPdf.PdfJobStatusResponse
import org.example.project.tutor.repo.ITutorRepo

class MarkUnitAsReadUseCase(
    private val repo: ITutorRepo
) {
    suspend operator fun invoke(
        documentId: String,
        unitIndex: Int,
        token: String
    ): PdfJobStatusResponse {
        return repo.markUnitAsRead(
            documentId = documentId,
            unitIndex = unitIndex,
            token = token
        )
    }
}