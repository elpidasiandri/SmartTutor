package org.example.project.settings.logOut

import org.example.project.registration.firebase.initFirebase
import org.example.project.settings.logout.repo.ILogoutRepo
import org.example.project.settings.logout.useCase.LogoutUseCase
import org.koin.dsl.module

val logoutModule= module {
    single<ILogoutRepo> { LogoutRepoImpl(initFirebase().app) }
    factory {
        LogoutUseCase(get())
    }
}