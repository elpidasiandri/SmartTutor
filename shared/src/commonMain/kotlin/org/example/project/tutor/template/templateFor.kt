package org.example.project.tutor.template

import org.example.project.tutor.networkModel.uploadPdf.LanguageOfLearningUnitsEnum


object TemplateDocIds {
    //TODO ELPIDA CHANGE
    const val EN = "doc_152cd7d8722041f7854e5c54bea61f02"
    const val EL = "doc_152cd7d8722041f7854e5c54bea61f02"

    fun forLanguage(lang: LanguageOfLearningUnitsEnum): String =
        when (lang) {
            LanguageOfLearningUnitsEnum.English -> EN
            LanguageOfLearningUnitsEnum.Greek -> EL
        }
}