package org.example.project.registration.useCases.registration.signUp

import org.example.project.registration.repo.IAuthRepository

class SignUpUseCase(val repo: IAuthRepository) {
    suspend operator fun invoke(
        email: String,
        password: String,
        onResult: (Boolean, String?) -> Unit,
    ) = repo.signup(
        email = email,
        password = password,
        onResult = onResult
    )
}