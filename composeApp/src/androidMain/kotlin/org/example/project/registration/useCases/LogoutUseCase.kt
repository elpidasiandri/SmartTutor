package org.example.project.registration.useCases

import org.example.project.registration.repo.IAuthRepository

class LogoutUseCase (val repo: IAuthRepository) {
    suspend operator fun invoke(
        email: String,
        password: String,
        onResult: (Boolean, String?) -> Unit,
    ) = repo.logout()
}