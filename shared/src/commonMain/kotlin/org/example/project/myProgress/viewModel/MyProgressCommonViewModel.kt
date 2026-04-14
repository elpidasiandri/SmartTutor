package org.example.project.myProgress.viewModel

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.example.project.myProgress.MyProgressChatItem
import org.example.project.myProgress.MyProgressEvents
import org.example.project.myProgress.MyProgressState
import org.example.project.myProgress.MyProgressUiEvents
import org.example.project.settings.SettingsUiEvents
import org.example.project.tutor.commonViewModel.TutorPlatform
import org.example.project.tutor.repo.analytics.IAnalyticsTracker
import org.example.project.tutor.useCases.GetAllHistoryChatUseCase
import org.example.project.tutor.useCases.GetChatByIdUseCase

class MyProgressCommonViewModel(
    private val ioDispatcher: CoroutineScope,
    private val getAllHistoryChatUseCase: GetAllHistoryChatUseCase,
    private val getChatByIdUseCase: GetChatByIdUseCase,
    private val analyticsTracker: IAnalyticsTracker,
    private val userId: String,
    private val platform: TutorPlatform,
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
        ioDispatcher.launch {
            try {
                _state.update { it.copy(loading = true, error = null) }

                track("my_progress_opened")

                val chats = getAllHistoryChatUseCase(userId)
                val latestChat = chats.firstOrNull()?.let {
                    getChatByIdUseCase(it.chatId, userId)
                }

                val units = latestChat?.result?.learningUnits
                    ?: latestChat?.partialResult?.learningUnits
                    ?: emptyList()

                val totalUnits = units.size
                val currentUnitIndex = if (units.isNotEmpty()) 0 else 0
                val progressPercent =
                    if (totalUnits > 0) ((currentUnitIndex + 1) * 100) / totalUnits else 0

                val totalQuestionsAsked = latestChat?.messages
                    ?.count { it.sender.equals("USER", ignoreCase = true) }
                    ?: 0

                val totalExercisesCount = units.sumOf { it.exercises.size }

                _state.update {
                    it.copy(
                        loading = false,
                        totalChats = chats.size,
                        totalUploadedPdfs = chats.map { chat -> chat.documentId }.distinct().size,
                        totalQuestionsAsked = totalQuestionsAsked,
                        totalExercisesCount = totalExercisesCount,
                        currentSubjectTitle = latestChat?.title.orEmpty(),
                        currentUnitIndex = currentUnitIndex,
                        totalUnits = totalUnits,
                        selectedTab = latestChat?.mode.orEmpty(),
                        progressPercent = progressPercent,
                        lastActivityLabel = "Today",
                        streakDays = 1,
                        recentChats = chats.take(5).map { chat ->
                            MyProgressChatItem(
                                chatId = chat.chatId,
                                title = chat.title,
                                progressPercent = if (chat.chatId == latestChat?.chatId) progressPercent else 0
                            )
                        },
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
}