package org.example.project.settings.deleteAccount.useCase

import org.example.project.settings.deleteAccount.repo.IDeleteAccountRepo

class DeleteAccountUseCase(val repo: IDeleteAccountRepo) {
    operator fun invoke(onResult: (Boolean, String?) -> Unit) =
        repo.deleteAccount(onResult)
}