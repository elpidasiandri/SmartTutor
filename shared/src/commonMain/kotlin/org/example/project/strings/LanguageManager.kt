package org.example.project.strings

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

object LanguageManager {

    private val _language =
        MutableStateFlow(AppLanguage.ENGLISH)

    val language: StateFlow<AppLanguage> =
        _language.asStateFlow()

    private var storage: LanguageStorage? = null

    fun initialize(
        languageStorage: LanguageStorage
    ) {
        storage = languageStorage

        val savedLanguage = languageStorage.loadLanguage()

        if (savedLanguage != null) {
            _language.value = savedLanguage
        }
    }

    fun setLanguage(
        language: AppLanguage
    ) {
        _language.value = language
        storage?.saveLanguage(language)
    }
}
