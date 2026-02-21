package org.example.project.tutor.useCases

import org.example.project.tutor.domainModels.file.UploadFile
import org.example.project.tutor.networkModel.uploadPdf.UploadPdfResponse
import org.example.project.tutor.repo.ITutorRepo

class PdfUploadServiceUseCase(private val repo :ITutorRepo) {
    suspend operator fun invoke(file: UploadFile, idToken: String): UploadPdfResponse {
        return repo.uploadFile(file, idToken)
    }
}