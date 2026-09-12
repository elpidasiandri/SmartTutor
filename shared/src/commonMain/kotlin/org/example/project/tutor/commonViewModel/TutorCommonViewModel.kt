package org.example.project.tutor.commonViewModel

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import org.example.project.myProgress.mappers.toDto
import org.example.project.myProgress.models.UnitReadProgressDomain
import org.example.project.myProgress.networkModel.ExerciseAnswerProgressDto
import org.example.project.myProgress.useCases.DeleteChatProgressUseCase
import org.example.project.myProgress.useCases.GetExerciseStatsUseCase
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
import org.example.project.tutor.models.AnalyticsEvents.PDF_UPLOADED
import org.example.project.tutor.models.AnalyticsEvents.PDF_UPLOAD_FAILED
import org.example.project.tutor.models.AnalyticsEvents.TAB_CHANGED
import org.example.project.tutor.models.AnalyticsEvents.TUTOR_SCREEN_OPENED
import org.example.project.tutor.models.domainModels.learningUnits.ExerciseType
import org.example.project.tutor.models.domainModels.learningUnits.LearningUnitExerciseStatsDomain
import org.example.project.tutor.models.domainModels.myProgress.ChatProgressUpsertDomain
import org.example.project.tutor.networkModel.uploadPdf.ExerciseAttemptDto
import org.example.project.tutor.repo.analytics.IAnalyticsTracker
import org.example.project.tutor.state.TutorEvents
import org.example.project.tutor.state.TutorState
import org.example.project.tutor.state.TutorUiEvents
import org.example.project.tutor.useCases.AddMoreExercisesUseCase
import org.example.project.tutor.useCases.AskTutorUseCase
import org.example.project.tutor.useCases.DeleteChatUseCase
import org.example.project.tutor.useCases.GetAllHistoryChatUseCase
import org.example.project.tutor.useCases.GetChatByIdUseCase
import org.example.project.tutor.useCases.LoadInitialExercisesUseCase
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
    private val getExerciseStateUseCase: GetExerciseStatsUseCase,
    private val addMoreExercisesUseCase: AddMoreExercisesUseCase,
    private val loadInitialExercisesUseCase: LoadInitialExercisesUseCase,
) {
    private val _state = MutableStateFlow(TutorState())
    val state: StateFlow<TutorState> = _state
    private val initialExercisesRequestsInFlight = mutableSetOf<String>()
    private fun userId(): String = userIdProvider()

    fun onEvent(event: TutorUiEvents) {
        when (event) {
            TutorUiEvents.Logout -> logOutUser()
            is TutorUiEvents.SendMessage -> sendMessageToTutor(event.text, true)
            is TutorUiEvents.OpenExistingChat -> loadExistingChat(event.chatId)
            TutorUiEvents.NewChat -> startNewChat()
            TutorUiEvents.InitializeStateAfterShowingMessage -> initializeStateAfterShowingMessage()
            TutorUiEvents.ClearUiEvent -> clearUiEvent()
            is TutorUiEvents.ShowErrorMessage -> stateForErrorMessage(event.message)
            is TutorUiEvents.DeleteChat -> {
                deleteChat(event.subjectId)
            }

            is TutorUiEvents.AskTutorForOpenExercise -> {
                askTutorForOpenExercise(event)
            }

            is TutorUiEvents.SelectLearningUnit -> selectLearningUnitByTitle(
                event.title,
                event.subjectId
            )

            TutorUiEvents.LoadMoreExercises -> loadMoreExercises()
            TutorUiEvents.NextUnit -> goNext()
            is TutorUiEvents.ExerciseAnswered -> {
                trackExerciseAnswered(event)
                saveExerciseAnswered(event)
            }

            TutorUiEvents.OpenMyProgress -> openMyProgress()
            is TutorUiEvents.UploadPdf -> uploadPdf(event.file)
            is TutorUiEvents.ChangeTab -> {
                changeTab(event.tab)
            }

            is TutorUiEvents.OpenUnitSection -> {
                openUnitSection(
                    title = event.title,
                    subjectId = event.subjectId,
                    tab = event.tab,
                    exerciseIndex = event.exerciseIndex
                )
            }

            else -> {}
        }
    }

    private fun openUnitSection(
        title: String,
        subjectId: String,
        tab: TutorTab,
        exerciseIndex: Int? = null,
    ) {
        if (_state.value.activeChatId != subjectId) {
            loadExistingChatAtUnitSection(
                chatId = subjectId,
                unitTitle = title,
                tab = tab,
                exerciseIndex = exerciseIndex
            )
            return
        }

        val units = _state.value.allUnits
        val index = units.indexOfFirst {
            it.title.trim().equals(title.trim(), ignoreCase = true)
        }
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
                selectedTab = tab,
                selectedExerciseIndex = exerciseIndex ?: 0,
                tabInfoMessage = if (tab == TutorTab.Learning) {
                    buildLearningTabMessage(selected)
                } else {
                    buildExercisesTabMessage(selected, exerciseIndex ?: 0)
                },
                canGoNext = index < units.lastIndex
            )
        }

        markUnitAsRead(index)
        saveProgress()
        if (tab == TutorTab.Exercises) {
            if (selected.exercises.isEmpty()) {
                loadInitialExercisesForCurrentUnit(
                    openExercisesTab = true
                )
            }
        }
    }

    private fun loadMoreExercises() {
        val current = _state.value
        val chatId = current.activeChatId
        val unitIndex = current.currentIndex

        scope.launch {
            runCatching {
                _state.update { it.copy(askTutor = true) }

                val token = getIdTokenSuspendUseCase()

                val response = addMoreExercisesUseCase(
                    chatId = chatId,
                    userId = userId(),
                    unitIndex = unitIndex,
                    token = token
                )

                val extra = response.exercises.map { it.toDomain() }

                _state.update { state ->
                    val updatedUnit = state.currentUnit.copy(
                        exercises = state.currentUnit.exercises + extra
                    )

                    state.copy(
                        askTutor = false,
                        currentUnit = updatedUnit,
                        allUnits = state.allUnits.mapIndexed { i, unit ->
                            if (i == unitIndex) updatedUnit else unit
                        },
                        session = buildChatSessionForSelectedUnit(
                            fileName = state.session.material.fileName,
                            unit = updatedUnit,
                            allStoredMessages = state.allChatMessages
                        ),
                        chatHistory = state.chatHistory.copy(
                            subjects = state.chatHistory.subjects.map { subject ->
                                if (subject.subjectId != chatId) {
                                    subject
                                } else {
                                    subject.copy(
                                        learningUnits = subject.learningUnits.mapIndexed { i, menuUnit ->
                                            if (i == unitIndex) {
                                                menuUnit.copy(
                                                    exercises = menuUnit.exercises + extra
                                                )
                                            } else {
                                                menuUnit
                                            }
                                        }
                                    )
                                }
                            })
                    )
                }
                saveProgress()
            }.onFailure {
                _state.update {
                    it.copy(
                        askTutor = false,
                        showCustomMessage = true,
                        isError = true,
                        message = "Failed to load more exercises"
                    )
                }
            }
        }
    }

    private fun changeTab(tab: TutorTab) {
        val current = _state.value
        val unit = current.currentUnit

        track(
            name = TAB_CHANGED,
            params = mapOf(
                "chatId" to current.activeChatId,
                "tab" to tab.name,
                "unitTitle" to unit.title,
                "userId" to userId()
            )
        )

        if (tab == TutorTab.Exercises && unit.exercises.isEmpty()) {
            loadInitialExercisesForCurrentUnit(openExercisesTab = true)
            return
        }
        val msg = when (tab) {
            TutorTab.Learning ->
                buildLearningTabMessage(unit)

            TutorTab.Exercises ->
                buildExercisesTabMessage(unit)
        }

        _state.update {
            it.copy(
                selectedTab = tab,
                tabInfoMessage = msg
            )
        }

        saveProgress()
    }

    private fun loadInitialExercisesForCurrentUnit(
        openExercisesTab: Boolean = false,
    ) {
        val current = _state.value

        val documentId = current.activeDocumentId
        val chatId = current.activeChatId
        val unit = current.currentUnit
        val unitNumber = unit.numbering

        if (documentId.isBlank()) {
            return
        }

        if (chatId.isBlank()) {
            return
        }

        if (unitNumber <= 0) {
            return
        }

        /*
         * Αν ο user πάτησε το Exercises tab,
         * το ανοίγουμε αμέσως.
         *
         * Αυτό γίνεται ακόμα κι αν το request
         * για τις ασκήσεις τρέχει ήδη.
         */
        if (openExercisesTab) {
            _state.update {
                it.copy(
                    selectedTab = TutorTab.Exercises,
                    selectedExerciseIndex = 0,
                    tabInfoMessage = buildExercisesTabMessage(
                        it.currentUnit
                    )
                )
            }
        }

        /*
         * Οι ασκήσεις υπάρχουν ήδη.
         * Δεν χρειάζεται κανένα API call.
         */
        if (unit.exercises.isNotEmpty()) {
            return
        }

        /*
         * Μοναδικό key για αυτό το document + unit.
         */
        val requestKey = "$documentId:$unitNumber"

        /*
         * Αν υπάρχει ήδη request για αυτό ακριβώς το unit,
         * σταματάμε εδώ.
         *
         * Άρα:
         * background prefetch -> user clicks Exercises
         * ΔΕΝ δημιουργεί δεύτερο request.
         */
        if (!initialExercisesRequestsInFlight.add(requestKey)) {
            return
        }

        _state.update {
            it.copy(
                exercisesLoading = true
            )
        }

        scope.launch {
            try {
                val token = getIdTokenSuspendUseCase()

                val response = loadInitialExercisesUseCase(
                    documentId = documentId,
                    unitNumber = unitNumber,
                    token = token
                )

                val exercises = response.exercises.map {
                    it.toDomain()
                }

                if (exercises.isEmpty()) {
                    error("No exercises were generated")
                }

                _state.update { state ->
                    if (state.activeChatId != chatId || state.activeDocumentId != documentId) {
                        return@update state
                    }

                    val targetUnit = state.allUnits.firstOrNull {
                        it.numbering == unitNumber
                    } ?: return@update state

                    val updatedUnit = targetUnit.copy(exercises = exercises)

                    val updatedUnits = state.allUnits.map { oldUnit ->
                        if (oldUnit.numbering == unitNumber) {
                            updatedUnit
                        } else {
                            oldUnit
                        }
                    }

                    val isStillCurrentUnit = state.currentUnit.numbering == unitNumber

                    val updatedChatHistory =
                        state.chatHistory.copy(
                            subjects = state.chatHistory.subjects.map { subject ->
                                if (subject.subjectId != chatId) {
                                    subject
                                } else {
                                    subject.copy(
                                        learningUnits =
                                            subject.learningUnits.map { menuUnit ->

                                                if (
                                                    menuUnit.numbering ==
                                                    unitNumber
                                                ) {
                                                    menuUnit.copy(
                                                        exercises = exercises
                                                    )
                                                } else {
                                                    menuUnit
                                                }
                                            }
                                    )
                                }
                            }
                        )

                    val resolvedCurrentUnit =
                        if (isStillCurrentUnit) {
                            updatedUnit
                        } else {
                            state.currentUnit
                        }

                    state.copy(
                        allUnits = updatedUnits,

                        currentUnit = resolvedCurrentUnit,

                        chatHistory = updatedChatHistory,

                        tabInfoMessage =
                            if (
                                isStillCurrentUnit &&
                                state.selectedTab == TutorTab.Exercises
                            ) {
                                buildExercisesTabMessage(
                                    updatedUnit,
                                    state.selectedExerciseIndex
                                )
                            } else {
                                state.tabInfoMessage
                            },

                        session =
                            if (isStillCurrentUnit) {
                                buildChatSessionForSelectedUnit(
                                    fileName =
                                        state.session.material.fileName,
                                    unit = updatedUnit,
                                    allStoredMessages =
                                        state.allChatMessages
                                )
                            } else {
                                state.session
                            }
                    )
                }

                saveProgress()

            } catch (throwable: Throwable) {

                platform.log(
                    tag = "InitialExercises",
                    message = "Failed to load exercises",
                    t = throwable
                )

                _state.update {
                    it.copy(
                        showCustomMessage = true,
                        isError = true,
                        message =
                            throwable.message
                                ?: "Failed to load exercises"
                    )
                }

            } finally {

                initialExercisesRequestsInFlight.remove(
                    requestKey
                )

                _state.update {
                    it.copy(
                        exercisesLoading =
                            initialExercisesRequestsInFlight.isNotEmpty()
                    )
                }
            }
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

    private fun exerciseKey(chatId: String, unitIndex: Int, exerciseIndex: Int): String {
        return "$chatId-$unitIndex-$exerciseIndex"
    }

    private fun loadAnsweredExercises() {
        scope.launch {
            runCatching {
                val currentChatId = _state.value.activeChatId

                if (currentChatId.isBlank()) {
                    _state.update {
                        it.copy(
                            answeredExerciseKeys = emptySet()
                        )
                    }
                    return@runCatching
                }

                val stats = getExerciseStateUseCase(
                    userId = userId(),
                    chatId = currentChatId,
                )

                val answeredKeys = stats.flatMap { unit ->
                    unit.questions.map { question ->
                        exerciseKey(
                            chatId = currentChatId,
                            unitIndex = unit.unitIndex,
                            exerciseIndex = question.exerciseIndex,
                        )
                    }
                }.toSet()

                _state.update {
                    it.copy(
                        answeredExerciseKeys = answeredKeys
                    )
                }
            }.onFailure {
                platform.log(
                    "Progress",
                    "Failed to load answered exercises",
                    it
                )
            }
        }
    }

    private fun saveExerciseAnswered(event: TutorUiEvents.ExerciseAnswered) {
        val current = _state.value
        val unit = current.currentUnit
        val exercise = unit.exercises.getOrNull(event.exerciseIndex) ?: return
        val maxPoints = exercise.maxPoints

        val answeredAt = Clock.System.now().toEpochMilliseconds()

        val awardedPoints = if (event.isCorrect) maxPoints else 0.0

        val gradingStatus = if (event.isCorrect) {
            "correct"
        } else {
            "wrong"
        }

        val key = exerciseKey(
            chatId = current.activeChatId,
            unitIndex = current.currentIndex,
            exerciseIndex = event.exerciseIndex,
        )
        val domainAttempt = LearningUnitExerciseStatsDomain(
            attemptNumber = exercise.attempts.size + 1,
            awardedPoints = awardedPoints,
            gradingStatus = gradingStatus,
            isCorrect = event.isCorrect,
            selectedAnswer = event.selectedAnswer,
            answeredAtEpochMillis = answeredAt
        )

        val dtoAttempt = ExerciseAttemptDto(
            attemptNumber = 0,// server will replace
            awardedPoints = awardedPoints,
            gradingStatus = gradingStatus,
            isCorrect = event.isCorrect,
            selectedAnswer = event.selectedAnswer,
            answeredAtEpochMillis = answeredAt
        )


        val updatedExercise = exercise.copy(
            attempts = exercise.attempts + domainAttempt
        )

        val updatedUnit = unit.copy(
            isRead = true,
            exercises = unit.exercises.mapIndexed { index, oldExercise ->
                if (index == event.exerciseIndex) {
                    updatedExercise
                } else {
                    oldExercise
                }
            }
        )

        _state.update { state ->
            state.copy(
                currentUnit = updatedUnit,

                allUnits = state.allUnits.mapIndexed { index, oldUnit ->
                    if (index == current.currentIndex) {
                        updatedUnit
                    } else {
                        oldUnit
                    }
                },

                chatHistory = state.chatHistory.copy(
                    subjects = state.chatHistory.subjects.map { subject ->
                        if (subject.subjectId != current.activeChatId) {
                            subject
                        } else {
                            subject.copy(
                                learningUnits = subject.learningUnits.mapIndexed {
                                        unitIndex,
                                        learningUnit,
                                    ->
                                    if (unitIndex == current.currentIndex) {
                                        learningUnit.copy(
                                            isRead = true
                                        )
                                    } else {
                                        learningUnit
                                    }
                                }
                            )
                        }
                    }
                ),

                answeredExerciseKeys =
                    state.answeredExerciseKeys + key
            )
        }
        markUnitAsRead(
            index = current.currentIndex,
            forceBackendUpdate = true,
        )

        saveProgress()
        track(
            name = EXERCISE_ANSWERED,
            params = mapOf(
                "chatId" to current.activeChatId,
                "unitIndex" to current.currentIndex.toString(),
                "unitTitle" to unit.title,
                "exerciseIndex" to event.exerciseIndex.toString(),
                "exerciseType" to event.exerciseType.name,
                "exerciseType" to event.exerciseType.name,
                "selectedAnswer" to event.selectedAnswer,
                "correctAnswer" to event.correctAnswer,
                "isCorrect" to event.isCorrect.toString(),
                "timeToAnswerMs" to event.timeToAnswerMs.toString(),
                "maxPoints" to maxPoints.toString(),
                "awardedPoints" to awardedPoints.toString(),
                "gradingStatus" to gradingStatus,
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
                        answeredAtEpochMillis = answeredAt,
                        maxPoints = maxPoints,
                        attempts = listOf(
                            dtoAttempt
                        )
                    )
                )
            }.onFailure {
                platform.log("Progress", "Failed to save exercise answer", it)
            }
        }
    }

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
    private fun sendMessageToTutor(text: String, saveToChat: Boolean) {
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
                    message = trimmed,
                    token = token,
                    chatMode = state.value.selectedTab.name,
                    saveToChat = saveToChat
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

    private fun uploadPdf(picked: PickedFile) {
        scope.launch {
            try {
                _state.update {
                    it.copy(
                        loading = true,
                        pdfProcessingProgress = 0
                    )
                }

                val res = loadTutorFromPdfUseCase.loadTemplateForPickedPdf(
                    picked = picked,
                    userId = userId(),
                    mode = state.value.selectedTab.name,
                    onProgress = { progress ->
                        _state.update {
                            it.copy(
                                pdfProcessingProgress = progress
                            )
                        }
                    }
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
                        "language" to res.language,
                        "mode" to state.value.selectedTab.name,
                        "userId" to userId()
                    )
                )

                val updatedUnits = units.mapIndexed { index, item ->
                    if (index == 0) item.copy(isRead = true) else item
                }

                val selected = updatedUnits.firstOrNull() ?: LearningUnitContentDomain()

                _state.update {
                    it.copy(
                        loading = false,
                        pdfProcessingProgress = null,
                        activeChatId = res.chatId,
                        activeDocumentId = res.documentId,
                        subjectTitle = subjectTitle,
                        allUnits = updatedUnits,
                        currentUnit = selected,
                        currentIndex = 0,
                        unlockedCount = 1,
                        session = session,
                        savedChats = refreshedChats,
                        chatHistory = refreshedChats.toChatHistoryDomain(),
                        isError = false,
                        selectedTab = TutorTab.Learning,
                        tabInfoMessage = buildLearningTabMessage(
                            units.firstOrNull() ?: LearningUnitContentDomain()
                        ),
                        canGoNext = units.size > 1
                    )
                }
                markUnitAsRead(0)
                saveProgress()
                loadAnsweredExercises()
                loadInitialExercisesForCurrentUnit(openExercisesTab = false)
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
                        pdfProcessingProgress = null,
                        showCustomMessage = true,
                        isError = true,
                        message = "Upload failed"
                    )
                }
            }
        }
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

        loadInitialExercisesForCurrentUnit(openExercisesTab = false)
    }

    private fun loadExistingChatAtUnitSection(
        chatId: String,
        unitTitle: String,
        tab: TutorTab = TutorTab.Learning,
        exerciseIndex: Int? = null,
    ) {

        scope.launch {
            try {
                _state.update { it.copy(loading = true) }

                val fullChat = getChatByIdUseCase(chatId, userId())

                val units = fullChat.result?.learningUnits
                    ?: fullChat.partialResult?.learningUnits
                    ?: emptyList()

                val domainUnits = units.map { it.toDomain() }

                val index = domainUnits.indexOfFirst {
                    it.title.trim().equals(unitTitle.trim(), ignoreCase = true)
                }
                if (index == -1) {
                    _state.update {
                        it.copy(
                            loading = false,
                            showCustomMessage = true,
                            isError = true,
                            message = "Unit not found: $unitTitle"
                        )
                    }
                    return@launch
                }
                val selected = domainUnits.getOrNull(index) ?: LearningUnitContentDomain()

                val fullMessages = fullChat.messages
                    .sortedBy { it.createdAtEpochMillis }
                    .map { it.toDomain() }

                val session = buildChatSessionForSelectedUnit(
                    fileName = fullChat.title,
                    unit = selected,
                    allStoredMessages = fullMessages
                )
                val targetExerciseIndex = exerciseIndex ?: 0

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
                        canGoNext = index < domainUnits.lastIndex,
                        selectedTab = tab,
                        selectedExerciseIndex = targetExerciseIndex,
                        tabInfoMessage = when (tab) {
                            TutorTab.Learning -> buildLearningTabMessage(selected)
                            TutorTab.Exercises -> buildExercisesTabMessage(
                                selected,
                                targetExerciseIndex
                            )
                        },
                    )
                }
                markUnitAsRead(index)
                saveProgress()
                loadAnsweredExercises()
                if (
                    tab == TutorTab.Exercises &&
                    selected.exercises.isEmpty()
                ) {
                    loadInitialExercisesForCurrentUnit(openExercisesTab = true)
                }
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
                    it.title.trim().equals(unitTitle.trim(), ignoreCase = true)
                }
                if (index == -1) {
                    _state.update {
                        it.copy(
                            loading = false,
                            showCustomMessage = true,
                            isError = true,
                            message = "Unit not found: $unitTitle"
                        )
                    }
                    return@launch
                }
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
                loadAnsweredExercises()

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

    private fun askTutorForOpenExercise(event: TutorUiEvents.AskTutorForOpenExercise, ) {
        val current = _state.value
        val chatId = current.activeChatId
        val unitIndex = current.currentIndex
        val startedAt = Clock.System.now().toEpochMilliseconds()

        if (chatId.isBlank()) {
            _state.update {
                it.copy(
                    showCustomMessage = true,
                    isError = true,
                    message = "Missing chat"
                )
            }
            return
        }

        if (event.userAnswer.isBlank()) {
            _state.update {
                it.copy(
                    showCustomMessage = true,
                    isError = true,
                    message = "Please write an answer"
                )
            }
            return
        }

        val exercise = current.currentUnit.exercises
            .getOrNull(event.exerciseIndex)

        if (exercise == null) {
            _state.update {
                it.copy(
                    showCustomMessage = true,
                    isError = true,
                    message = "Exercise not found"
                )
            }
            return
        }

        _state.update {
            it.copy(
                loading = true,
                askTutor = true
            )
        }

        scope.launch {
            runCatching {
                val token = getIdTokenSuspendUseCase()

                val response = askTutorUseCase(
                    chatId = chatId,
                    userId = userId(),
                    unitIndex = unitIndex,
                    message = event.userAnswer,
                    token = token,
                    chatMode = "exercises",
                    saveToChat = false,
                    isExerciseEvaluation = true,
                    exerciseIndex = event.exerciseIndex,
                )

                val evaluation = response.evaluation
                    ?: error(
                        "Exercise evaluation was missing " +
                                "from tutor response"
                    )

                val aiText = response.aiMessage?.text
                    ?.takeIf { it.isNotBlank() }
                    ?: evaluation.feedback

                val answeredAt = Clock.System
                    .now()
                    .toEpochMilliseconds()

                val attempt = ExerciseAttemptDto(
                    attemptNumber = 0,
                    awardedPoints = evaluation.awardedPoints,
                    gradingStatus = evaluation.gradingStatus,
                    isCorrect = evaluation.isCorrect,
                    selectedAnswer = event.userAnswer,
                    answeredAtEpochMillis = answeredAt,
                )

                saveExerciseAnswerUseCase(
                    userId = userId(),
                    token = token,
                    answer = ExerciseAnswerProgressDto(
                        chatId = chatId,
                        userId = userId(),
                        unitIndex = unitIndex,
                        unitTitle = current.currentUnit.title,
                        exerciseIndex = event.exerciseIndex,
                        exerciseType = ExerciseType.OPEN.name,
                        question = event.question,
                        selectedAnswer = event.userAnswer,
                        correctAnswer = event.suggestedSolution,
                        isCorrect = evaluation.isCorrect,
                        timeToAnswerMs = answeredAt - startedAt,
                        answeredAtEpochMillis = answeredAt,
                        maxPoints = evaluation.maxPoints,
                        attempts = listOf(attempt),
                    )
                )

                val domainAttempt = LearningUnitExerciseStatsDomain(
                    attemptNumber = exercise.attempts.size + 1,
                    awardedPoints = evaluation.awardedPoints,
                    gradingStatus = evaluation.gradingStatus,
                    isCorrect = evaluation.isCorrect,
                    selectedAnswer = event.userAnswer,
                    answeredAtEpochMillis = answeredAt,
                )

                val updatedExercise = exercise.copy(
                    attempts = exercise.attempts + domainAttempt
                )

                val updatedUnit = current.currentUnit.copy(
                    isRead = true,
                    exercises = current.currentUnit.exercises
                        .mapIndexed { index, oldExercise ->
                            if (index == event.exerciseIndex) {
                                updatedExercise
                            } else {
                                oldExercise
                            }
                        }
                )
                val key = exerciseKey(
                    chatId = chatId,
                    unitIndex = unitIndex,
                    exerciseIndex = event.exerciseIndex,
                )

                _state.update { state ->
                    state.copy(
                        currentUnit = updatedUnit,

                        allUnits = state.allUnits.mapIndexed { index, oldUnit ->
                            if (index == unitIndex) {
                                updatedUnit
                            } else {
                                oldUnit
                            }
                        },

                        chatHistory = state.chatHistory.copy(
                            subjects = state.chatHistory.subjects.map { subject ->
                                if (subject.subjectId != chatId) {
                                    subject
                                } else {
                                    subject.copy(
                                        learningUnits = subject.learningUnits.mapIndexed {
                                                index,
                                                learningUnit,
                                            ->
                                            if (index == unitIndex) {
                                                learningUnit.copy(
                                                    isRead = true
                                                )
                                            } else {
                                                learningUnit
                                            }
                                        }
                                    )
                                }
                            }
                        ),

                        answeredExerciseKeys =
                            state.answeredExerciseKeys + key,

                        loading = false,
                        askTutor = false,

                        exerciseTutorAnswers =
                            state.exerciseTutorAnswers +
                                    (event.exerciseIndex to aiText),
                    )
                }

                markUnitAsRead(
                    index = unitIndex,
                    forceBackendUpdate = true,
                )

                saveProgress()
                /*
                 * Analytics μετά την αξιολόγηση,
                 * ώστε να έχουμε τον πραγματικό βαθμό.
                 */
                track(
                    name = EXERCISE_ANSWERED,
                    params = mapOf(
                        "chatId" to chatId,
                        "unitIndex" to unitIndex.toString(),
                        "unitTitle" to current.currentUnit.title,
                        "exerciseIndex" to
                                event.exerciseIndex.toString(),
                        "exerciseType" to ExerciseType.OPEN.name,
                        "selectedAnswer" to event.userAnswer,
                        "correctAnswer" to event.suggestedSolution,
                        "maxPoints" to
                                evaluation.maxPoints.toString(),
                        "awardedPoints" to
                                evaluation.awardedPoints.toString(),
                        "gradingStatus" to
                                evaluation.gradingStatus,
                        "isCorrect" to
                                evaluation.isCorrect.toString(),
                        "isOpenAnswer" to "true",
                        "timeToAnswerMs" to
                                (answeredAt - startedAt).toString(),
                    )
                )
            }.onFailure { throwable ->
                platform.log(
                    tag = "OpenExercise",
                    message = "Failed to grade open exercise",
                    t = throwable,
                )

                _state.update {
                    it.copy(
                        loading = false,
                        askTutor = false,
                        showCustomMessage = true,
                        isError = true,
                        message = throwable.message
                            ?: "Failed to check your answer",
                    )
                }
            }
        }
    }

    private fun markUnitAsRead(index: Int, forceBackendUpdate: Boolean = false) {
        val current = _state.value
        val chatId = current.activeChatId

        if (chatId.isBlank()) return

        val unit = current.allUnits.getOrNull(index) ?: return
        val wasAlreadyRead = unit.isRead

        if (!wasAlreadyRead) {
            _state.update { state ->

                val updatedUnits = state.allUnits.mapIndexed { i, item ->
                    if (i == index) {
                        item.copy(isRead = true)
                    } else {
                        item
                    }
                }

                val updatedCurrentUnit = if (state.currentIndex == index) {
                    state.currentUnit.copy(
                        isRead = true
                    )
                } else {
                    state.currentUnit
                }

                val updatedHistory = state.chatHistory.copy(
                    subjects = state.chatHistory.subjects.map { subject ->
                        if (subject.subjectId != chatId) {
                            subject
                        } else {
                            subject.copy(
                                learningUnits =
                                    subject.learningUnits.mapIndexed { unitIndex, learningUnit ->
                                        if (unitIndex == index) {
                                            learningUnit.copy(
                                                isRead = true
                                            )
                                        } else {
                                            learningUnit
                                        }
                                    }
                            )
                        }
                    }
                )

                state.copy(
                    allUnits = updatedUnits,
                    currentUnit = updatedCurrentUnit,
                    chatHistory = updatedHistory,
                )
            }
        }

        if (wasAlreadyRead && !forceBackendUpdate) {
            return
        }

        scope.launch {
            runCatching {
                val token = getIdTokenSuspendUseCase()

                markUnitAsReadUseCase(
                    userId = userId(),
                    chatId = chatId,
                    unitIndex = index,
                    token = token,
                )
            }.onFailure {
                platform.log(
                    "Progress",
                    "Failed to mark unit as read",
                    it
                )
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
        val current = _state.value
        val units = current.allUnits
        if (units.isEmpty()) return

        val currentUnit = current.currentUnit
        val exercises = currentUnit.exercises

        if (current.selectedTab == TutorTab.Learning) {
            if (exercises.isEmpty()) {
                loadInitialExercisesForCurrentUnit(openExercisesTab = true)
                return
            }

            _state.update {
                it.copy(
                    selectedTab = TutorTab.Exercises,
                    selectedExerciseIndex = 0,
                    tabInfoMessage =
                        buildExercisesTabMessage(currentUnit)
                )
            }

            saveProgress()
            return
        }

        if (current.selectedTab == TutorTab.Exercises) {
            val nextExerciseIndex = current.selectedExerciseIndex + 1

            if (nextExerciseIndex < exercises.size) {
                _state.update {
                    it.copy(selectedExerciseIndex = nextExerciseIndex)
                }
                saveProgress()
                return
            }
        }

        val nextUnitIndex = current.currentIndex + 1
        if (nextUnitIndex > units.lastIndex) return

        val selectedUnit = units[nextUnitIndex]

        val session = buildChatSessionForSelectedUnit(
            fileName = current.session.material.fileName,
            unit = selectedUnit,
            allStoredMessages = current.allChatMessages
        )

        _state.update {
            it.copy(
                currentIndex = nextUnitIndex,
                unlockedCount = maxOf(current.unlockedCount, nextUnitIndex + 1),
                currentUnit = selectedUnit,
                session = session,
                selectedTab = TutorTab.Learning,
                selectedExerciseIndex = 0,
                tabInfoMessage = buildLearningTabMessage(selectedUnit),
                canGoNext = nextUnitIndex < units.lastIndex
            )
        }

        markUnitAsRead(nextUnitIndex)
        saveProgress()

        loadInitialExercisesForCurrentUnit(openExercisesTab = false)
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

        val progressPercent = if (totalUnits > 0) {
                ((currentIndex + 1) * 100) / totalUnits
            } else { 0 }

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
            progressPercent = progressPercent,
            units = _state.value.allUnits.mapIndexed { index, unit ->
                    UnitReadProgressDomain(
                        unitIndex = index,
                        unitTitle = unit.title,
                        isRead = unit.isRead,
                        totalExercises = unit.exercises.size,
                        totalMaxPoints = unit.exercises.sumOf { exercise -> exercise.maxPoints }
                    )
                }
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
                allUnits = emptyList(),
                currentIndex = 0,
                subjectTitle = "",
                activeChatId = "",
                activeDocumentId = "",
                allChatMessages = emptyList(),
                selectedTab = TutorTab.Learning,
                selectedExerciseIndex = 0,
                tabInfoMessage = "",
                exerciseTutorAnswers = emptyMap(),
                answeredExerciseKeys = emptySet(),
                event = TutorEvents.None,
                exercisesLoading = false,
            )
        }
    }

    fun initializeData() = scope.launch {
        val uid = userId()

        if (_state.value.session.material.fileUploaded) {
            return@launch
        }

        if (uid.isBlank()) {
            platform.log(
                "TutorStore",
                "UserId not ready yet",
                null
            )
            return@launch
        }

        try {
            _state.update {
                it.copy(
                    loading = true,
                    error = null
                )
            }

            val chats = getAllHistoryChatUseCase(uid)

            track(
                name = TUTOR_SCREEN_OPENED,
                params = mapOf(
                    "savedChatsCount" to chats.size.toString(),
                    "userId" to uid
                )
            )

            if (chats.isEmpty()) {
                _state.update {
                    it.copy(
                        loading = false,

                        savedChats = emptyList(),
                        chatHistory = chats.toChatHistoryDomain(),

                        activeChatId = "",
                        activeDocumentId = "",
                        subjectTitle = "",

                        allUnits = emptyList(),
                        currentIndex = 0,
                        unlockedCount = 0,
                        currentUnit = LearningUnitContentDomain(),

                        session = buildEmptyChatSessionStart(),

                        allChatMessages = emptyList(),

                        selectedTab = TutorTab.Learning,
                        selectedExerciseIndex = 0,
                        exercisesLoading = false,

                        tabInfoMessage = "",
                        canGoNext = false,

                        event = TutorEvents.None
                    )
                }

                return@launch
            }

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

            val units =
                latestChat.result?.learningUnits
                    ?: latestChat.partialResult?.learningUnits
                    ?: emptyList()

            val domainUnits = units.mapIndexed { index, unit ->
                val domain = unit.toDomain()

                if (index == 0) {
                    domain.copy(
                        isRead = true
                    )
                } else {
                    domain
                }
            }

            val activeIndex = 0

            val activeUnit =
                domainUnits.firstOrNull()
                    ?: LearningUnitContentDomain()

            val session = buildChatSessionFromStoredChat(
                chat = latestChat,
                units = domainUnits,
                activeIndex = activeIndex
            )

            val fullMessages =
                latestChat.messages
                    .sortedBy {
                        it.createdAtEpochMillis
                    }
                    .map {
                        it.toDomain()
                    }

            val chatHistory =
                chats.toChatHistoryDomain()

            _state.update {
                it.copy(
                    loading = false,

                    savedChats = chats,

                    chatHistory = chatHistory.copy(
                        subjects = chatHistory.subjects.map { subject ->
                            if (subject.subjectId != latestChat.chatId) {
                                subject
                            } else {
                                subject.copy(
                                    learningUnits = subject.learningUnits.mapIndexed {
                                            index,
                                            learningUnit,
                                        ->

                                        if (index == 0) {
                                            learningUnit.copy(
                                                isRead = true
                                            )
                                        } else {
                                            learningUnit
                                        }
                                    }
                                )
                            }
                        }
                    ),
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
                    selectedExerciseIndex = 0,
                    exercisesLoading = false,
                    tabInfoMessage = buildLearningTabMessage(activeUnit),
                    canGoNext = activeIndex < domainUnits.lastIndex,
                    event = TutorEvents.Loaded
                )
            }

            markUnitAsRead(0)
            loadAnsweredExercises()

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

                val initialTab =
                    if (fullChat.mode.equals("Exercises", true)) {
                        TutorTab.Exercises
                    } else {
                        TutorTab.Learning
                    }

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
                        selectedTab = initialTab,
                        selectedExerciseIndex = 0,
                        tabInfoMessage = when (initialTab) {
                            TutorTab.Learning ->
                                buildLearningTabMessage(firstUnit)

                            TutorTab.Exercises ->
                                buildExercisesTabMessage(firstUnit)
                        },
                        canGoNext = domainUnits.size > 1
                    )
                }

                loadAnsweredExercises()

                if (
                    initialTab == TutorTab.Exercises &&
                    firstUnit.exercises.isEmpty()
                ) {
                    loadInitialExercisesForCurrentUnit(openExercisesTab = true)
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

