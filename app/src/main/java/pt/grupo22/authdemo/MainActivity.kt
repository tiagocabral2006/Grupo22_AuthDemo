package pt.grupo22.authdemo

import android.os.Bundle
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

class MainActivity : ComponentActivity() {
    private val vm: AuthViewModel by viewModels { AuthViewModel.factory(applicationContext) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                val s by vm.state.collectAsState()
                AuthScreen(s, vm::onEmailChange, vm::onPasswordChange, vm::login, vm::register, vm::logout)
            }
        }
    }
}

@Composable
fun AuthScreen(
    s: AuthUiState,
    onEmail: (String) -> Unit, onPassword: (String) -> Unit,
    onLogin: () -> Unit, onRegister: () -> Unit, onLogout: () -> Unit,
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
                OutlinedButton(onClick = onRegister, enabled = !s.isLoading) { Text("Registar") }
            }
            if (s.isLoading) LinearProgressIndicator(Modifier.fillMaxWidth().padding(top = 16.dp))
        }
    }
}
