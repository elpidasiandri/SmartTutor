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
import org.example.project.myProgress.state.toDomain
import org.example.project.myProgress.useCases.GetChatProgressListUseCase
import org.example.project.myProgress.useCases.GetExerciseStatsUseCase
import org.example.project.myProgress.useCases.GetProgressOverviewUseCase
import org.example.project.tutor.commonViewModel.TutorPlatform
import org.example.project.tutor.repo.analytics.IAnalyticsTracker

class MyProgressCommonViewModel(
    private val scope: CoroutineScope,
    private val analyticsTracker: IAnalyticsTracker,
    private val userId: String,
    private val platform: TutorPlatform,
    private val getProgressOverviewUseCase: GetProgressOverviewUseCase,
    private val getChatProgressListUseCase: GetChatProgressListUseCase,
    private val getExerciseStateUseCase: GetExerciseStatsUseCase,
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
                _state.update { it.copy(loading = true, error = null) }

                track("my_progress_opened")

                val overview = getProgressOverviewUseCase(userId)
                val chatProgressList = getChatProgressListUseCase(userId)

                val latestChat = chatProgressList.maxByOrNull { it.lastActivityEpochMillis }
                val exerciseStats = getExerciseStateUseCase(userId)
                val statsByUnitIndex = exerciseStats.associateBy { it.unitIndex }

                val unitExerciseStatsUi = latestChat?.units.orEmpty().map { chatUnit ->
                    val exerciseState = statsByUnitIndex[chatUnit.unitIndex]

                    val questions = exerciseState
                        ?.questions
                        ?.map { it.toDomain() }
                        .orEmpty()

                    val answeredUniqueExercises = questions
                        .map { it.exerciseIndex }
                        .distinct()
                        .size

                    val awardedPoints = questions.sumOf { question ->
                        question.attempts
                            .maxByOrNull { it.attemptNumber }
                            ?.awardedPoints
                            ?: 0.0
                    }

                    LearningUnitExerciseStatsUi(
                        unitIndex = chatUnit.unitIndex,
                        unitTitle = chatUnit.unitTitle,
                        correctCount = exerciseState?.correctCount ?: 0,
                        wrongCount = exerciseState?.wrongCount ?: 0,
                        totalAnswered = answeredUniqueExercises,
                        totalExercises = chatUnit.totalExercises,
                        totalMaxPoints = chatUnit.totalMaxPoints,
                        awardedPoints = awardedPoints,
                        isRead = chatUnit.isRead,
                        questions = questions
                    )
                }

                val totalLearningUnits = unitExerciseStatsUi.size

                val readLearningUnits = unitExerciseStatsUi.count { unit ->
                    unit.isRead
                }

                val totalExercises = unitExerciseStatsUi.sumOf { unit ->
                    unit.totalExercises
                }

                val answeredExercises = unitExerciseStatsUi.sumOf { unit ->
                    unit.totalAnswered.coerceAtMost(unit.totalExercises)
                }

                val totalProgressItems = totalLearningUnits + totalExercises
                val completedProgressItems = readLearningUnits + answeredExercises

                val calculatedProgressPercent = if (totalProgressItems == 0) {
                    0
                } else {
                    ((completedProgressItems * 100) / totalProgressItems)
                        .coerceIn(0, 100)
                }

                _state.update {
                    it.copy(
                        loading = false,
                        totalChats = overview.totalChats,
                        totalUploadedPdfs = overview.totalDocuments,
                        totalQuestionsAsked = overview.totalMessagesSent,
                        totalExercisesCount = latestChat?.availableExercisesCount ?: 0,
                        currentSubjectTitle = latestChat?.title.orEmpty(),
                        currentUnitIndex = latestChat?.currentUnitIndex ?: 0,
                        totalUnits = latestChat?.totalUnits ?: 0,
                        selectedTab = latestChat?.selectedTab.orEmpty(),
                        progressPercent = calculatedProgressPercent,
                        lastActivityEpochMillis = overview.lastActivityEpochMillis,
                        lastActivityLabel = formatLastActivity(overview.lastActivityEpochMillis),
                        streakDays = overview.streakDays,
                        totalCorrectAnswers = exerciseStats.sumOf { exerciseState -> exerciseState.correctCount },
                        totalWrongAnswers = exerciseStats.sumOf { exerciseState -> exerciseState.wrongCount },
                        totalPartialAnswers = exerciseStats.sumOf { exerciseState -> exerciseState.partialCount },
                        unitExerciseStats = unitExerciseStatsUi,
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
