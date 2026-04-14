package org.example.project.myProgress

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.StateFlow
import org.example.project.myProgress.viewModel.MyProgressCommonViewModel
import org.example.project.registration.AuthUserHolder
import org.example.project.tutor.commonViewModel.TutorPlatform
import org.example.project.tutor.repo.analytics.IAnalyticsTracker
import org.example.project.tutor.useCases.GetAllHistoryChatUseCase
import org.example.project.tutor.useCases.GetChatByIdUseCase

class MyProgressController(
    private val getAllHistoryChatUseCase: GetAllHistoryChatUseCase,
    private val getChatByIdUseCase: GetChatByIdUseCase,
    private val analyticsTracker: IAnalyticsTracker,
    private val platform: TutorPlatform,
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    val common = MyProgressCommonViewModel(
        ioDispatcher = scope,
        getAllHistoryChatUseCase = getAllHistoryChatUseCase,
        getChatByIdUseCase = getChatByIdUseCase,
        analyticsTracker = analyticsTracker,
        userId = AuthUserHolder.currentUser?.uid ?: "",
        platform = platform
    )
    val state: StateFlow<MyProgressState> = common.state

}