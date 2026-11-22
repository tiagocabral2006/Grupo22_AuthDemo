package pt.grupo22.authdemo

import java.security.MessageDigest
import java.security.SecureRandom
import java.util.Base64
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.PBEKeySpec

/** PBKDF2-HMAC-SHA256 com salt aleatório. Nunca se guarda a palavra-passe em claro. */
object PasswordHasher {
    private const val ITERATIONS = 120_000
    private fun b64(b: ByteArray) = Base64.getEncoder().encodeToString(b)

    private fun derive(pw: String, salt: ByteArray): ByteArray =
        SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256")
            .generateSecret(PBEKeySpec(pw.toCharArray(), salt, ITERATIONS, 256)).encoded

    @JvmStatic fun hash(pw: String): String {
        val salt = ByteArray(16).also { SecureRandom().nextBytes(it) }
        return "${b64(salt)}:${b64(derive(pw, salt))}"
    }

    @JvmStatic fun verify(pw: String, stored: String): Boolean {
        val (s, h) = stored.split(":").takeIf { it.size == 2 } ?: return false
        val expected = Base64.getDecoder().decode(h)
        return MessageDigest.isEqual(derive(pw, Base64.getDecoder().decode(s)), expected)
    }
}
