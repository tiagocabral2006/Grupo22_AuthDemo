package pt.grupo22.authdemo

import android.content.Context
import android.util.Base64

data class AuthResult(val ok: Boolean, val error: String? = null)

/** Camada de dados: a UI/ViewModel só conhecem esta interface (facilita testes com fakes). */
interface AuthRepository {
    fun register(email: String, password: String): AuthResult
    fun login(email: String, password: String): AuthResult
    fun currentUser(): String?
    fun logout()
}

interface SecureStorage {
    fun get(key: String): String?
    fun put(key: String, value: String)
    fun remove(key: String)
}

/** Valores cifrados com AES/GCM (chave no Keystore) antes de irem para SharedPreferences. */
class KeystoreSecureStorage(context: Context, private val crypto: CryptoManager = CryptoManager()) : SecureStorage {
    private val prefs = context.getSharedPreferences("secure_prefs", Context.MODE_PRIVATE)
    override fun get(key: String) = prefs.getString(key, null)?.let {
        String(crypto.decrypt(Base64.decode(it, Base64.NO_WRAP)))
    }
    override fun put(key: String, value: String) {
        prefs.edit().putString(key, Base64.encodeToString(crypto.encrypt(value.toByteArray()), Base64.NO_WRAP)).apply()
    }
    override fun remove(key: String) { prefs.edit().remove(key).apply() }
}

class LocalAuthRepository(private val store: SecureStorage) : AuthRepository {
    private fun k(email: String) = "user:" + email.trim().lowercase()

    override fun register(email: String, password: String): AuthResult {
        if (store.get(k(email)) != null) return AuthResult(false, "Utilizador já existe")
        store.put(k(email), PasswordHasher.hash(password))
        store.put("session", email.trim().lowercase())
        return AuthResult(true)
    }

    override fun login(email: String, password: String): AuthResult {
        val h = store.get(k(email))
        if (h == null || !PasswordHasher.verify(password, h)) return AuthResult(false, "Credenciais inválidas")
        store.put("session", email.trim().lowercase())
        return AuthResult(true)
    }

    override fun currentUser() = store.get("session")
    override fun logout() = store.remove("session")
}
