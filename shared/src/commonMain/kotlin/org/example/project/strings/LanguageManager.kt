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

    private var isGreekLocked = false

    fun initialize(languageStorage: LanguageStorage) {
        storage = languageStorage

        if (!isGreekLocked) {
            _language.value =
                languageStorage.loadLanguage()
                    ?: AppLanguage.ENGLISH
        }
    }

    fun setExperimentMode(enabled: Boolean) {
        isGreekLocked = enabled

        _language.value = if (enabled) {
            AppLanguage.GREEK
        } else {
            storage?.loadLanguage()
                ?: AppLanguage.ENGLISH
        }
    }

    fun setLanguage(language: AppLanguage) {
        if (isGreekLocked) return

        _language.value = language
        storage?.saveLanguage(language)
    }
}
