package org.example.project.services

object LanguageDetector {
    fun detect(text: String): String {
        val sample = text.take(10_000)
        val greekChars = sample.count { it in '\u0370'..'\u03FF' || it in '\u1F00'..'\u1FFF' }
        val letters = sample.count { it.isLetter() }.coerceAtLeast(1)
        val ratio = greekChars.toDouble() / letters.toDouble()
        return if (ratio > 0.15) "el" else "en"
    }
}
