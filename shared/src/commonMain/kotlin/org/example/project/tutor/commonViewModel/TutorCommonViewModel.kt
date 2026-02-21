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
import org.example.project.tutor.domainModels.file.UploadFile
import org.example.project.tutor.domainModels.learningUnits.LearningUnitContentDomain
import org.example.project.tutor.mappers.learningUnits.buildChatHistoryFromUnits
import org.example.project.tutor.mappers.learningUnits.buildChatSessionStart
import org.example.project.tutor.mappers.learningUnits.buildEmptyChatSessionStart
import org.example.project.tutor.state.TutorEvents
import org.example.project.tutor.state.TutorState
import org.example.project.tutor.state.TutorUiEvents
import org.example.project.tutor.useCases.PdfUploadServiceUseCase
import org.example.project.tutor.useCases.getIdToken.GetIdTokenSuspendUseCase
import org.example.project.utils.EmptyValues

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
    private val pdfUploadServiceUseCase: PdfUploadServiceUseCase,
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
            TutorUiEvents.NewChat -> startNewChat()
            TutorUiEvents.InitializeStateAfterShowingMessage -> initializeStateAfterShowingMessage()
            TutorUiEvents.ClearUiEvent -> clearUiEvent()
            is TutorUiEvents.ShowErrorMessage -> stateForErrorMessage(event.message)
            is TutorUiEvents.DeleteMaterial -> deleteMaterialLocal(event.subjectId)
            is TutorUiEvents.SelectLearningUnit -> selectLearningUnit(event.luId)
            TutorUiEvents.NextUnit -> goNext()
            is TutorUiEvents.OpenUnit -> openUnit(event.index)
            is TutorUiEvents.UploadPdf -> uploadPdf(event.file)
            else -> {}
        }
    }

    fun playAIMessage(text: String) {
        platform.speak(normalizeTranscript(text))
    }

    fun startVoiceInput(onResult: (String) -> Unit) = platform.startVoice(onResult)
    fun stopVoiceInput() = platform.stopVoice()
    private fun selectLearningUnit(luId: String) {
        scope.launch {
            val prototype = getLearningUnitUseCase()
            val units = prototype.learningUnits

            val index = units.indexOfFirst { it.id == luId }
            if (index == -1) return@launch

            val selected = units[index]

            val newSession = buildChatSessionStart(
                materialId = _state.value.session.material.materialId,
                fileName = _state.value.session.material.fileName,
                units = units,
                activeIndex = index
            )

            _state.update {
                it.copy(
                    session = newSession,
                    currentUnit = selected
                )
            }
        }
    }

    private fun uploadPdf(picked: PickedFile) {
        scope.launch {
            try {
                _state.update {
                    it.copy(
                        loading = true
                    )
                }
                val uploadFile = UploadFile(
                    bytes = picked.bytes,
                    fileName = picked.name,
                    contentType = picked.mimeType
                )
                println("Q12345 uploadFile $uploadFile")

                val token = getIdTokenSuspendUseCase()

                val response = pdfUploadServiceUseCase(uploadFile, token)
                println("Q12345 response $response")

                _state.update {
                    it.copy(
                        loading = false,
                        showCustomMessage = true,
                        isError = false,
                        message = "Uploaded: ${response.documentId}",
                        session =  it.session.copy(
                            material = it.session.material.copy(
                                fileUploaded = true
                            )
                        )
                    )
                }

            } catch (t: Throwable) {
                println("Q12345 Throwable $t")
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

    private fun deleteMaterialLocal(materialId: String) {
        //todo elpida add
        val currentMaterialId = _state.value.session.material.materialId

        val newSubjects = _state.value.chatHistory.subjects
            .filterNot { it.subjectId == materialId }

        val newHistory = _state.value.chatHistory.copy(subjects = newSubjects)

        val shouldResetSession = currentMaterialId == materialId

        if (shouldResetSession) {
            _state.update {
                it.copy(
                    chatHistory = newHistory,
                    session = buildEmptyChatSessionStart(),
                    currentUnit = LearningUnitContentDomain(),
                    allUnits = emptyList(),
                    currentIndex = 0,
                    unlockedCount = 1
                )
            }
        } else {
            _state.update {
                it.copy(
                    chatHistory = newHistory
                )
            }
        }
    }

    private fun goNext() {
        val units = _state.value.allUnits
        val subjectTitle = _state.value.subjectTitle
        val materialId = _state.value.session.material.materialId

        val nextIndex = (_state.value.currentIndex + 1).coerceAtMost(units.lastIndex)
        if (nextIndex == _state.value.currentIndex) return

        val newUnlocked = maxOf(_state.value.unlockedCount, nextIndex + 1)

        val session = buildChatSessionStart(
            materialId = materialId,
            fileName = "files/material_learning_units.json",
            units = units,
            activeIndex = nextIndex
        )

        val history = buildChatHistoryFromUnits(
            units = units,
            materialId = materialId,
            unlockedCount = newUnlocked,
            subjectTitle = subjectTitle
        )

        _state.update {
            it.copy(
                currentIndex = nextIndex,
                unlockedCount = newUnlocked,
                currentUnit = units.getOrNull(nextIndex) ?: LearningUnitContentDomain(),
                session = session,
                chatHistory = history
            )
        }
    }

    private fun openUnit(index: Int) {
        val units = _state.value.allUnits
        if (index !in units.indices) return

        val materialId = _state.value.session.material.materialId
        val subjectTitle = _state.value.subjectTitle
        val newUnlocked = maxOf(_state.value.unlockedCount, index + 1)

        val session = buildChatSessionStart(
            materialId = materialId,
            fileName = "files/material_learning_units.json",
            units = units,
            activeIndex = index
        )

        val history = buildChatHistoryFromUnits(
            units = units,
            materialId = materialId,
            unlockedCount = newUnlocked,
            subjectTitle = subjectTitle
        )

        _state.update {
            it.copy(
                currentIndex = index,
                unlockedCount = newUnlocked,
                currentUnit = units.getOrNull(index) ?: LearningUnitContentDomain(),
                session = session,
                chatHistory = history
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
        try {
            _state.update { it.copy(loading = true, error = null) }
            val prototype = getLearningUnitUseCase()
            val units = prototype.learningUnits
            val subjectTitle = prototype.title
            val first = units.firstOrNull()

            val materialId = "material_local_001"
            val currentIndex = 0
            val unlockedCount = 1

            val session = buildChatSessionStart(
                materialId = materialId,
                fileName = "files/material_learning_units.json",
                units = units,
                activeIndex = currentIndex
            )
            val chatHistory = buildChatHistoryFromUnits(
                units = units,
                materialId = materialId,
                unlockedCount = unlockedCount,
                subjectTitle = subjectTitle
            )

            first?.let { currentUnit ->
                _state.update {
                    it.copy(
                        loading = false,
                        allUnits = units,
                        session = session,
                        currentIndex = currentIndex,
                        unlockedCount = unlockedCount,
                        currentUnit = currentUnit,
                        chatHistory = chatHistory,
                        showCustomMessage = false,
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
                    showCustomMessage = true
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
            it.copy(showCustomMessage = false, message = EmptyValues.EMPTY_STRING, isError = false)
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
