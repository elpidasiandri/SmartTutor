package org.example.project.utils.validation

object Validation {
    fun isValidEmail(email: String): Boolean {
        val trimmedEmail = email.trim()

        if (trimmedEmail.isBlank()) return false
        if (!containsOnlyLatinEmailCharacters(trimmedEmail)) return false

        val emailRegex =
            Regex("^[A-Za-z0-9.!#$%&'*+/=?^_`{|}~-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")

        return emailRegex.matches(trimmedEmail)
    }
//    fun isValidDuthEmail(email: String): Boolean {
//        return email.trim().matches(
//            Regex("^[A-Za-z0-9._%+-]+@cs\\.duth\\.gr$")
//        )
//    }
    fun isValidPassword(password: String): Boolean {
        if (password.length < 6) return false
        val hasLetter = password.any { it.isLetter() }
        val hasDigit = password.any { it.isDigit() }
        val hasSpecial = password.any { !it.isLetterOrDigit() }
        return hasLetter && hasDigit && hasSpecial
    }
    fun containsOnlyLatinEmailCharacters(email: String): Boolean {
        return email.none { char ->
            char.code in 0x0370..0x03FF ||
                    char.code in 0x1F00..0x1FFF
        }
    }
}