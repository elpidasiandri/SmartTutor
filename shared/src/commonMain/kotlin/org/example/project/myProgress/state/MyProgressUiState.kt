package org.example.project.myProgress.state

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.example.project.myProgress.networkModel.ExerciseAnswerProgressDto

data class MyProgressState(
    val loading: Boolean = false,
    val error: String? = null,
    val showCustomMessage: Boolean = false,
    val message: String = "",
    val isError: Boolean = false,

    val totalChats: Int = 0,
    val totalUploadedPdfs: Int = 0,
    val totalQuestionsAsked: Int = 0,
    val totalExercisesCount: Int = 0,

    val currentSubjectTitle: String = "",
    val currentUnitIndex: Int = 0,
    val totalUnits: Int = 0,
    val selectedTab: String = "",
    val progressPercent: Int = 0,

    val lastActivityEpochMillis: Long = 0L,
    val lastActivityLabel: String = "",
    val streakDays: Int = 0,

    val recentChats: List<MyProgressChatItem> = emptyList(),

    val event: MyProgressEvents = MyProgressEvents.None,
    val uiEvent: MyProgressUiEvents = MyProgressUiEvents.None,
    val totalCorrectAnswers: Int = 0,
    val totalWrongAnswers: Int = 0,
    val unitExerciseStats: List<LearningUnitExerciseStatsUi> = emptyList(),
)

data class MyProgressChatItem(
    val chatId: String,
    val title: String,
    val progressPercent: Int,
    val lastActivityEpochMillis: Long = 0L,
)

data class ExerciseAnswerProgressDomain(
    val chatId: String,
    val userId: String,
    val unitIndex: Int,
    val unitTitle: String,
    val exerciseIndex: Int,
    val exerciseType: String,
    val question: String,
    val selectedAnswer: String,
    val correctAnswer: String,
    val isCorrect: Boolean,
    val timeToAnswerMs: Long,
    val answeredAtEpochMillis: Long,
)

fun ExerciseAnswerProgressDto.toDomain(): ExerciseAnswerProgressDomain {
    return ExerciseAnswerProgressDomain(
        chatId = chatId,
        userId = userId,
        unitIndex = unitIndex,
        unitTitle = unitTitle,
        exerciseIndex = exerciseIndex,
        exerciseType = exerciseType,
        question = question,
        selectedAnswer = selectedAnswer,
        correctAnswer = correctAnswer,
        isCorrect = isCorrect,
        timeToAnswerMs = timeToAnswerMs,
        answeredAtEpochMillis = answeredAtEpochMillis
    )
}

data class LearningUnitExerciseStatsUi(
    val unitIndex: Int,
    val unitTitle: String,
    val correctCount: Int,
    val wrongCount: Int,
    val totalAnswered: Int,
    val answers: List<ExerciseAnswerProgressDomain>,
)

sealed class MyProgressEvents {
    data object None : MyProgressEvents()
    data object Loaded : MyProgressEvents()
}

sealed class MyProgressUiEvents {
    data object None : MyProgressUiEvents()
    data object LoadProgress : MyProgressUiEvents()
    data object ClearUiEvent : MyProgressUiEvents()
    data object InitializeStateAfterShowingMessage : MyProgressUiEvents()
    data class ShowErrorMessage(val message: String) : MyProgressUiEvents()
}
