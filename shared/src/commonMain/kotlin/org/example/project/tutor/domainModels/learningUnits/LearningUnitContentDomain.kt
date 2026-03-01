package org.example.project.tutor.domainModels.learningUnits

import org.example.project.tutor.networkModel.uploadPdf.LanguageOfLearningUnitsEnum

data class LearningUnitsPrototypeDomain(
    val title: String = "",
    val language: String = LanguageOfLearningUnitsEnum.Greek.name,
    val totalUnits: Int = 0,
    val learningUnits: List<LearningUnitContentDomain> = emptyList(),
)

data class LearningUnitContentDomain(
    val title: String = "",
    val numbering: Int = 0,
    val text: String = "",
    val images: List<UnitImageDomain> = emptyList(),
    val quotations: List<QuotationDomain> = emptyList(),
    val exercises: List<ExerciseDomain> = emptyList(),
)

data class ExerciseDomain(
    val type: String,
    val difficulty: String,
    val estimatedMinutes: Int,
    val question: String,
    val options: List<String>,
    val answerIndex: Int?,
    val hint: String,
    val explanation: String,
    val solution: String,
)
enum class ExerciseType { MCQ, TRUE_FALSE, OPEN }
data class UnitImageDomain(
    val relativePath: String = "",
    val caption: String = "",
    val prompt: String = "",
    val numbering: Int = 0,
)

data class QuotationDomain(
    val text: String = "",
    val numbering: Int = 0,
)

