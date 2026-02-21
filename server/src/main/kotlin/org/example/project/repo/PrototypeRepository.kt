package org.example.project.repo

import org.example.project.tutor.networkModel.uploadPdf.LearningUnitsPrototypeDto
import org.example.project.tutor.networkModel.uploadPdf.PdfJobStatusResponse

interface PdfJobRepository {

    fun createAndEnqueue(
        documentId: String,
        fileName: String,
        language: String,
        storagePath: String
    )

    fun claimNextJob(workerId: String): PdfJob?

    fun heartbeat(documentId: String, workerId: String)
    fun setProgress(documentId: String, progress: Int)

    fun savePartialResult(documentId: String, partial: LearningUnitsPrototypeDto, progress: Int)
    fun completeSuccess(documentId: String, result: LearningUnitsPrototypeDto)
    fun completeError(documentId: String, message: String)

    fun saveLastLlmRaw(documentId: String, raw: String)
    fun getLastLlmRaw(documentId: String): String?

    fun getStatus(documentId: String): PdfJobStatusResponse?
}

data class PdfJob(
    val documentId: String,
    val fileName: String,
    val language: String,
    val storagePath: String
)
