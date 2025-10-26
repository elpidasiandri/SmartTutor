package org.example.project.registration.di

import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import org.example.project.registration.repo.AuthRepositoryImpl
import org.example.project.registration.repo.IAuthRepository
import org.example.project.registration.useCases.registration.RegistrationUseCase
import org.example.project.registration.useCases.registration.login.LoginUseCase
import org.example.project.registration.useCases.registration.logOut.LogoutUseCase
import org.example.project.registration.useCases.registration.signUp.SignUpUseCase
import org.example.project.registration.useCases.registration.resetPassword.UpdatePasswordUseCase
import org.example.project.registration.useCases.registration.resetPassword.SendEmailToResetPasswordUseCase
import org.example.project.registration.viewModel.RegistrationViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val registrationModule = module {
    viewModel {
        RegistrationViewModel(
            get(),
            get()
        )
    }
    single<IAuthRepository> {
        AuthRepositoryImpl(FirebaseAuth.getInstance())
    }
    single<CoroutineDispatcher> { Dispatchers.IO }
    single {
        LoginUseCase(get())
    }
    single {
        RegistrationUseCase(
            get(),
            get(),
            get(),
            get(),
            get()
        )
    }
    single {
        LogoutUseCase(get())
    }
    single {
        SendEmailToResetPasswordUseCase(get())
    }
    single {
        UpdatePasswordUseCase(get())
    }
    single {
        SignUpUseCase(get())
    }
}