package org.example.project.tutor.viewModel

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.StateFlow
import org.example.project.registration.AuthUserHolder
import org.example.project.settings.logout.useCase.LearningUnitsUseCase
import org.example.project.settings.logout.useCase.LogoutUseCase
import org.example.project.tutor.commonViewModel.TutorCommonViewModel
import org.example.project.tutor.commonViewModel.TutorPlatform
import org.example.project.tutor.repo.analytics.IAnalyticsTracker
import org.example.project.tutor.state.TutorState
import org.example.project.tutor.state.TutorUiEvents
import org.example.project.tutor.useCases.AskTutorUseCase
import org.example.project.tutor.useCases.DeleteChatUseCase
import org.example.project.tutor.useCases.GetAllHistoryChatUseCase
import org.example.project.tutor.useCases.GetChatByIdUseCase
import org.example.project.tutor.useCases.LoadTutorFromPdfUseCase
import org.example.project.tutor.useCases.getIdToken.GetIdTokenSuspendUseCase

class TutorController(
    private val logoutUseCase: LogoutUseCase,
    private val getLearningUnitUseCase: LearningUnitsUseCase,
    private val loadTutorFromPdfUseCase: LoadTutorFromPdfUseCase,
    platform: TutorPlatform,
    private val getAllHistoryChatUseCase: GetAllHistoryChatUseCase,
    private val askTutorUseCase: AskTutorUseCase,
    private val getIdTokenSuspendUseCase: GetIdTokenSuspendUseCase,
    private val getChatByIdUseCase: GetChatByIdUseCase,
    private val deleteChatUseCase: DeleteChatUseCase,
    private val analyticsTracker: IAnalyticsTracker,

    ) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    val common = TutorCommonViewModel(
        logoutUseCase = logoutUseCase,
        getLearningUnitUseCase = getLearningUnitUseCase,
        scope = scope,
        platform = platform,
        loadTutorFromPdfUseCase = loadTutorFromPdfUseCase,
        getAllHistoryChatUseCase = getAllHistoryChatUseCase,
        userId = AuthUserHolder.currentUser?.uid ?: "",
        askTutorUseCase = askTutorUseCase,
        getIdTokenSuspendUseCase = getIdTokenSuspendUseCase,
        getChatByIdUseCase = getChatByIdUseCase,
        deleteChatUseCase = deleteChatUseCase,
        analyticsTracker = analyticsTracker,

    )

    val state: StateFlow<TutorState> = common.state
    fun onEvent(event: TutorUiEvents) = common.onEvent(event)
    fun playAIMessage(text: String) = common.playAIMessage(text)
    fun startVoiceInput(onResult: (String) -> Unit) = common.startVoiceInput(onResult)
    fun stopVoiceInput() = common.stopVoiceInput()
}
