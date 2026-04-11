package org.example.project.tutor.commonViewModel

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.example.project.settings.logout.useCase.LearningUnitsUseCase
import org.example.project.settings.logout.useCase.LogoutUseCase
import org.example.project.strings.SmartTutorStrings
import org.example.project.tutor.domainModels.file.PickedFile
import org.example.project.tutor.domainModels.learningUnits.LearningUnitContentDomain
import org.example.project.tutor.domainModels.tutorChat.ChatMessageDomain
import org.example.project.tutor.domainModels.tutorChat.SenderType
import org.example.project.tutor.domainModels.tutorChat.TutorTab
import org.example.project.tutor.mappers.chatHistory.toChatHistoryDomain
import org.example.project.tutor.mappers.chatHistory.toDomain
import org.example.project.tutor.mappers.learningUnits.buildChatHistoryFromUnits
import org.example.project.tutor.mappers.learningUnits.buildChatSessionStart
import org.example.project.tutor.mappers.learningUnits.buildEmptyChatSessionStart
import org.example.project.tutor.mappers.learningUnits.buildExercisesTabMessage
import org.example.project.tutor.mappers.learningUnits.buildLearningTabMessage
import org.example.project.tutor.state.TutorEvents
import org.example.project.tutor.state.TutorState
import org.example.project.tutor.state.TutorUiEvents
import org.example.project.tutor.useCases.AskTutorUseCase
import org.example.project.tutor.useCases.GetAllHistoryChatUseCase
import org.example.project.tutor.useCases.LoadTutorFromPdfUseCase
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
    private val userId: String,
    private val askTutorUseCase: AskTutorUseCase,
    private val getIdTokenSuspendUseCase: GetIdTokenSuspendUseCase,
) {
    private val _state = MutableStateFlow(TutorState())
    val state: StateFlow<TutorState> = _state

    init {
        initializeData()
    }

    fun onEvent(event: TutorUiEvents) {
        when (event) {
            TutorUiEvents.Logout -> logOutUser()
            is TutorUiEvents.SendMessage -> sendMessageToTutor(event.text)
            TutorUiEvents.NewChat -> startNewChat()
            TutorUiEvents.InitializeStateAfterShowingMessage -> initializeStateAfterShowingMessage()
            TutorUiEvents.ClearUiEvent -> clearUiEvent()
            is TutorUiEvents.ShowErrorMessage -> stateForErrorMessage(event.message)
            is TutorUiEvents.DeleteMaterial -> deleteMaterialLocal(event.subjectId)
            is TutorUiEvents.SelectLearningUnit -> selectLearningUnitByTitle(event.title)
            TutorUiEvents.NextUnit -> goNext()
            is TutorUiEvents.UploadPdf -> uploadPdf(event.file)
            is TutorUiEvents.ChangeTab -> {
                refreshTabInfoMessage(event.tab)
            }

            else -> {}
        }
    }

    fun playAIMessage(text: String) {
        platform.speak(normalizeTranscript(text))
    }

    fun startVoiceInput(onResult: (String) -> Unit) = platform.startVoice(onResult)
    fun stopVoiceInput() = platform.stopVoice()

    private fun selectLearningUnitByTitle(title: String) {
        val units = _state.value.allUnits
        if (units.isEmpty()) return

        val index = units.indexOfFirst { it.title.trim() == title.trim() }
        if (index == -1) return

        val selected = units[index]

        val newSession = buildChatSessionStart(
            fileName = _state.value.session.material.fileName,
            units = units,
            activeIndex = index
        )

        _state.update {
            it.copy(
                currentIndex = index,
                currentUnit = selected,
                session = newSession,
                selectedTab = TutorTab.Learning,
                tabInfoMessage = buildLearningTabMessage(selected)
            )
        }
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

        _state.update { current ->
            current.copy(
                loading = true,
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
                    userId = userId,
                    unitIndex = unitIndex,
                    message = trimmed,
                    token = token,
                    chatMode = state.value.selectedTab.name
                )

                val aiMsg = response.aiMessage?.toDomain()

                _state.update { current ->
                    current.copy(
                        loading = false,
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

            } catch (t: Throwable) {
                val msg = when {
                    t.message?.contains("timeout", ignoreCase = true) == true ->
                        "Ο server άργησε να απαντήσει. Δοκίμασε ξανά σε λίγο."

                    else ->
                        t.message ?: "Failed to send message"
                }

                _state.update {
                    it.copy(
                        loading = false,
                        showCustomMessage = true,
                        isError = true,
                        message = msg
                    )
                }
            }
        }
    }

    private fun refreshTabInfoMessage(tab: TutorTab) {
        val unit = _state.value.currentUnit
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
    }

    private fun uploadPdf(picked: PickedFile) {
        scope.launch {
            try {
                _state.update { it.copy(loading = true) }

                val res = loadTutorFromPdfUseCase.loadTemplateForPickedPdf(
                    picked = picked,
                    userId = userId,
                    mode = state.value.selectedTab.name
                )

                val units = res.prototype.learningUnits
                val subjectTitle = res.prototype.title

                val session = buildChatSessionStart(
                    fileName = subjectTitle,
                    units = units,
                    activeIndex = 0
                )

                val history = buildChatHistoryFromUnits(
                    units = units,
                    unlockedCount = 1,
                    subjectTitle = subjectTitle
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
                        chatHistory = history,
                        showCustomMessage = true,
                        message = "Chat loaded (${res.languageEnum.value})",
                        isError = false,
                        tabInfoMessage = buildLearningTabMessage(
                            units.firstOrNull() ?: LearningUnitContentDomain()
                        )
                    )
                }
            } catch (t: Throwable) {
                t.toUserMessage()
                _state.update {
                    it.copy(
                        loading = false,
                        showCustomMessage = true,
                        isError = true,
                        message = t.message ?: "Upload failed"
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

    private fun deleteMaterialLocal(materialId: String) {
        //todo elpida add

        val newSubjects = _state.value.chatHistory.subjects
            .filterNot { it.subjectId == materialId }

        val newHistory = _state.value.chatHistory.copy(subjects = newSubjects)

        _state.update {
            it.copy(
                chatHistory = newHistory
            )
        }
    }

    private fun goNext() {
        val units = _state.value.allUnits
        val subjectTitle = _state.value.subjectTitle

        val nextIndex = (_state.value.currentIndex + 1).coerceAtMost(units.lastIndex)
        if (nextIndex == _state.value.currentIndex) return

        val newUnlocked = maxOf(_state.value.unlockedCount, nextIndex + 1)

        val session = buildChatSessionStart(
            fileName = _state.value.session.material.fileName,
            units = units,
            activeIndex = nextIndex
        )

        val history = buildChatHistoryFromUnits(
            units = units,
            unlockedCount = newUnlocked,
            subjectTitle = subjectTitle
        )

        _state.update {
            it.copy(
                currentIndex = nextIndex,
                unlockedCount = newUnlocked,
                currentUnit = units.getOrNull(nextIndex) ?: LearningUnitContentDomain(),
                session = session,
                chatHistory = history,
                selectedTab = TutorTab.Learning
            )
        }
    }

    private fun startNewChat() {
        _state.update {
            it.copy(
                session = buildEmptyChatSessionStart(),
                currentUnit = LearningUnitContentDomain(),
                event = TutorEvents.None
            )
        }
    }

    private fun initializeData() = scope.launch {

        if (_state.value.session.material.fileUploaded) return@launch

        try {
            _state.update { it.copy(loading = true, error = null) }

            val chats = getAllHistoryChatUseCase(userId)

            if (chats.isNotEmpty()) {
                val latestChat = chats.first()

                val units = latestChat.result?.learningUnits
                    ?: latestChat.partialResult?.learningUnits
                    ?: emptyList()

                val domainUnits = units.map { it.toDomain() }
                val firstUnit = domainUnits.firstOrNull() ?: LearningUnitContentDomain()

                val session = buildChatSessionStart(
                    fileName = latestChat.title,
                    units = domainUnits,
                    activeIndex = 0
                )

                _state.update {
                    it.copy(
                        loading = false,
                        savedChats = chats,
                        chatHistory = chats.toChatHistoryDomain(),
                        activeChatId = latestChat.chatId,
                        activeDocumentId = latestChat.documentId,
                        subjectTitle = latestChat.title,
                        allUnits = domainUnits,
                        currentIndex = 0,
                        unlockedCount = maxOf(1, domainUnits.size),
                        currentUnit = firstUnit,
                        session = session,
                        selectedTab = TutorTab.Learning,
                        tabInfoMessage = buildLearningTabMessage(firstUnit),
                        event = TutorEvents.Loaded
                    )
                }

                return@launch
            }

            val prototype = getLearningUnitUseCase()
            val units = prototype.learningUnits
            val subjectTitle = prototype.title
            val first = units.firstOrNull()

            val currentIndex = 0
            val unlockedCount = 1

            val session = buildChatSessionStart(
                fileName = subjectTitle,
                units = units,
                activeIndex = currentIndex
            )

            val chatHistory = buildChatHistoryFromUnits(
                units = units,
                unlockedCount = unlockedCount,
                subjectTitle = subjectTitle
            )

            first?.let {
                _state.update {
                    it.copy(
                        loading = false,
                        tabInfoMessage = buildLearningTabMessage(first),
                        selectedTab = TutorTab.Learning,
                        allUnits = units,
                        session = session,
                        currentIndex = currentIndex,
                        unlockedCount = unlockedCount,
                        currentUnit = first,
                        chatHistory = chatHistory,
                        subjectTitle = subjectTitle,
                        event = TutorEvents.Loaded
                    )
                }
            }
        } catch (t: Throwable) {
            platform.log("TutorStore", "initializeData failed", t)
            _state.update {
                it.copy(
                    loading = false,
                    message = t.message ?: "Unknown error",
                    showCustomMessage = true,
                    isError = true
                )
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
