package pt.grupo22.authdemo

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    private val vm: AuthViewModel by viewModels { AuthViewModel.factory(applicationContext) }
    private val credentials by lazy { CredentialHelper(this) } // precisa de uma Activity

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                val s by vm.state.collectAsStateWithLifecycle()
                AuthScreen(
                    s, vm::onEmailChange, vm::onPasswordChange, vm::login, vm::register, vm::logout,
                    onSaveCredential = { saveCredential(s.email, s.password) },
                    onUseSavedCredential = ::useSavedCredential,
                )
            }
        }
    }

    private fun saveCredential(email: String, password: String) {
        if (!Validator.isEmailValid(email) || !Validator.isPasswordValid(password)) {
            toast("Preencha um email e uma palavra-passe válidos"); return
        }
        lifecycleScope.launch {
            val ok = credentials.savePassword(email.trim(), password)
            toast(if (ok) "Credenciais guardadas no gestor" else "Não foi possível guardar")
        }
    }

    private fun useSavedCredential() {
        lifecycleScope.launch {
            val saved = credentials.getSavedPassword()
            if (saved == null) toast("Sem credenciais guardadas")
            else { vm.onEmailChange(saved.first); vm.onPasswordChange(saved.second) }
        }
    }

    private fun toast(msg: String) = Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
}

@Composable
fun AuthScreen(
    s: AuthUiState,
    onEmail: (String) -> Unit, onPassword: (String) -> Unit,
    onLogin: () -> Unit, onRegister: () -> Unit, onLogout: () -> Unit,
    onSaveCredential: () -> Unit = {},
    onUseSavedCredential: () -> Unit = {},
) {
    Column(Modifier.fillMaxSize().padding(24.dp), verticalArrangement = Arrangement.Center) {
        if (s.user != null) {
            Text("Sessão iniciada: ${s.user}", Modifier.testTag("welcome"))
            Spacer(Modifier.height(16.dp))
            Button(onClick = onLogout, modifier = Modifier.testTag("logout")) { Text("Terminar sessão") }
        } else {
            OutlinedTextField(s.email, onEmail, label = { Text("Email") }, singleLine = true,
                modifier = Modifier.fillMaxWidth().testTag("email"))
            Spacer(Modifier.height(8.dp))
            OutlinedTextField(s.password, onPassword, label = { Text("Palavra-passe") }, singleLine = true,
                visualTransformation = PasswordVisualTransformation(),
                modifier = Modifier.fillMaxWidth().testTag("password"))
            s.error?.let { Text(it, color = MaterialTheme.colorScheme.error, modifier = Modifier.testTag("error")) }
            Spacer(Modifier.height(16.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(onClick = onLogin, enabled = !s.isLoading, modifier = Modifier.testTag("login")) { Text("Entrar") }
                OutlinedButton(onClick = onRegister, enabled = !s.isLoading, modifier = Modifier.testTag("register")) { Text("Registar") }
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                TextButton(onClick = onSaveCredential, modifier = Modifier.testTag("saveCredential")) { Text("Guardar no gestor") }
                TextButton(onClick = onUseSavedCredential, modifier = Modifier.testTag("useCredential")) { Text("Usar guardadas") }
            }
            if (s.isLoading) LinearProgressIndicator(Modifier.fillMaxWidth().padding(top = 16.dp))
        }
    }
}
