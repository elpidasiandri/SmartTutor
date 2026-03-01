package org.example.project.utils.validation

import org.example.project.tutor.domainModels.file.PickedFile

private const val MAX_PDF_SIZE = 50L * 1024 * 1024 // 50MB

fun validatePdf(file: PickedFile): PdfValidationResult {
    return when {
        file.mimeType != "application/pdf" ->
            PdfValidationResult.Error("Only PDF files are allowed")

        file.sizeBytes > MAX_PDF_SIZE ->
            PdfValidationResult.Error("The PDF file is over than 50MB")

        else -> PdfValidationResult.Success(file)
    }
}

sealed class PdfValidationResult {
    data class Success(val file: PickedFile) : PdfValidationResult() {
        override fun toString(): String = "Success(file=${file.name}, size=${file.bytes.size}B)"
    }    data class Error(val message: String) : PdfValidationResult()
}
