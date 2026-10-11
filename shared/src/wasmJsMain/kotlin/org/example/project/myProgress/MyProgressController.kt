package org.example.project.myProgress

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.StateFlow
import org.example.project.myProgress.state.MyProgressState
import org.example.project.myProgress.useCases.GetChatProgressListUseCase
import org.example.project.myProgress.useCases.GetExerciseStatsUseCase
import org.example.project.myProgress.useCases.GetProgressOverviewUseCase
import org.example.project.myProgress.viewModel.MyProgressCommonViewModel
import org.example.project.registration.AuthUserHolder
import org.example.project.tutor.commonViewModel.TutorPlatform
import org.example.project.tutor.repo.analytics.IAnalyticsTracker
import org.example.project.tutor.useCases.GetChatByIdUseCase
import org.example.project.tutor.useCases.GetAllHistoryChatUseCase
class MyProgressController(
    private val analyticsTracker: IAnalyticsTracker,
    private val platform: TutorPlatform,
    private val getProgressOverviewUseCase: GetProgressOverviewUseCase,
    private val getChatProgressListUseCase: GetChatProgressListUseCase,
    private val getExerciseStatsUseCase: GetExerciseStatsUseCase,
    private val getChatByIdUseCase: GetChatByIdUseCase,
    private val userId: String,
    private val getAllHistoryChatUseCase: GetAllHistoryChatUseCase,
    ) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    val common = MyProgressCommonViewModel(
        scope = scope,
        analyticsTracker = analyticsTracker,
        userId = userId ,
        platform = platform,
        getProgressOverviewUseCase = getProgressOverviewUseCase,
        getChatProgressListUseCase = getChatProgressListUseCase,
        getExerciseStateUseCase = getExerciseStatsUseCase,
        getChatByIdUseCase = getChatByIdUseCase,
        getAllHistoryChatUseCase = getAllHistoryChatUseCase,
    )
    val state: StateFlow<MyProgressState> = common.state

}