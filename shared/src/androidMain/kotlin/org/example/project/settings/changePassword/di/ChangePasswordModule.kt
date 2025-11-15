package org.example.project.settings.changePassword.di

import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import org.example.project.settings.changePassword.repo.ChangePasswordRepoImpl
import org.example.project.settings.changePassword.repo.IChangePasswordRepo
import org.example.project.settings.changePassword.useCase.ChangePasswordUseCase
import org.example.project.settings.changePassword.viewModel.ChangePasswordViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val changePasswordModule = module {
    viewModel {
        ChangePasswordViewModel(get(), get())
    }
    factory<CoroutineDispatcher> { Dispatchers.IO }
    single<IChangePasswordRepo> {
        ChangePasswordRepoImpl(FirebaseAuth.getInstance())
    }
    single {
        ChangePasswordUseCase(get())
    }
}