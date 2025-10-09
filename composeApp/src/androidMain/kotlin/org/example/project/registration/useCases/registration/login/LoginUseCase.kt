package org.example.project.registration.useCases.registration.login

import org.example.project.registration.repo.IAuthRepository

class LoginUseCase(val repo: IAuthRepository) {
    suspend operator fun invoke(
        email: String,
        password: String,
        onResult: (Boolean, String?) -> Unit,
    ) = repo.login(
        email = email,
        password = password,
        onResult = onResult
    )
}