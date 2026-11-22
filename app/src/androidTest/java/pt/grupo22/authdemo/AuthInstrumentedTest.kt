package pt.grupo22.authdemo

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Assert.assertArrayEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class AuthInstrumentedTest {
    @get:Rule val rule = createComposeRule()

    @Test fun keystoreRoundTrip() {
        val c = CryptoManager("test_alias"); val plain = "dados sensíveis".toByteArray()
        assertArrayEquals(plain, c.decrypt(c.encrypt(plain)))
    }

    @Test fun uiShowsErrorOnInvalidEmail() {
        rule.setContent {
            var s by androidx.compose.runtime.mutableStateOf(AuthUiState())
            AuthScreen(s, { s = s.copy(email = it) }, { s = s.copy(password = it) },
                { s = s.copy(error = "Email inválido") }, {}, {})
        }
        rule.onNodeWithTag("email").performTextInput("x")
        rule.onNodeWithTag("login").performClick()
        rule.onNodeWithTag("error").assertTextEquals("Email inválido")
    }
}
