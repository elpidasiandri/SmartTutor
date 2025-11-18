package org.example.project.tutor.di

import org.example.project.settings.logOut.logoutModule
import org.example.project.tutor.viewModel.TutorController
import org.koin.dsl.module

val tutorModule = module {
    single { TutorController(get()) }
    includes(logoutModule)
}
