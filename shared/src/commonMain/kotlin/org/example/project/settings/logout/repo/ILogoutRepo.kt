package org.example.project.settings.logout.repo

interface ILogoutRepo {
    fun logout(onResult: (Boolean, String?) ->Unit)
}