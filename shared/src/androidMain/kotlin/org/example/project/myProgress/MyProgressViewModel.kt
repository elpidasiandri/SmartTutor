package org.example.project.myProgress

import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.StateFlow
import org.example.project.myProgress.state.MyProgressState
import org.example.project.myProgress.useCases.GetChatProgressListUseCase
import org.example.project.myProgress.useCases.GetProgressOverviewUseCase
import org.example.project.myProgress.viewModel.MyProgressCommonViewModel
import org.example.project.tutor.commonViewModel.TutorPlatform
import org.example.project.tutor.repo.analytics.IAnalyticsTracker

class MyProgressViewModel(
    private val ioDispatcher: CoroutineScope,
    private val analyticsTracker: IAnalyticsTracker,
    private val auth: FirebaseAuth,
    private val platform: TutorPlatform,
    private val getProgressOverviewUseCase: GetProgressOverviewUseCase,
    private val getChatProgressListUseCase: GetChatProgressListUseCase,
) : ViewModel() {
    val common = MyProgressCommonViewModel(
        scope = ioDispatcher,
        analyticsTracker = analyticsTracker,
        userId = auth.uid ?: "",
        platform = platform,
        getProgressOverviewUseCase = getProgressOverviewUseCase,
        getChatProgressListUseCase = getChatProgressListUseCase,
    )
    val state: StateFlow<MyProgressState> = common.state

}
