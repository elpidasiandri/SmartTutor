package org.example.project.tutor.template

import org.example.project.tutor.networkModel.uploadPdf.LanguageOfLearningUnitsEnum


object TemplateDocIds {
    //TODO ELPIDA CHANGE
    const val EN = "doc_5bf2d6bd8f644c3a9508b4bf5bea7dd9"
    const val EL = "doc_7b3d895fd1184d09a7de53fea9f20f0d"

    fun forLanguage(lang: LanguageOfLearningUnitsEnum): String =
        when (lang) {
            LanguageOfLearningUnitsEnum.English -> EN
            LanguageOfLearningUnitsEnum.Greek -> EL
        }
}