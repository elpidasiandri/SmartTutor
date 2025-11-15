package org.example.project.settings.deleteAccount.di

import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import org.example.project.settings.deleteAccount.useCase.DeleteAccountUseCase
import org.example.project.settings.deleteAccount.repo.IDeleteAccountRepo
import org.example.project.settings.deleteAccount.repo.DeleteAccountRepoImpl
import org.example.project.settings.deleteAccount.viewModel.DeleteAccountViewModel
import org.example.project.settings.logout.module.logoutModule
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val deleteAccountModule = module {
    viewModel {
        DeleteAccountViewModel(get(), get(), get())
    }
    factory<CoroutineDispatcher> { Dispatchers.IO }
    single<IDeleteAccountRepo> {
        DeleteAccountRepoImpl(FirebaseAuth.getInstance())
    }
    single {
        DeleteAccountUseCase(get())
    }
    includes(logoutModule)
}