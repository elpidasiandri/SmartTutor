package org.example.project.strings

import kotlinx.browser.localStorage

class WebLanguageStorage : LanguageStorage {

    override fun saveLanguage(
        language: AppLanguage,
    ) {
        localStorage.setItem("smart_tutor_language", language.name)
    }

    override fun loadLanguage(): AppLanguage? {
        val saved = localStorage.getItem("smart_tutor_language") ?: return null

        return runCatching {
            AppLanguage.valueOf(saved) }.getOrNull()
    }
}
