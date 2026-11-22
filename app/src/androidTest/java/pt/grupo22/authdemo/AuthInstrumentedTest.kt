package pt.grupo22.authdemo

import android.util.Log
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.getValue
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.runtime.collectAsState
import androidx.test.ext.junit.runners.AndroidJUnit4
import kotlinx.coroutines.Dispatchers
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/** Repositório falso: a UI e o ViewModel são os verdadeiros, só os dados são simulados. */
private class FakeRepo : AuthRepository {
    private var session: String? = null
    override fun register(email: String, password: String) = AuthResult(true).also { session = email.trim().lowercase() }
    override fun login(email: String, password: String) =
        if (password == "correta123") AuthResult(true).also { session = email.trim().lowercase() }
        else AuthResult(false, "Credenciais inválidas")
    override fun currentUser() = session
    override fun logout() { session = null }
}

@RunWith(AndroidJUnit4::class)
class AuthInstrumentedTest {
    @get:Rule val rule = createComposeRule()

    // ---------- Keystore (corre num dispositivo/emulador real) ----------

    @Test fun keystoreRoundTrip() {
        val c = CryptoManager("test_alias_roundtrip"); val plain = "dados sensíveis".toByteArray()
        try { assertArrayEquals(plain, c.decrypt(c.encrypt(plain))) } finally { c.deleteKey() }
    }

    @Test fun keystoreRejectsTamperedData() {
        val c = CryptoManager("test_alias_tamper")
        try {
            val data = c.encrypt("dados sensíveis".toByteArray())
            data[data.size - 1] = (data.last().toInt() xor 1).toByte() // altera 1 bit da tag GCM
            assertThrows(Exception::class.java) { c.decrypt(data) }
        } finally { c.deleteKey() }
    }

    @Test fun keystoreReportsSecureHardware() {
        val c = CryptoManager("test_alias_hw")
        try { Log.i("AuthDemo", "Chave em hardware seguro: ${c.isInsideSecureHardware()}") } finally { c.deleteKey() }
    }

    // ---------- UI Compose ligada ao AuthViewModel real ----------

    private fun showScreen() {
        val vm = AuthViewModel(FakeRepo(), Dispatchers.Unconfined)
        rule.setContent {
            MaterialTheme {
                val s by vm.state.collectAsState()
                AuthScreen(s, vm::onEmailChange, vm::onPasswordChange, vm::login, vm::register, vm::logout)
            }
        }
    }

    private fun waitForWelcome() = rule.waitUntil(5_000) {
        rule.onAllNodesWithTag("welcome").fetchSemanticsNodes().isNotEmpty()
    }

    @Test fun uiShowsErrorOnInvalidEmail() {
        showScreen()
        rule.onNodeWithTag("email").performTextInput("x")
        rule.onNodeWithTag("password").performTextInput("abcdef12")
        rule.onNodeWithTag("login").performClick()
        rule.onNodeWithTag("error").assertTextEquals("Email inválido") // erro vem da validação real
    }

    @Test fun uiLoginShowsWelcome() {
        showScreen()
        rule.onNodeWithTag("email").performTextInput("Ana@ubi.pt")
        rule.onNodeWithTag("password").performTextInput("correta123")
        rule.onNodeWithTag("login").performClick()
        waitForWelcome()
        rule.onNodeWithTag("welcome").assertTextContains("ana@ubi.pt", substring = true)
    }

    @Test fun uiLogoutReturnsToForm() {
        showScreen()
        rule.onNodeWithTag("email").performTextInput("ana@ubi.pt")
        rule.onNodeWithTag("password").performTextInput("correta123")
        rule.onNodeWithTag("login").performClick()
        waitForWelcome()
        rule.onNodeWithTag("logout").performClick()
        rule.onNodeWithTag("email").assertExists()
    }
}
