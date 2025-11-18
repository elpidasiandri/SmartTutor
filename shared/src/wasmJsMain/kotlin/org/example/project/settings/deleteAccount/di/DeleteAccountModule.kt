package org.example.project.settings.deleteAccount.di

import org.example.project.registration.firebase.initFirebase
import org.example.project.settings.deleteAccount.repo.DeleteAccountRepoImpl
import org.example.project.settings.deleteAccount.repo.IDeleteAccountRepo
import org.example.project.settings.deleteAccount.useCase.DeleteAccountUseCase
import org.example.project.settings.deleteAccount.viewModel.DeleteAccountController
import org.example.project.settings.logOut.logoutModule
import org.koin.dsl.module

val deleteAccountModule = module {
    single<IDeleteAccountRepo> { DeleteAccountRepoImpl(initFirebase().app) }
    single {
        DeleteAccountController(get(), get())
    }
    single {
        DeleteAccountUseCase(get())
    }
    includes(logoutModule)
}