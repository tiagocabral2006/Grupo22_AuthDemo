package pt.grupo22.authdemo

object Validator {
    private val EMAIL = Regex("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")

    @JvmStatic fun isEmailValid(email: String) = EMAIL.matches(email.trim())

    /** Mínimo 8 caracteres, com pelo menos uma letra e um dígito. */
    @JvmStatic fun isPasswordValid(p: String) =
        p.length >= 8 && p.any { it.isLetter() } && p.any { it.isDigit() }
}
