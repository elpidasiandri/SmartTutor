package org.example.project.utils

object Validation {
    fun isValidEmail(email: String): Boolean {
        return email.contains("@") && email.contains(".")
    }

    fun isValidPassword(password: String): Boolean {
        if (password.length < 6) return false
        val hasLetter = password.any { it.isLetter() }
        val hasDigit = password.any { it.isDigit() }
        val hasSpecial = password.any { !it.isLetterOrDigit() }
        return hasLetter && hasDigit && hasSpecial
    }
}