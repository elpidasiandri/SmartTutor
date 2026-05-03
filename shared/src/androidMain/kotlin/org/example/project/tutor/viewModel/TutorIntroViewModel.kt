package org.example.project.tutor.viewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import org.example.project.myProgress.useCases.DeleteChatProgressUseCase
import org.example.project.myProgress.useCases.UpsertChatProgressUseCase
import org.example.project.settings.logout.useCase.LearningUnitsUseCase
import org.example.project.settings.logout.useCase.LogoutUseCase
import org.example.project.tutor.commonViewModel.TutorCommonViewModel
import org.example.project.tutor.commonViewModel.TutorPlatform
import org.example.project.tutor.repo.analytics.IAnalyticsTracker
import org.example.project.tutor.state.TutorEvents
import org.example.project.tutor.state.TutorState
import org.example.project.tutor.state.TutorUiEvents
import org.example.project.tutor.useCases.AskTutorUseCase
import org.example.project.tutor.useCases.DeleteChatUseCase
import org.example.project.tutor.useCases.GetAllHistoryChatUseCase
import org.example.project.tutor.useCases.GetChatByIdUseCase
import org.example.project.tutor.useCases.LoadTutorFromPdfUseCase
import org.example.project.tutor.useCases.getIdToken.GetIdTokenSuspendUseCase

class TutorIntroViewModel(
    logout: LogoutUseCase,
    private val getLearningUnitUseCase: LearningUnitsUseCase,
    private val platform: TutorPlatform,
    private val loadTutorFromPdfUseCase: LoadTutorFromPdfUseCase,
    private val getAllHistoryChatUseCase: GetAllHistoryChatUseCase,
    private val auth: FirebaseAuth,
    private val askTutorUseCase: AskTutorUseCase,
    private val getIdTokenSuspendUseCase: GetIdTokenSuspendUseCase,
    private val getChatByIdUseCase: GetChatByIdUseCase,
    private val deleteChatUseCase: DeleteChatUseCase,
    private val analyticsTracker: IAnalyticsTracker,
    private val upsertChatProgressUseCase: UpsertChatProgressUseCase,
    private val deleteChatProgressUseCase: DeleteChatProgressUseCase,

    ) : ViewModel() {
    val common = TutorCommonViewModel(
        logoutUseCase = logout,
        getLearningUnitUseCase = getLearningUnitUseCase,
        scope = viewModelScope,
        platform = platform,
        loadTutorFromPdfUseCase = loadTutorFromPdfUseCase,
        getAllHistoryChatUseCase = getAllHistoryChatUseCase,
        userId = auth.uid?:"",
        askTutorUseCase = askTutorUseCase,
        getIdTokenSuspendUseCase = getIdTokenSuspendUseCase,
        getChatByIdUseCase = getChatByIdUseCase,
        deleteChatUseCase = deleteChatUseCase,
        analyticsTracker = analyticsTracker,
        upsertChatProgressUseCase = upsertChatProgressUseCase,
        deleteChatProgressUseCase = deleteChatProgressUseCase,
    )
    val state: StateFlow<TutorState> = common.state

    fun onEvent(event: TutorUiEvents) = common.onEvent(event)

    fun playAIMessage(text: String) = common.playAIMessage(text)
    fun startVoiceInput(onResult: (String) -> Unit) = common.startVoiceInput(onResult)
    fun stopVoiceInput() = common.stopVoiceInput()
}