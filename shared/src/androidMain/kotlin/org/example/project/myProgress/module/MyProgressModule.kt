package org.example.project.myProgress.module

import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import org.example.project.myProgress.MyProgressViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val myProgressModule = module {
    factory<CoroutineDispatcher> { Dispatchers.IO }

    factory<CoroutineScope> { CoroutineScope(get<CoroutineDispatcher>() + SupervisorJob()) }

    viewModel {
        MyProgressViewModel(
            ioDispatcher = get(),
            getAllHistoryChatUseCase = get(),
            getChatByIdUseCase = get(),
            analyticsTracker = get(),
            auth = FirebaseAuth.getInstance(),
            platform = get()
        )
    }
}
