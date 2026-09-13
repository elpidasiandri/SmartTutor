package org.example.project.myProgress.viewModel

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import org.example.project.myProgress.state.LearningUnitExerciseStatsUi
import org.example.project.myProgress.state.MyProgressEvents
import org.example.project.myProgress.state.MyProgressState
import org.example.project.myProgress.state.MyProgressUiEvents
import org.example.project.myProgress.state.PdfProgressUi
import org.example.project.myProgress.state.toDomain
import org.example.project.myProgress.useCases.GetChatProgressListUseCase
import org.example.project.myProgress.useCases.GetExerciseStatsUseCase
import org.example.project.myProgress.useCases.GetProgressOverviewUseCase
import org.example.project.tutor.commonViewModel.TutorPlatform
import org.example.project.tutor.repo.analytics.IAnalyticsTracker
import org.example.project.tutor.useCases.GetChatByIdUseCase

class MyProgressCommonViewModel(
    private val scope: CoroutineScope,
    private val analyticsTracker: IAnalyticsTracker,
    private val userId: String,
    private val platform: TutorPlatform,
    private val getProgressOverviewUseCase: GetProgressOverviewUseCase,
    private val getChatProgressListUseCase: GetChatProgressListUseCase,
    private val getExerciseStateUseCase: GetExerciseStatsUseCase,
    private val getChatByIdUseCase: GetChatByIdUseCase,
) {
    private val _state = MutableStateFlow(MyProgressState())
    val state: StateFlow<MyProgressState> = _state

    init {
        loadProgress()
    }

    fun onEvent(event: MyProgressUiEvents) {
        when (event) {
            MyProgressUiEvents.LoadProgress -> loadProgress()
            MyProgressUiEvents.ClearUiEvent -> {
                _state.update {
                    it.copy(
                        uiEvent = MyProgressUiEvents.None,
                        event = MyProgressEvents.None
                    )
                }
            }

            MyProgressUiEvents.InitializeStateAfterShowingMessage -> {
                _state.update {
                    it.copy(
                        showCustomMessage = false,
                        message = "",
                        isError = false
                    )
                }
            }

            is MyProgressUiEvents.ShowErrorMessage -> {
                _state.update {
                    it.copy(
                        showCustomMessage = true,
                        message = event.message,
                        isError = true
                    )
                }
            }

            MyProgressUiEvents.None -> Unit
        }
    }

    private fun track(
        name: String,
        params: Map<String, String> = emptyMap(),
    ) {
        runCatching {
            analyticsTracker.sendEvent(
                name = name,
                params = params + mapOf("userId" to userId)
            )
        }.onFailure {
            platform.log("MyProgressAnalytics", "Failed to log event: $name", it)
        }
    }

    private fun loadProgress() {
        scope.launch {
            try {
                _state.update {
                    it.copy(
                        loading = true,
                        error = null
                    )
                }

                track("my_progress_opened")

                val overview = getProgressOverviewUseCase(userId)

                val chatProgressList = getChatProgressListUseCase(userId)

                val pdfProgressList = chatProgressList.map { chat ->
                    val fullChat = getChatByIdUseCase(chatId = chat.chatId, userId = userId)
                    val sourceUnits =
                        fullChat.result?.learningUnits ?: fullChat.partialResult?.learningUnits
                        ?: emptyList()
                    val exerciseStats =
                        getExerciseStateUseCase(userId = userId, chatId = chat.chatId)
                    val statsByUnitIndex = exerciseStats.associateBy { it.unitIndex }

                    val unitStats = chat.units.map { chatUnit ->

                        val sourceUnit = sourceUnits.getOrNull(chatUnit.unitIndex)
                        val exerciseState = statsByUnitIndex[chatUnit.unitIndex]
                        val questions = exerciseState?.questions?.map { it.toDomain() }.orEmpty()
                        val answeredUniqueExercises =
                            questions.map { it.exerciseIndex }.distinct().size


                        val actualExercises = sourceUnit?.exercises.orEmpty()

                        val totalExercises = maxOf(
                            20,
                            actualExercises.size,
                            chatUnit.totalExercises,
                            answeredUniqueExercises
                        )

                        val actualTotalMaxPoints = actualExercises.sumOf { it.maxPoints ?: 0.0 }

                        val totalMaxPoints = maxOf(actualTotalMaxPoints, chatUnit.totalMaxPoints)

                        val awardedPoints = questions.sumOf { question ->
                            question.attempts.maxByOrNull { it.attemptNumber }?.awardedPoints ?: 0.0
                        }

                        LearningUnitExerciseStatsUi(
                            unitIndex = chatUnit.unitIndex,
                            unitTitle = chatUnit.unitTitle,
                            correctCount = exerciseState?.correctCount ?: 0,
                            wrongCount = exerciseState?.wrongCount ?: 0,
                            totalAnswered = answeredUniqueExercises,
                            totalExercises = totalExercises,
                            totalMaxPoints = totalMaxPoints,
                            awardedPoints = awardedPoints,
                            isRead = chatUnit.isRead,
                            questions = questions
                        )
                    }
                    val totalLearningUnits = unitStats.size
                    val readLearningUnits = unitStats.count { it.isRead }
                    val totalExercises = unitStats.sumOf { it.totalExercises }
                    val answeredExercises =
                        unitStats.sumOf { it.totalAnswered.coerceAtMost(it.totalExercises) }
                    val totalProgressItems = totalLearningUnits + totalExercises
                    val completedProgressItems = readLearningUnits + answeredExercises
                    val progressPercent = if (totalProgressItems == 0) {
                        0
                    } else {
                        (completedProgressItems * 100 / totalProgressItems).coerceIn(0, 100)
                    }

                    PdfProgressUi(
                        chatId = chat.chatId,
                        documentId = chat.documentId,
                        title = chat.title,
                        progressPercent = progressPercent,
                        totalUnits = totalLearningUnits,
                        readUnits = readLearningUnits,
                        totalExercises = totalExercises,
                        answeredExercises = answeredExercises,
                        correctCount = exerciseStats.sumOf { it.correctCount },
                        wrongCount = exerciseStats.sumOf { it.wrongCount },
                        partialCount = exerciseStats.sumOf { it.partialCount },
                        unitExerciseStats = unitStats
                    )
                }

                val totalCorrect = pdfProgressList.sumOf { it.correctCount }

                val totalWrong = pdfProgressList.sumOf { it.wrongCount }
                val totalPartial = pdfProgressList.sumOf { it.partialCount }
                val totalExercises = pdfProgressList.sumOf { it.totalExercises }

                _state.update {
                    it.copy(
                        loading = false,
                        totalChats = overview.totalChats,
                        totalUploadedPdfs = overview.totalDocuments,
                        totalQuestionsAsked = overview.totalMessagesSent,
                        totalExercisesCount = totalExercises,
                        totalCorrectAnswers = totalCorrect,
                        totalWrongAnswers = totalWrong,
                        totalPartialAnswers = totalPartial,
                        lastActivityEpochMillis = overview.lastActivityEpochMillis,
                        lastActivityLabel = formatLastActivity(overview.lastActivityEpochMillis),
                        streakDays = overview.streakDays,
                        pdfProgress = pdfProgressList,
                        event = MyProgressEvents.Loaded
                    )
                }

            } catch (t: Throwable) {

                platform.log("MyProgressViewModel", "loadProgress failed", t)

                _state.update {
                    it.copy(
                        loading = false,
                        error = t.message,
                        showCustomMessage = true,
                        isError = true,
                        message = t.message ?: "Failed to load progress"
                    )
                }
            }
        }
    }

    private fun formatLastActivity(epochMillis: Long): String {
        if (epochMillis <= 0L) return "No activity yet"

        val now = Clock.System.now().toEpochMilliseconds()
        val diff = now - epochMillis
        val minutes = diff / 60_000
        val hours = diff / 3_600_000
        val days = diff / 86_400_000

        return when {
            minutes < 1 -> "Just now"
            minutes < 60 -> "$minutes min ago"
            hours < 24 -> "$hours h ago"
            days == 1L -> "Yesterday"
            else -> "$days days ago"
        }
    }


}
