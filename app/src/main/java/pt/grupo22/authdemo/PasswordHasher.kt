package pt.grupo22.authdemo

import java.security.MessageDigest
import java.security.SecureRandom
import java.util.Base64
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.PBEKeySpec

/**
 * PBKDF2-HMAC-SHA256 com salt aleatório. Nunca se guarda a palavra-passe em claro.
 * Formato guardado: "iterações:salt(base64):hash(base64)". As iterações vão dentro do
 * valor para o custo poder aumentar no futuro sem invalidar hashes antigos.
 * 600 000 iterações = valor mínimo recomendado pela OWASP para PBKDF2-HMAC-SHA256.
 */
object PasswordHasher {
    const val ITERATIONS = 600_000
    private fun b64(b: ByteArray) = Base64.getEncoder().encodeToString(b)

    private fun derive(pw: String, salt: ByteArray, iterations: Int): ByteArray =
        SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256")
            .generateSecret(PBEKeySpec(pw.toCharArray(), salt, iterations, 256)).encoded

    @JvmStatic fun hash(pw: String): String {
        val salt = ByteArray(16).also { SecureRandom().nextBytes(it) }
        return "$ITERATIONS:${b64(salt)}:${b64(derive(pw, salt, ITERATIONS))}"
    }

    @JvmStatic fun verify(pw: String, stored: String): Boolean {
        val parts = stored.split(":")
        if (parts.size != 3) return false
        val iterations = parts[0].toIntOrNull() ?: return false
        return try {
            val salt = Base64.getDecoder().decode(parts[1])
            val expected = Base64.getDecoder().decode(parts[2])
            MessageDigest.isEqual(derive(pw, salt, iterations), expected) // tempo constante
        } catch (e: IllegalArgumentException) {
            false // base64 inválido
        }
    }
}
