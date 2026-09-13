package org.example.project.strings

interface LanguageStorage {
    fun saveLanguage(language: AppLanguage)
    fun loadLanguage(): AppLanguage?
}
