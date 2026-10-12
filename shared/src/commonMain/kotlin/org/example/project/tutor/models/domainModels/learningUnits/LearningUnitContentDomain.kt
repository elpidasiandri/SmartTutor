package org.example.project.tutor.models.domainModels.learningUnits

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
    val isRead: Boolean = false,
    val totalPoints: Double = 0.0,
)


data class ExerciseDomain(
    val type: ExerciseType,
    val difficulty: String,
    val estimatedMinutes: Int,
    val question: String,
    val options: List<String>,
    val answerIndex: Int?,
    val hint: String,
    val explanation: String,
    val solution: String,
    val maxPoints: Double,
    val attempts: List<LearningUnitExerciseStatsDomain>,
    val introducedInRound: Int = 1,
){
    val latestAttempt: LearningUnitExerciseStatsDomain?
        get() = attempts.maxByOrNull { it.attemptNumber }

    val isAnswered: Boolean
        get() = attempts.isNotEmpty()

    val attemptsCount: Int
        get() = attempts.size

    val nextRequiredRound: Int
        get() = introducedInRound + attemptsCount

    fun hasAnsweredInRound(round: Int): Boolean =
        round >= introducedInRound &&
                nextRequiredRound > round

    val latestAwardedPoints: Double
        get() = latestAttempt?.awardedPoints ?: 0.0

    val latestGradingStatus: String
        get() = latestAttempt?.gradingStatus.orEmpty()
}

data class LearningUnitExerciseStatsDomain(
    val attemptNumber: Int,
    val awardedPoints: Double,
    val gradingStatus: String, // correct | partial | wrong
    val isCorrect: Boolean,
    val selectedAnswer: String,
    val answeredAtEpochMillis: Long,
)

enum class ExerciseType {
    MCQ,
    TRUE_FALSE,
    OPEN,
    NONE
}

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

