package org.example.project.myProgress

import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.StateFlow
import org.example.project.myProgress.viewModel.MyProgressCommonViewModel
import org.example.project.tutor.commonViewModel.TutorPlatform
import org.example.project.tutor.repo.analytics.IAnalyticsTracker
import org.example.project.tutor.useCases.GetAllHistoryChatUseCase
import org.example.project.tutor.useCases.GetChatByIdUseCase

class MyProgressViewModel(
    private val ioDispatcher: CoroutineScope,
    private val getAllHistoryChatUseCase: GetAllHistoryChatUseCase,
    private val getChatByIdUseCase: GetChatByIdUseCase,
    private val analyticsTracker: IAnalyticsTracker,
    private val auth: FirebaseAuth,
    private val platform: TutorPlatform,
) : ViewModel() {
    val common = MyProgressCommonViewModel(
        ioDispatcher = ioDispatcher,
        getAllHistoryChatUseCase = getAllHistoryChatUseCase,
        getChatByIdUseCase = getChatByIdUseCase,
        analyticsTracker = analyticsTracker,
        userId = auth.uid ?: "",
        platform = platform
    )
    val state: StateFlow<MyProgressState> = common.state

}
