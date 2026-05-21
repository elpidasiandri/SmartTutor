package org.example.project.utils.validation

object Validation {
    // [image 2.3.0] και [image_2.3.0]
    val imageRegex = Regex(
        """\[\s*image[\s_:]+([0-9]+(?:\.[0-9]+)*)\s*\]""",
        RegexOption.IGNORE_CASE
    )

    // [quote_do_not_forget], [quote_beware]
    val quoteKeyRegex = Regex(
        """\[\s*(quote_[a-z0-9_]+)\s*\]""",
        RegexOption.IGNORE_CASE
    )

    val quotationRegex = Regex(
        """\[\s*(?:hot\s*tip\s*=\s*)?quotation\s*\]""",
        RegexOption.IGNORE_CASE
    )

    val anyMarker = Regex("""\[[^\]]+\]""")


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