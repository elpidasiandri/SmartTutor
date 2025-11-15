package org.example.project.settings.deleteAccount.repo

interface IDeleteAccountRepo {
    fun deleteAccount(onResult: (Boolean, String?) -> Unit)
}