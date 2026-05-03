package org.example.project.myProgress.module

import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import org.example.project.myProgress.MyProgressViewModel
import org.example.project.myProgress.repo.IProgressRepo
import org.example.project.myProgress.repo.ProgressRepoImpl
import org.example.project.myProgress.useCases.GetChatProgressListUseCase
import org.example.project.myProgress.useCases.GetProgressOverviewUseCase
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val myProgressModule = module {
    factory<CoroutineDispatcher> { Dispatchers.IO }
    single { GetProgressOverviewUseCase(get()) }
    single { GetChatProgressListUseCase(get()) }
    factory<IProgressRepo> {
        ProgressRepoImpl(
            httpClient = get(),
            baseUrl = "http://10.0.2.2:8081",
        )
    }
    factory<CoroutineScope> { CoroutineScope(get<CoroutineDispatcher>() + SupervisorJob()) }

    viewModel {
        MyProgressViewModel(
            ioDispatcher = get(),
            analyticsTracker = get(),
            auth = FirebaseAuth.getInstance(),
            platform = get(),
            getProgressOverviewUseCase = get(),
            getChatProgressListUseCase = get(),
        )
    }
}
