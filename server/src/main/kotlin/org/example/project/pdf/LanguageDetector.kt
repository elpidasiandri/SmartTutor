package org.example.project.pdf

import java.util.Locale

object LanguageDetector {

    fun detect(text: String): String {
        if (text.isBlank()) return "en"

        val sample = text
            .take(5000)
            .lowercase()

        val greekChars = sample.count {
            it in '\u0370'..'\u03FF' || it in '\u1F00'..'\u1FFF'
        }

        val cyrillicChars = sample.count {
            it in '\u0400'..'\u04FF'
        }

        val latinChars = sample.count {
            it in 'a'..'z'
        }

        return when {
            greekChars > latinChars && greekChars > cyrillicChars -> "el"
            cyrillicChars > latinChars -> "ru"
            else -> detectLatinLanguage(sample)
        }
    }

    private fun detectLatinLanguage(text: String): String {

        val scores = mapOf(
            "en" to listOf(" the ", " and ", " of ", " to ", " is ", " are "),
            "es" to listOf(" el ", " la ", " de ", " que ", " los ", " las "),
            "fr" to listOf(" le ", " la ", " de ", " et ", " les ", " des "),
            "de" to listOf(" der ", " die ", " das ", " und ", " ist ", " von "),
            "it" to listOf(" il ", " la ", " di ", " che ", " e ", " gli "),
            "pt" to listOf(" o ", " a ", " de ", " que ", " os ", " uma ")
        )

        return scores
            .mapValues { (_, words) ->
                words.sumOf { word ->
                    Regex(Regex.escape(word)).findAll(text).count()
                }
            }
            .maxByOrNull { it.value }
            ?.takeIf { it.value > 0 }
            ?.key
            ?: "en"
    }
}
