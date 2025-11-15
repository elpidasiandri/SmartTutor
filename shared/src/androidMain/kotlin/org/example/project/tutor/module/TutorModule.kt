package org.example.project.tutor.module

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import org.example.project.settings.logout.module.logoutModule
import org.example.project.tutor.ITutorRepo
import org.example.project.tutor.repo.TutorIntroRepoImpl
import org.example.project.tutor.viewModel.TutorIntroViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val tutorModule = module {
    viewModel{
        TutorIntroViewModel(get(),get())
    }
    factory<CoroutineDispatcher> { Dispatchers.IO }
    single<ITutorRepo> {
        TutorIntroRepoImpl()
    }
    includes(logoutModule)
}