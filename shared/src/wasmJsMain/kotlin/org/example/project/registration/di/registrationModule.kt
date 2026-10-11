package org.example.project.registration.di

import org.example.project.registration.firebase.FirebaseContext
import org.example.project.registration.repo.IAuthRepository
import org.example.project.registration.repo.WebAuthRepositoryImpl
import org.example.project.registration.useCases.registration.RegistrationUseCase
import org.example.project.registration.useCases.registration.login.LoginUseCase
import org.example.project.registration.useCases.registration.resetPassword.SendEmailToResetPasswordUseCase
import org.example.project.registration.useCases.registration.signUp.SignUpUseCase
import org.example.project.registration.viewModel.WebRegistrationController
import org.koin.dsl.module

val registrationModule = module {
    factory<IAuthRepository> { WebAuthRepositoryImpl(get<FirebaseContext>().app) }
    factory { RegistrationUseCase(get(), get(), get()) }
    factory { WebRegistrationController(get()) }
    factory {
        SignUpUseCase(get())
    }
    factory {
        LoginUseCase(get())
    }

    factory {
        SendEmailToResetPasswordUseCase(get())
    }
}
