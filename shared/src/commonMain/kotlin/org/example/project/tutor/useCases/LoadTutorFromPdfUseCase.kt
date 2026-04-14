package org.example.project.tutor.useCases

import org.example.project.tutor.models.domainModels.file.PickedFile
import org.example.project.tutor.models.domainModels.learningUnits.LearningUnitsPrototypeDomain
import org.example.project.tutor.mappers.learningUnits.toDomain
import org.example.project.tutor.networkModel.chatHistory.CreateChatFromDocumentRequest
import org.example.project.tutor.networkModel.uploadPdf.LanguageOfLearningUnitsEnum
import org.example.project.tutor.template.TemplateDocIds
import org.example.project.tutor.useCases.getIdToken.GetIdTokenSuspendUseCase

data class TutorLoadResult(
    val materialId: String,
    val fileName: String,
    val languageEnum: LanguageOfLearningUnitsEnum,
    val templateDocId: String,
    val chatId: String,
    val prototype: LearningUnitsPrototypeDomain
)

class LoadTutorFromPdfUseCase(
    private val pdfUpload: PdfUploadServiceUseCase,
    private val getToken: GetIdTokenSuspendUseCase,
) {
    suspend fun loadTemplateForPickedPdf(
        picked: PickedFile,
        userId: String,
        mode :String
    ): TutorLoadResult {
        val token = getToken()

        val languageEnum = detectLanguage(picked)
        val templateDocId = TemplateDocIds.forLanguage(languageEnum)

        val chatResponse = pdfUpload.createChatFromDocument(
            request = CreateChatFromDocumentRequest(
                userId = userId,
                documentId = templateDocId,
                mode = mode
            ),
            token = token
        )

        val chat = chatResponse.chat
        val dto = chat.result ?: chat.partialResult
        ?: error("Chat has no result/partialResult for $templateDocId")

        return TutorLoadResult(
            materialId = templateDocId,
            fileName = picked.name,
            languageEnum = languageEnum,
            templateDocId = templateDocId,
            chatId = chat.chatId,
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
