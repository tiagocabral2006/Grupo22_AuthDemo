package pt.grupo22.authdemo

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

data class AuthUiState(
    val email: String = "",
    val password: String = "",
    val isLoading: Boolean = false,
    val error: String? = null,
    val user: String? = null,
)

/** Fluxo unidirecional: UI -> eventos (funções) -> ViewModel -> novo estado -> UI. */
class AuthViewModel(
    private val repo: AuthRepository,
    private val io: CoroutineDispatcher = Dispatchers.Default,
) : ViewModel() {
    private val _state = MutableStateFlow(AuthUiState(user = repo.currentUser()))
    val state: StateFlow<AuthUiState> = _state.asStateFlow()

    fun onEmailChange(v: String) = _state.update { it.copy(email = v, error = null) }
    fun onPasswordChange(v: String) = _state.update { it.copy(password = v, error = null) }
    fun login() = submit(register = false)
    fun register() = submit(register = true)

    fun logout() { repo.logout(); _state.value = AuthUiState() }

    private fun submit(register: Boolean) {
        val s = _state.value
        val err = when {
            !Validator.isEmailValid(s.email) -> "Email inválido"
            !Validator.isPasswordValid(s.password) -> "Palavra-passe: mínimo 8 caracteres, letra e dígito"
            else -> null
        }
        if (err != null) { _state.update { it.copy(error = err) }; return }
        _state.update { it.copy(isLoading = true, error = null) }
        viewModelScope.launch {
            val r = withContext(io) {
                if (register) repo.register(s.email, s.password) else repo.login(s.email, s.password)
            }
            _state.update {
                if (r.ok) AuthUiState(user = s.email.trim().lowercase())
                else it.copy(isLoading = false, error = r.error)
            }
        }
    }

    companion object {
        fun factory(ctx: Context) = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>) =
                AuthViewModel(LocalAuthRepository(KeystoreSecureStorage(ctx))) as T
        }
    }
}
