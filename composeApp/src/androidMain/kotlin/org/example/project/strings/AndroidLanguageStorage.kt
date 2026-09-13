package org.example.project.strings

import android.content.Context
import androidx.core.content.edit

class AndroidLanguageStorage(
    context: Context,
) : LanguageStorage {

    private val preferences =
        context.getSharedPreferences("smart_tutor_preferences", Context.MODE_PRIVATE)

    override fun saveLanguage(
        language: AppLanguage,
    ) {
        preferences.edit { putString("language", language.name) }
    }

    override fun loadLanguage(): AppLanguage? {
        val saved = preferences.getString("language", null) ?: return null

        return runCatching {
            AppLanguage.valueOf(saved)
        }.getOrNull()
    }
}
