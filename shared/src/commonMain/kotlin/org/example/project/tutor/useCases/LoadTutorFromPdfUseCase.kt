package org.example.project.tutor.useCases

import org.example.project.tutor.domainModels.file.PickedFile
import org.example.project.tutor.domainModels.learningUnits.LearningUnitsPrototypeDomain
import org.example.project.tutor.mappers.learningUnits.toDomain
import org.example.project.tutor.networkModel.uploadPdf.LanguageOfLearningUnitsEnum
import org.example.project.tutor.template.TemplateDocIds
import org.example.project.tutor.useCases.getIdToken.GetIdTokenSuspendUseCase

data class TutorLoadResult(
    val materialId: String,
    val fileName: String,
    val languageEnum: LanguageOfLearningUnitsEnum,
    val templateDocId: String,
    val prototype: LearningUnitsPrototypeDomain,
)

class LoadTutorFromPdfUseCase(
    private val pdfUpload: PdfUploadServiceUseCase,
    private val getToken: GetIdTokenSuspendUseCase,
) {
    suspend fun loadTemplateForPickedPdf(picked: PickedFile): TutorLoadResult {
        val token = getToken()

        val languageEnum = detectLanguage(picked) // Greek/English
        val templateDocId = TemplateDocIds.forLanguage(languageEnum)

        val status = pdfUpload.getPdfJobStatus(templateDocId, token)

        val dto = status.result ?: status.partialResult
        ?: error("Template has no resultJson/partialResult yet for $templateDocId")

        return TutorLoadResult(
            materialId = templateDocId,
            fileName = picked.name,
            languageEnum = languageEnum,
            templateDocId = templateDocId,
            prototype = dto.toDomain()
        )
    }

    private fun detectLanguage(picked: PickedFile): LanguageOfLearningUnitsEnum {
        val name = picked.name.lowercase()
        return if (name.contains("en") || name.contains("english")) {
            LanguageOfLearningUnitsEnum.English
        } else {
            LanguageOfLearningUnitsEnum.Greek
        }
    }
}