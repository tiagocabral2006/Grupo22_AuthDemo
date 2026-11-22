package pt.grupo22.authdemo

import org.junit.Assert.*
import org.junit.Test

/** Armazenamento em memória: permite testar o repositório na JVM, sem Keystore. */
private class MemoryStore : SecureStorage {
    val map = mutableMapOf<String, String>()
    override fun get(key: String) = map[key]
    override fun put(key: String, value: String) { map[key] = value }
    override fun remove(key: String) { map.remove(key) }
}

class LocalAuthRepositoryTest {
    private val store = MemoryStore()
    private val repo = LocalAuthRepository(store)

    @Test fun register_thenLogin_succeeds() {
        assertTrue(repo.register("ana@ubi.pt", "segredo123").ok)
        repo.logout()
        assertTrue(repo.login("ana@ubi.pt", "segredo123").ok)
    }

    @Test fun register_duplicate_isRejected_caseInsensitive() {
        repo.register("ana@ubi.pt", "segredo123")
        val r = repo.register(" ANA@ubi.pt ", "outra12345")
        assertFalse(r.ok); assertEquals("Utilizador já existe", r.error)
    }

    @Test fun login_wrongPassword_fails() {
        repo.register("ana@ubi.pt", "segredo123"); repo.logout()
        val r = repo.login("ana@ubi.pt", "errada1234")
        assertFalse(r.ok); assertNull(repo.currentUser())
    }

    @Test fun login_unknownUser_fails() {
        assertFalse(repo.login("ninguem@ubi.pt", "segredo123").ok)
    }

    @Test fun session_isSetOnLogin_andClearedOnLogout() {
        repo.register("Ana@ubi.pt", "segredo123")
        assertEquals("ana@ubi.pt", repo.currentUser())
        repo.logout()
        assertNull(repo.currentUser())
    }

    @Test fun passwordIsNeverStoredInPlainText() {
        repo.register("ana@ubi.pt", "segredo123")
        assertTrue(store.map.values.none { it.contains("segredo123") })
    }
}
