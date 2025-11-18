package org.example.project.settings.changePassword.di

import org.example.project.registration.firebase.initFirebase
import org.example.project.settings.changePassword.repo.ChangePasswordRepoImpl
import org.example.project.settings.changePassword.repo.IChangePasswordRepo
import org.example.project.settings.changePassword.useCase.ChangePasswordUseCase
import org.example.project.settings.changePassword.viewModel.ChangePasswordController
import org.koin.dsl.module

val changePasswordModule = module {
    single {
        ChangePasswordController(get())
    }
    single { ChangePasswordUseCase(get()) }
    single<IChangePasswordRepo> { ChangePasswordRepoImpl(initFirebase().app) }
}