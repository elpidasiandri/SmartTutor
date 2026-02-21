package org.example.project.tutor.domainModels.learningUnits

import org.example.project.tutor.networkModel.learningUnits.LanguageOfLearningUnitsEnum

data class LearningUnitsPrototypeDomain(
    val title: String = "",
    val language: LanguageOfLearningUnitsEnum = LanguageOfLearningUnitsEnum.Greek,
    val totalUnits: Int = 0,
    val learningUnits: List<LearningUnitContentDomain> = emptyList(),
)


data class LearningUnitsPageDomain(
    val title: String = "",
    val language: LanguageOfLearningUnitsEnum = LanguageOfLearningUnitsEnum.Greek,
    val totalUnits: Int = 0,
    val offset: Int = 0,
    val limit: Int = 0,
    val learningUnits: List<LearningUnitContentDomain> = emptyList(),
) {
    val hasNext: Boolean get() = offset + learningUnits.size < totalUnits
    val nextOffset: Int get() = offset + learningUnits.size
}

data class LearningUnitContentDomain(
    val id: String = "",
    val title: String = "",
    val numbering: Int = 0,
    val text: String = "",
    val dsk: List<DskContentDomain> = emptyList(),
    val images: List<UnitImageDomain> = emptyList(),
    val quotations: List<QuotationDomain> = emptyList(),
    val exercises: List<ExerciseDomain> = emptyList(), // NEW
)

data class ExerciseDomain(
    val type: String,
    val question: String = "",
    val options: List<String> = emptyList(),
    val answerIndex: Int? = null,
    val explanation: String = "",
    val sampleAnswer: String = ""
)


data class DskContentDomain(
    val type: String? = null,
    val dsc: String = "",
    val caption: String = "",
    val numbering: Int = 0,
)

data class UnitImageDomain(
    val relativePath: String = "",
    val caption: String = "",
    val numbering: Int = 0,
)

data class QuotationDomain(
    val imageRelativePath: String = "",
    val text: String = "",
    val numbering: Int = 0,
)

