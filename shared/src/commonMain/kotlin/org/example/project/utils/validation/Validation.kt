package org.example.project.utils.validation

object Validation {
    fun isValidEmail(email: String): Boolean {
        return email.contains("@") && email.contains(".")
    }
    fun isValidDuthEmail(email: String): Boolean {
        return email.trim().matches(
            Regex("^[A-Za-z0-9._%+-]+@cs\\.duth\\.gr$")
        )
    }
    fun isValidPassword(password: String): Boolean {
        if (password.length < 6) return false
        val hasLetter = password.any { it.isLetter() }
        val hasDigit = password.any { it.isDigit() }
        val hasSpecial = password.any { !it.isLetterOrDigit() }
        return hasLetter && hasDigit && hasSpecial
    }
}