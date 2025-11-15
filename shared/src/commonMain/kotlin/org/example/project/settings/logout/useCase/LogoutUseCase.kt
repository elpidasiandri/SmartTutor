package org.example.project.settings.logout.useCase

import org.example.project.settings.logout.repo.ILogoutRepo

class LogoutUseCase(private val repo: ILogoutRepo) {
    operator fun invoke(onResult: (Boolean, String?) -> Unit) =
        repo.logout(onResult)
}