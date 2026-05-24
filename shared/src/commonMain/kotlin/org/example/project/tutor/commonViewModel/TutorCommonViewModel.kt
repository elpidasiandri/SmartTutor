package org.example.project.tutor.commonViewModel

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import org.example.project.myProgress.mappers.toDto
import org.example.project.myProgress.networkModel.ExerciseAnswerProgressDto
import org.example.project.myProgress.useCases.DeleteChatProgressUseCase
import org.example.project.myProgress.useCases.UpsertChatProgressUseCase
import org.example.project.settings.logout.useCase.LearningUnitsUseCase
import org.example.project.settings.logout.useCase.LogoutUseCase
import org.example.project.strings.SmartTutorStrings
import org.example.project.strings.SmartTutorStrings.generic_error
import org.example.project.tutor.models.domainModels.file.PickedFile
import org.example.project.tutor.models.domainModels.learningUnits.LearningUnitContentDomain
import org.example.project.tutor.models.domainModels.tutorChat.ChatMessageDomain
import org.example.project.tutor.models.domainModels.tutorChat.SenderType
import org.example.project.tutor.models.domainModels.tutorChat.TutorTab
import org.example.project.tutor.mappers.chatHistory.buildChatSessionForSelectedUnit
import org.example.project.tutor.mappers.chatHistory.buildChatSessionFromStoredChat
import org.example.project.tutor.mappers.chatHistory.toChatHistoryDomain
import org.example.project.tutor.mappers.chatHistory.toDomain
import org.example.project.tutor.mappers.learningUnits.buildChatHistoryFromUnits
import org.example.project.tutor.mappers.learningUnits.buildChatSessionStart
import org.example.project.tutor.mappers.learningUnits.buildEmptyChatSessionStart
import org.example.project.tutor.mappers.learningUnits.buildExercisesTabMessage
import org.example.project.tutor.mappers.learningUnits.buildLearningTabMessage
import org.example.project.tutor.models.AnalyticsEvents.AI_REPLY_RECEIVED
import org.example.project.tutor.models.AnalyticsEvents.CHAT_DELETED
import org.example.project.tutor.models.AnalyticsEvents.CHAT_DELETE_FAILED
import org.example.project.tutor.models.AnalyticsEvents.EXERCISE_ANSWERED
import org.example.project.tutor.models.AnalyticsEvents.EXISTING_CHAT_OPENED
import org.example.project.tutor.models.AnalyticsEvents.EXISTING_CHAT_OPEN_FAILED
import org.example.project.tutor.models.AnalyticsEvents.LATEST_CHAT_AUTO_LOADED
import org.example.project.tutor.models.AnalyticsEvents.MESSAGE_SEND_FAILED
import org.example.project.tutor.models.AnalyticsEvents.MESSAGE_SENT
import org.example.project.tutor.models.AnalyticsEvents.MY_PROGRESS_OPENED
import org.example.project.tutor.models.AnalyticsEvents.NEW_CHAT_STARTED
import org.example.project.tutor.models.AnalyticsEvents.NEXT_UNIT_CLICKED
import org.example.project.tutor.models.AnalyticsEvents.PDF_UPLOADED
import org.example.project.tutor.models.AnalyticsEvents.PDF_UPLOAD_FAILED
import org.example.project.tutor.models.AnalyticsEvents.TAB_CHANGED
import org.example.project.tutor.models.AnalyticsEvents.TUTOR_SCREEN_OPENED
import org.example.project.tutor.models.domainModels.myProgress.ChatProgressUpsertDomain
import org.example.project.tutor.repo.analytics.IAnalyticsTracker
import org.example.project.tutor.state.TutorEvents
import org.example.project.tutor.state.TutorState
import org.example.project.tutor.state.TutorUiEvents
import org.example.project.tutor.useCases.AskTutorUseCase
import org.example.project.tutor.useCases.DeleteChatUseCase
import org.example.project.tutor.useCases.GetAllHistoryChatUseCase
import org.example.project.tutor.useCases.GetChatByIdUseCase
import org.example.project.tutor.useCases.LoadTutorFromPdfUseCase
import org.example.project.tutor.useCases.MarkUnitAsReadUseCase
import org.example.project.tutor.useCases.SaveExerciseAnswerUseCase
import org.example.project.tutor.useCases.getIdToken.GetIdTokenSuspendUseCase
import org.example.project.utils.EmptyValues
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid


interface TutorPlatform {
    fun speak(text: String)
    fun startVoice(onResult: (String) -> Unit)
    fun stopVoice()
    fun log(tag: String, message: String, t: Throwable? = null)
}

class TutorCommonViewModel(
    private val logoutUseCase: LogoutUseCase,
    private val getLearningUnitUseCase: LearningUnitsUseCase,
    private val scope: CoroutineScope,
    private val platform: TutorPlatform,
    private val loadTutorFromPdfUseCase: LoadTutorFromPdfUseCase,
    private val getAllHistoryChatUseCase: GetAllHistoryChatUseCase,
    private val userIdProvider: () -> String,
    private val askTutorUseCase: AskTutorUseCase,
    private val getIdTokenSuspendUseCase: GetIdTokenSuspendUseCase,
    private val getChatByIdUseCase: GetChatByIdUseCase,
    private val deleteChatUseCase: DeleteChatUseCase,
    private val analyticsTracker: IAnalyticsTracker,
    private val upsertChatProgressUseCase: UpsertChatProgressUseCase,
    private val saveExerciseAnswerUseCase: SaveExerciseAnswerUseCase,
    private val deleteChatProgressUseCase: DeleteChatProgressUseCase,
    private val markUnitAsReadUseCase: MarkUnitAsReadUseCase,

    ) {
    private val _state = MutableStateFlow(TutorState())
    val state: StateFlow<TutorState> = _state
    private fun userId(): String = userIdProvider()

    fun onEvent(event: TutorUiEvents) {
        when (event) {
            TutorUiEvents.Logout -> logOutUser()
            is TutorUiEvents.SendMessage -> sendMessageToTutor(event.text)
            is TutorUiEvents.OpenExistingChat -> loadExistingChat(event.chatId)
            TutorUiEvents.NewChat -> startNewChat()
            TutorUiEvents.InitializeStateAfterShowingMessage -> initializeStateAfterShowingMessage()
            TutorUiEvents.ClearUiEvent -> clearUiEvent()
            is TutorUiEvents.ShowErrorMessage -> stateForErrorMessage(event.message)
            is TutorUiEvents.DeleteChat -> {
                deleteChat(event.subjectId)
            }

            is TutorUiEvents.SelectLearningUnit -> selectLearningUnitByTitle(
                event.title,
                event.subjectId
            )

            TutorUiEvents.NextUnit -> goNext()
            is TutorUiEvents.ExerciseAnswered -> {
                trackExerciseAnswered(event)
                saveExerciseAnswered(event)
            }

            TutorUiEvents.OpenMyProgress -> openMyProgress()
            is TutorUiEvents.UploadPdf -> uploadPdf(event.file)
            is TutorUiEvents.ChangeTab -> {
                refreshTabInfoMessage(event.tab)
            }

            else -> {}
        }
    }

    private fun track(
        name: String,
        params: Map<String, String> = emptyMap(),
    ) {
        runCatching {
            analyticsTracker.sendEvent(
                name = name,
                params = params + mapOf("userId" to userId())
            )
        }.onFailure {
            platform.log("Analytics", "Failed to log event: $name", it)
        }
    }

    private fun saveExerciseAnswered(event: TutorUiEvents.ExerciseAnswered) {
        val current = _state.value
        val unit = current.currentUnit

        track(
            name = EXERCISE_ANSWERED,
            params = mapOf(
                "chatId" to current.activeChatId,
                "unitIndex" to current.currentIndex.toString(),
                "unitTitle" to unit.title,
                "exerciseIndex" to event.exerciseIndex.toString(),
                "exerciseType" to event.exerciseType.name,
                "selectedAnswer" to event.selectedAnswer,
                "correctAnswer" to event.correctAnswer,
                "isCorrect" to event.isCorrect.toString(),
                "timeToAnswerMs" to event.timeToAnswerMs.toString()
            )
        )

        scope.launch {
            runCatching {
                val token = getIdTokenSuspendUseCase()

                saveExerciseAnswerUseCase(
                    userId = userId(),
                    token = token,
                    answer = ExerciseAnswerProgressDto(
                        chatId = current.activeChatId,
                        userId = userId(),
                        unitIndex = current.currentIndex,
                        unitTitle = unit.title,
                        exerciseIndex = event.exerciseIndex,
                        exerciseType = event.exerciseType.name,
                        question = event.question,
                        selectedAnswer = event.selectedAnswer,
                        correctAnswer = event.correctAnswer,
                        isCorrect = event.isCorrect,
                        timeToAnswerMs = event.timeToAnswerMs,
                        answeredAtEpochMillis = event.timeToAnswerMs
                    )
                )
            }.onFailure {
                platform.log("Progress", "Failed to save exercise answer", it)
            }
        }

    }

//    private fun markCurrentUnitAsRead(index: Int) {
//        _state.update { current ->
//            val updatedUnits = current.allUnits.mapIndexed { i, unit ->
//                if (i == index) unit.copy(isRead = true) else unit
//            }
//
//            current.copy(
//                allUnits = updatedUnits,
//                currentUnit = updatedUnits.getOrNull(index) ?: current.currentUnit
//            )
//        }
//
//        saveProgress()
//    }

    private fun trackExerciseAnswered(event: TutorUiEvents.ExerciseAnswered) {
        val unit = _state.value.currentUnit

        track(
            name = EXERCISE_ANSWERED,
            params = mapOf(
                "chatId" to _state.value.activeChatId,
                "unitIndex" to _state.value.currentIndex.toString(),
                "unitTitle" to unit.title,
                "exerciseIndex" to event.exerciseIndex.toString(),
                "exerciseType" to event.exerciseType.name,
                "selectedAnswer" to event.selectedAnswer,
                "correctAnswer" to event.correctAnswer,
                "isCorrect" to event.isCorrect.toString(),
                "timeToAnswerMs" to event.timeToAnswerMs.toString()
            )
        )
    }

    fun playAIMessage(text: String) {
        platform.speak(normalizeTranscript(text))
    }

    fun startVoiceInput(onResult: (String) -> Unit) = platform.startVoice(onResult)
    fun stopVoiceInput() = platform.stopVoice()

    fun setEventNone() {
        _state.update { it.copy(event = TutorEvents.None) }

    }


    @OptIn(ExperimentalUuidApi::class)
    private fun sendMessageToTutor(text: String) {
        val trimmed = text.trim()
        if (trimmed.isBlank()) return

        val chatId = _state.value.activeChatId
        val unitIndex = _state.value.currentIndex

        val optimisticUserMessage = ChatMessageDomain(
            id = "local_${Uuid.random().toString()}",
            sender = SenderType.USER.name,
            content = trimmed,
            imagePath = emptyList()
        )

        track(
            name = MESSAGE_SENT,
            params = mapOf(
                "chatId" to chatId,
                "unitIndex" to unitIndex.toString(),
                "tab" to state.value.selectedTab.name,
                "messageLength" to trimmed.length.toString(),
                "userId" to userId()
            )
        )

        _state.update { current ->
            current.copy(
                loading = true,
                askTutor = true,
                allChatMessages = current.allChatMessages + optimisticUserMessage,
                session = current.session.copy(
                    chat = current.session.chat.copy(
                        messages = current.session.chat.messages + optimisticUserMessage
                    )
                )
            )
        }

        scope.launch {
            try {
                val token = getIdTokenSuspendUseCase()

                val response = askTutorUseCase(
                    chatId = chatId,
                    userId = userId(),
                    unitIndex = unitIndex,
                    message = generic_error,
                    token = token,
                    chatMode = state.value.selectedTab.name
                )

                val aiMsg = response.aiMessage?.toDomain()

                track(
                    name = AI_REPLY_RECEIVED,
                    params = mapOf(
                        "chatId" to chatId,
                        "unitIndex" to unitIndex.toString(),
                        "tab" to state.value.selectedTab.name,
                        "hasAiMessage" to (aiMsg != null).toString(),
                        "userId" to userId()
                    )
                )

                _state.update { current ->
                    current.copy(
                        loading = false,
                        askTutor = false,
                        allChatMessages = if (aiMsg != null) {
                            current.allChatMessages + aiMsg
                        } else {
                            current.allChatMessages
                        },
                        session = current.session.copy(
                            chat = current.session.chat.copy(
                                messages = if (aiMsg != null) {
                                    current.session.chat.messages + aiMsg
                                } else {
                                    current.session.chat.messages
                                }
                            )
                        )
                    )
                }
                saveProgress()
            } catch (t: Throwable) {
                track(
                    name = MESSAGE_SEND_FAILED,
                    params = mapOf(
                        "chatId" to chatId,
                        "reason" to (t.message ?: "unknown"),
                        "userId" to userId()
                    )
                )

                val msg = when {
                    t.message?.contains("timeout", ignoreCase = true) == true ->
                        generic_error

                    else ->
                        "Failed to send message"
                }

                _state.update {
                    it.copy(
                        loading = false,
                        showCustomMessage = true,
                        isError = true,
                        askTutor = false,
                        message = msg
                    )
                }
            }
        }
    }

    private fun selectLearningUnitByTitle(title: String, subjectId: String) {
        if (_state.value.activeChatId != subjectId) {
            loadExistingChatAtUnit(
                chatId = subjectId,
                unitTitle = title
            )
            return
        }

        selectCurrentChatUnitByTitle(title)
    }

    private fun selectCurrentChatUnitByTitle(title: String) {
        val units = _state.value.allUnits
        if (units.isEmpty()) return

        val index = units.indexOfFirst { it.title.trim() == title.trim() }
        if (index == -1) return

        val selected = units[index]

        val newSession = buildChatSessionForSelectedUnit(
            fileName = _state.value.session.material.fileName,
            unit = selected,
            allStoredMessages = _state.value.allChatMessages
        )

        _state.update {
            it.copy(
                currentIndex = index,
                currentUnit = selected,
                session = newSession,
                selectedTab = TutorTab.Learning,
                tabInfoMessage = buildLearningTabMessage(selected),
                canGoNext = index < units.lastIndex
            )
        }
        markUnitAsRead(index)
        saveProgress()
    }

    private fun loadExistingChatAtUnit(chatId: String, unitTitle: String) {
        scope.launch {
            try {
                _state.update { it.copy(loading = true) }

                val fullChat = getChatByIdUseCase(chatId, userId())

                val units = fullChat.result?.learningUnits
                    ?: fullChat.partialResult?.learningUnits
                    ?: emptyList()

                val domainUnits = units.map { it.toDomain() }

                val index = domainUnits.indexOfFirst {
                    it.title.trim() == unitTitle.trim()
                }.takeIf { it != -1 } ?: 0

                val selected = domainUnits.getOrNull(index) ?: LearningUnitContentDomain()

                val fullMessages = fullChat.messages
                    .sortedBy { it.createdAtEpochMillis }
                    .map { it.toDomain() }

                val session = buildChatSessionForSelectedUnit(
                    fileName = fullChat.title,
                    unit = selected,
                    allStoredMessages = fullMessages
                )

                _state.update {
                    it.copy(
                        loading = false,
                        activeChatId = fullChat.chatId,
                        activeDocumentId = fullChat.documentId,
                        subjectTitle = fullChat.title,
                        allUnits = domainUnits,
                        currentIndex = index,
                        currentUnit = selected,
                        session = session,
                        allChatMessages = fullMessages,
                        selectedTab = TutorTab.Learning,
                        tabInfoMessage = buildLearningTabMessage(selected),
                        canGoNext = index < domainUnits.lastIndex
                    )
                }
                markUnitAsRead(index)
                saveProgress()

            } catch (t: Throwable) {
                _state.update {
                    it.copy(
                        loading = false,
                        showCustomMessage = true,
                        isError = true,
                        message = "Failed to load chat unit"
                    )
                }
            }
        }
    }

    private fun refreshTabInfoMessage(tab: TutorTab) {
        val unit = _state.value.currentUnit

        track(
            name = TAB_CHANGED,
            params = mapOf(
                "chatId" to _state.value.activeChatId,
                "tab" to tab.name,
                "unitTitle" to unit.title,
                "userId" to userId()
            )
        )

        val msg = when (tab) {
            TutorTab.Learning -> buildLearningTabMessage(unit)
            TutorTab.Exercises -> buildExercisesTabMessage(unit)
        }
        _state.update {
            it.copy(
                tabInfoMessage = msg,
                selectedTab = tab
            )
        }
        saveProgress()
    }

    private fun markUnitAsRead(index: Int) {
        val current = _state.value
        val documentId = current.activeDocumentId
        if (documentId.isBlank()) return

        val unit = current.allUnits.getOrNull(index) ?: return
        if (unit.isRead) return

        val unitTitle = unit.title
        val activeChatId = current.activeChatId

        _state.update { state ->
            val updatedUnits = state.allUnits.mapIndexed { i, item ->
                if (i == index) item.copy(isRead = true) else item
            }

            val updatedHistory = state.chatHistory.copy(
                subjects = state.chatHistory.subjects.map { subject ->
                    if (subject.subjectId != activeChatId) subject
                    else subject.copy(
                        learningUnits = subject.learningUnits.map { lu ->
                            if (lu.title.trim() == unitTitle.trim()) {
                                lu.copy(isRead = true)
                            } else {
                                lu
                            }
                        }
                    )
                }
            )

            state.copy(
                allUnits = updatedUnits,
                currentUnit = updatedUnits.getOrNull(index) ?: state.currentUnit,
                chatHistory = updatedHistory
            )
        }

        scope.launch {
            runCatching {
                val token = getIdTokenSuspendUseCase()
                markUnitAsReadUseCase(
                    documentId = documentId,
                    unitIndex = index,
                    token = token
                )
            }.onFailure {
                platform.log("Progress", "Failed to mark unit as read", it)
            }
        }
    }
    private fun uploadPdf(picked: PickedFile) {
        scope.launch {
            try {
                _state.update { it.copy(loading = true) }

                val res = loadTutorFromPdfUseCase.loadTemplateForPickedPdf(
                    picked = picked,
                    userId = userId(),
                    mode = state.value.selectedTab.name
                )

                val units = res.prototype.learningUnits
                val subjectTitle = res.prototype.title

                val session = buildChatSessionStart(
                    fileName = subjectTitle,
                    units = units,
                    activeIndex = 0
                )

                val refreshedChats = getAllHistoryChatUseCase(userId())

                track(
                    name = PDF_UPLOADED,
                    params = mapOf(
                        "chatId" to res.chatId,
                        "documentTitle" to subjectTitle,
                        "unitsCount" to units.size.toString(),
                        "language" to res.languageEnum.value,
                        "mode" to state.value.selectedTab.name,
                        "userId" to userId()
                    )
                )

                _state.update {
                    it.copy(
                        loading = false,
                        activeChatId = res.chatId,
                        subjectTitle = subjectTitle,
                        allUnits = units,
                        currentIndex = 0,
                        unlockedCount = 1,
                        currentUnit = units.firstOrNull() ?: LearningUnitContentDomain(),
                        session = session,
                        savedChats = refreshedChats,
                        chatHistory = refreshedChats.toChatHistoryDomain(),
                        showCustomMessage = true,
                        message = "Chat loaded",
                        isError = false,
                        tabInfoMessage = buildLearningTabMessage(
                            units.firstOrNull() ?: LearningUnitContentDomain()
                        ),
                        canGoNext = units.size > 1
                    )
                }

                saveProgress()

            } catch (t: Throwable) {
                track(
                    name = PDF_UPLOAD_FAILED,
                    params = mapOf(
                        "reason" to (t.toUserMessage()),
                        "userId" to userId()
                    )
                )

                _state.update {
                    it.copy(
                        loading = false,
                        showCustomMessage = true,
                        isError = true,
                        message = "Upload failed"
                    )
                }
            }
        }
    }


    private fun Throwable.toUserMessage(): String {
        return when {
            this.message?.contains("10.0.2.2", ignoreCase = true) == true ->
                "Cannot connect to local server. Make sure backend is running and emulator can reach it."

            this.message?.contains("timeout", ignoreCase = true) == true ->
                "Request timed out. Please try again."

            this.cause?.message?.contains("timeout", ignoreCase = true) == true ->
                "Connection timed out while uploading PDF."

            else -> this.message ?: "Upload failed"
        }
    }

    private fun deleteChat(chatId: String) {
        scope.launch {
            try {
                _state.update { it.copy(loading = true) }

                val token = getIdTokenSuspendUseCase()
                deleteChatUseCase(
                    chatId = chatId,
                    userId = userId(),
                    token = token
                )
                deleteChatProgressUseCase(
                    chatId = chatId,
                    userId = userId(),
                    token = token
                )
                val remainingChats = _state.value.savedChats.filterNot { it.chatId == chatId }

                val deletedWasActive = _state.value.activeChatId == chatId

                track(
                    name = CHAT_DELETED,
                    params = mapOf(
                        "chatId" to chatId,
                        "userId" to userId()
                    )
                )

                _state.update { current ->
                    current.copy(
                        loading = false,
                        savedChats = remainingChats,
                        chatHistory = remainingChats.toChatHistoryDomain(),
                        showCustomMessage = true,
                        isError = false,
                        message = "The chat was deleted",
                        activeChatId = if (deletedWasActive) "" else current.activeChatId,
                        activeDocumentId = if (deletedWasActive) "" else current.activeDocumentId,
                        subjectTitle = if (deletedWasActive) "" else current.subjectTitle,
                        allUnits = if (deletedWasActive) emptyList() else current.allUnits,
                        currentIndex = if (deletedWasActive) 0 else current.currentIndex,
                        unlockedCount = if (deletedWasActive) 0 else current.unlockedCount,
                        currentUnit = if (deletedWasActive) LearningUnitContentDomain() else current.currentUnit,
                        allChatMessages = if (deletedWasActive) emptyList() else current.allChatMessages,
                        session = if (deletedWasActive) buildEmptyChatSessionStart() else current.session,
                    )
                }

                if (deletedWasActive && remainingChats.isNotEmpty()) {
                    loadExistingChat(remainingChats.first().chatId)
                }
            } catch (t: Throwable) {
                track(
                    name = CHAT_DELETE_FAILED,
                    params = mapOf(
                        "chatId" to chatId,
                        "reason" to (t.message ?: "unknown"),
                        "userId" to userId()
                    )
                )

                _state.update {
                    it.copy(
                        loading = false,
                        showCustomMessage = true,
                        isError = true,
                        message = "Failed to delete chat"
                    )
                }
            }
        }
    }

    private fun openMyProgress() {
        _state.update { it.copy(event = TutorEvents.NavigateToMyProgress) }

        track(
            name = MY_PROGRESS_OPENED,
            params = mapOf(
                "userId" to userId()
            )
        )
    }

    private fun goNext() {
        val units = _state.value.allUnits
        val subjectTitle = _state.value.subjectTitle

        val nextIndex = (_state.value.currentIndex + 1).coerceAtMost(units.lastIndex)
        if (nextIndex == _state.value.currentIndex) return

        val newUnlocked = maxOf(_state.value.unlockedCount, nextIndex + 1)

        track(
            name = NEXT_UNIT_CLICKED,
            params = mapOf(
                "chatId" to _state.value.activeChatId,
                "fromIndex" to _state.value.currentIndex.toString(),
                "toIndex" to nextIndex.toString(),
                "subjectTitle" to subjectTitle,
                "userId" to userId()
            )
        )

        val selectedUnit = units.getOrNull(nextIndex) ?: LearningUnitContentDomain()

        val session = buildChatSessionForSelectedUnit(
            fileName = _state.value.session.material.fileName,
            unit = selectedUnit,
            allStoredMessages = _state.value.allChatMessages
        )

        val history = buildChatHistoryFromUnits(
            units = units,
            subjectTitle = subjectTitle
        )

        _state.update {
            it.copy(
                currentIndex = nextIndex,
                unlockedCount = newUnlocked,
                currentUnit = units.getOrNull(nextIndex) ?: LearningUnitContentDomain(),
                session = session,
                chatHistory = history,
                selectedTab = TutorTab.Learning,
                canGoNext = nextIndex < units.lastIndex
            )
        }
        markUnitAsRead(nextIndex)
        saveProgress()
    }

    private fun saveProgress() {
        scope.launch {
            runCatching {
                val token = getIdTokenSuspendUseCase()

                val progress = buildCurrentProgress()

                upsertChatProgressUseCase(
                    userId = userId(),
                    token = token,
                    progress = progress.toDto()
                )
            }.onFailure {
                platform.log("Progress", "Failed to save progress", it)
            }
        }
    }

    private fun buildCurrentProgress(): ChatProgressUpsertDomain {
        val totalUnits = _state.value.allUnits.size
        val currentIndex = _state.value.currentIndex
        val progressPercent =
            if (totalUnits > 0) ((currentIndex + 1) * 100) / totalUnits else 0

        return ChatProgressUpsertDomain(
            chatId = _state.value.activeChatId,
            userId = userId(),
            documentId = _state.value.activeDocumentId,
            title = _state.value.subjectTitle,
            totalUnits = totalUnits,
            currentUnitIndex = currentIndex,
            selectedTab = _state.value.selectedTab.name,
            messagesSentCount = _state.value.allChatMessages.count { it.sender == SenderType.USER.name },
            availableExercisesCount = _state.value.allUnits.sumOf { it.exercises.size },
            lastActivityEpochMillis = Clock.System.now().toEpochMilliseconds(),
            progressPercent = progressPercent
        )
    }

    private fun startNewChat() {
        track(
            name = NEW_CHAT_STARTED,
            params = mapOf(
                "userId" to userId()
            )
        )

        _state.update {
            it.copy(
                session = buildEmptyChatSessionStart(),
                currentUnit = LearningUnitContentDomain(),
                event = TutorEvents.None
            )
        }
    }

    fun initializeData() = scope.launch {
        val uid = userId()

        if (_state.value.session.material.fileUploaded) {
            return@launch
        }

        if (uid.isBlank()) {
            platform.log("TutorStore", "UserId not ready yet", null)
            return@launch
        }

        try {
            _state.update { it.copy(loading = true, error = null) }

            val chats = getAllHistoryChatUseCase(uid)
            if (chats.isEmpty()) {
                loadGreekPdf()
            }
            track(
                name = TUTOR_SCREEN_OPENED,
                params = mapOf(
                    "savedChatsCount" to chats.size.toString(),
                    "userId" to uid
                )
            )

            if (chats.isNotEmpty()) {
                val latestChatSummary = chats.first()

                val latestChat = getChatByIdUseCase(
                    chatId = latestChatSummary.chatId,
                    userId = uid
                )

                track(
                    name = LATEST_CHAT_AUTO_LOADED,
                    params = mapOf(
                        "chatId" to latestChat.chatId,
                        "documentId" to latestChat.documentId,
                        "userId" to uid
                    )
                )

                val units = latestChat.result?.learningUnits
                    ?: latestChat.partialResult?.learningUnits
                    ?: emptyList()

                val domainUnits = units.map { it.toDomain() }

                val activeIndex = 0

                val activeUnit = domainUnits.firstOrNull()
                    ?: LearningUnitContentDomain()

                val session = buildChatSessionFromStoredChat(
                    chat = latestChat,
                    units = domainUnits,
                    activeIndex = activeIndex
                )

                val fullMessages = latestChat.messages
                    .sortedBy { it.createdAtEpochMillis }
                    .map { it.toDomain() }

                _state.update {
                    it.copy(
                        loading = false,
                        savedChats = chats,
                        chatHistory = chats.toChatHistoryDomain(),

                        activeChatId = latestChat.chatId,
                        activeDocumentId = latestChat.documentId,
                        subjectTitle = latestChat.title,

                        allUnits = domainUnits,

                        currentIndex = activeIndex,
                        unlockedCount = maxOf(1, domainUnits.size),
                        currentUnit = activeUnit,

                        session = session,

                        allChatMessages = fullMessages,

                        selectedTab = TutorTab.Learning,
                        tabInfoMessage = buildLearningTabMessage(activeUnit),

                        canGoNext = activeIndex < domainUnits.lastIndex,

                        event = TutorEvents.Loaded
                    )
                }

                return@launch
            }

            return@launch

        } catch (t: Throwable) {
            platform.log("TutorStore", "initializeData failed", t)

            _state.update {
                it.copy(
                    loading = false,
                    message = generic_error,
                    showCustomMessage = true,
                    isError = true
                )
            }
        }
    }

    private suspend fun loadGreekPdf() {
        // todo remove this logic
        try {
            _state.update { it.copy(loading = true) }

            val res = loadTutorFromPdfUseCase.loadGreekTemplate(
                userId = userId(),
                mode = state.value.selectedTab.name
            )

            val units = res.prototype.learningUnits
            val subjectTitle = res.prototype.title

            val session = buildChatSessionStart(
                fileName = subjectTitle,
                units = units,
                activeIndex = 0
            )

            val refreshedChats = getAllHistoryChatUseCase(userId())

            track(
                name = PDF_UPLOADED,
                params = mapOf(
                    "chatId" to res.chatId,
                    "documentTitle" to subjectTitle,
                    "unitsCount" to units.size.toString(),
                    "language" to res.languageEnum.value,
                    "mode" to state.value.selectedTab.name,
                    "userId" to userId()
                )
            )

            _state.update {
                it.copy(
                    loading = false,
                    activeChatId = res.chatId,
                    subjectTitle = subjectTitle,
                    allUnits = units,
                    currentIndex = 0,
                    unlockedCount = 1,
                    currentUnit = units.firstOrNull() ?: LearningUnitContentDomain(),
                    session = session,
                    savedChats = refreshedChats,
                    chatHistory = refreshedChats.toChatHistoryDomain(),
                    showCustomMessage = true,
                    message = "Chat loaded",
                    isError = false,
                    tabInfoMessage = buildLearningTabMessage(
                        units.firstOrNull() ?: LearningUnitContentDomain()
                    ),
                    canGoNext = units.size > 1
                )
            }

            saveProgress()

        } catch (t: Throwable) {
            track(
                name = PDF_UPLOAD_FAILED,
                params = mapOf(
                    "reason" to (t.toUserMessage()),
                    "userId" to userId()
                )
            )

            _state.update {
                it.copy(
                    loading = false,
                    showCustomMessage = true,
                    isError = true,
                    message = "Upload failed"
                )
            }
        }
    }

    private fun loadExistingChat(chatId: String) {
        scope.launch {
            try {
                _state.update { it.copy(loading = true) }

                track(
                    name = EXISTING_CHAT_OPENED,
                    params = mapOf(
                        "chatId" to chatId,
                        "userId" to userId()
                    )
                )

                val fullChat = getChatByIdUseCase(chatId, userId())

                val units = fullChat.result?.learningUnits
                    ?: fullChat.partialResult?.learningUnits
                    ?: emptyList()

                val domainUnits = units.map { it.toDomain() }
                val firstUnit = domainUnits.firstOrNull() ?: LearningUnitContentDomain()

                val session = buildChatSessionFromStoredChat(
                    chat = fullChat,
                    units = domainUnits,
                    activeIndex = 0
                )

                val fullMessages = fullChat.messages
                    .sortedBy { it.createdAtEpochMillis }
                    .map { it.toDomain() }

                _state.update {
                    it.copy(
                        loading = false,
                        activeChatId = fullChat.chatId,
                        activeDocumentId = fullChat.documentId,
                        subjectTitle = fullChat.title,
                        allUnits = domainUnits,
                        currentIndex = 0,
                        currentUnit = firstUnit,
                        session = session,
                        allChatMessages = fullMessages,
                        selectedTab = if (fullChat.mode.equals("Exercises", true)) {
                            TutorTab.Exercises
                        } else {
                            TutorTab.Learning
                        },
                        tabInfoMessage = buildLearningTabMessage(firstUnit),
                        canGoNext = domainUnits.size > 1
                    )
                }

            } catch (t: Throwable) {

                track(
                    name = EXISTING_CHAT_OPEN_FAILED,
                    params = mapOf(
                        "chatId" to chatId,
                        "reason" to (t.message ?: "unknown"),
                        "userId" to userId()
                    )
                )

                _state.update {
                    it.copy(
                        loading = false,
                        showCustomMessage = true,
                        isError = true,
                        message = "Failed to load chat"
                    )
                }
            }
        }
    }

    private fun logOutUser() = scope.launch {
        logoutUseCase { success, _ ->
            if (success) {
                _state.update { it.copy(event = TutorEvents.NavigateToLogOut) }
            } else {
                stateForErrorMessage()
            }
        }
    }

    private fun initializeStateAfterShowingMessage() {
        _state.update {
            it.copy(
                showCustomMessage = false,
                message = EmptyValues.EMPTY_STRING,
                isError = false
            )
        }
    }

    private fun clearUiEvent() {
        _state.update { it.copy(uiEvent = TutorUiEvents.None, event = TutorEvents.None) }
    }

    private fun stateForErrorMessage(message: String? = null) {
        _state.update {
            it.copy(
                showCustomMessage = true,
                message = message ?: SmartTutorStrings.generic_error,
                isError = true
            )
        }
    }

    private fun normalizeTranscript(text: String): String =
        text.replace("-", "").replace(Regex("\\s+"), " ").trim()

}
