package org.example.project.tutor.template

import org.example.project.shared.DocConfig.DOC_ENGLISH
import org.example.project.shared.DocConfig.DOC_GREEK
import org.example.project.tutor.networkModel.uploadPdf.LanguageOfLearningUnitsEnum


object TemplateDocIds {
    fun forLanguage(lang: LanguageOfLearningUnitsEnum): String =
        when (lang) {
            LanguageOfLearningUnitsEnum.English -> DocConfig.DOC_ENGLISH_ID
            LanguageOfLearningUnitsEnum.Greek -> DocConfig.DOC_GREEK_ID
        }
}

object DocConfig {
    const val DOC_ENGLISH_ID = DOC_ENGLISH
    const val DOC_GREEK_ID = DOC_GREEK
}