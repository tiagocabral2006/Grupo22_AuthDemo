package pt.grupo22.authdemo

import org.junit.Assert.*
import org.junit.Test

class ValidatorAndHasherTest {
    @Test fun emailValidation() {
        assertTrue(Validator.isEmailValid("ana@ubi.pt"))
        assertFalse(Validator.isEmailValid("ana@"))
    }
    @Test fun passwordRules() {
        assertTrue(Validator.isPasswordValid("abcdef12"))
        assertFalse(Validator.isPasswordValid("curta1"))
        assertFalse(Validator.isPasswordValid("semdigitos"))
    }
    @Test fun hashVerifiesAndIsSalted() {
        val h1 = PasswordHasher.hash("segredo123"); val h2 = PasswordHasher.hash("segredo123")
        assertNotEquals(h1, h2)                       // salts diferentes
        assertTrue(PasswordHasher.verify("segredo123", h1))
        assertFalse(PasswordHasher.verify("errada123", h1))
    }
    @Test fun hashEncodesIterationsAndRejectsMalformed() {
        assertTrue(PasswordHasher.hash("segredo123").startsWith("${PasswordHasher.ITERATIONS}:"))
        assertFalse(PasswordHasher.verify("segredo123", "lixo"))
        assertFalse(PasswordHasher.verify("segredo123", "abc:!!:!!"))
    }
}
