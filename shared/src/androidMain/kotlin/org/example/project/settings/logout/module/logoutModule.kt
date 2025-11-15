package org.example.project.settings.logout.module

import com.google.firebase.auth.FirebaseAuth
import org.example.project.settings.logout.repo.LogOutRepoImpl
import org.example.project.settings.logout.repo.ILogoutRepo
import org.example.project.settings.logout.useCase.LogoutUseCase
import org.koin.dsl.module

val logoutModule = module {
    factory<ILogoutRepo> {
        LogOutRepoImpl(FirebaseAuth.getInstance())
    }
    factory {
        LogoutUseCase(get())
    }
}