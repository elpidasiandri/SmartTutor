package org.example.project.registration.di

import org.example.project.registration.firebase.initFirebase
import org.example.project.registration.repo.IAuthRepository
import org.example.project.registration.repo.WebAuthRepositoryImpl
import org.example.project.registration.useCases.registration.RegistrationUseCase
import org.example.project.registration.useCases.registration.login.LoginUseCase
import org.example.project.registration.useCases.registration.resetPassword.SendEmailToResetPasswordUseCase
import org.example.project.registration.useCases.registration.signUp.SignUpUseCase
import org.example.project.registration.viewModel.WebRegistrationController
import org.koin.dsl.module

val registrationModule = module {
    single<IAuthRepository> { WebAuthRepositoryImpl(initFirebase().app) }
    single { RegistrationUseCase(get(), get(), get()) }
    single { WebRegistrationController(get()) }
    single {
        SignUpUseCase(get())
    }
    single {
        LoginUseCase(get())
    }

    single {
        SendEmailToResetPasswordUseCase(get())
    }
}
