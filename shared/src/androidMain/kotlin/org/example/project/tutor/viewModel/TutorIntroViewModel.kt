package org.example.project.tutor.viewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.StateFlow
import org.example.project.settings.logout.useCase.LearningUnitsUseCase
import org.example.project.settings.logout.useCase.LogoutUseCase
import org.example.project.tutor.commonViewModel.TutorCommonViewModel
import org.example.project.tutor.commonViewModel.TutorPlatform
import org.example.project.tutor.state.TutorState
import org.example.project.tutor.state.TutorUiEvents
import org.example.project.tutor.useCases.GetAllHistoryChatUseCase
import org.example.project.tutor.useCases.LoadTutorFromPdfUseCase

class TutorIntroViewModel(
    logout: LogoutUseCase,
    private val getLearningUnitUseCase: LearningUnitsUseCase,
    private val platform: TutorPlatform,
    private val loadTutorFromPdfUseCase: LoadTutorFromPdfUseCase,
    private val getAllHistoryChatUseCase: GetAllHistoryChatUseCase,
    private val auth: FirebaseAuth
) : ViewModel() {
    private val common = TutorCommonViewModel(
        logoutUseCase = logout,
        getLearningUnitUseCase = getLearningUnitUseCase,
        scope = viewModelScope,
        platform = platform,
        loadTutorFromPdfUseCase = loadTutorFromPdfUseCase,
        getAllHistoryChatUseCase = getAllHistoryChatUseCase,
        userId = auth.uid?:""
    )
    val state: StateFlow<TutorState> = common.state

    fun onEvent(event: TutorUiEvents) = common.onEvent(event)

    fun playAIMessage(text: String) = common.playAIMessage(text)
    fun startVoiceInput(onResult: (String) -> Unit) = common.startVoiceInput(onResult)
    fun stopVoiceInput() = common.stopVoiceInput()
}