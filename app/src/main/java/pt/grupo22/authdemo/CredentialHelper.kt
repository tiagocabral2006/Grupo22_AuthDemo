package pt.grupo22.authdemo

import android.content.Context
import androidx.credentials.CreatePasswordRequest
import androidx.credentials.CredentialManager
import androidx.credentials.GetCredentialRequest
import androidx.credentials.GetPasswordOption
import androidx.credentials.PasswordCredential
import androidx.credentials.exceptions.CreateCredentialException
import androidx.credentials.exceptions.GetCredentialException

/**
 * Integração mínima com o Credential Manager: guardar e obter uma palavra-passe do
 * gestor de credenciais do sistema (o ecrã de seleção/confirmação é do sistema).
 * O [context] tem de ser uma Activity, porque o sistema mostra UI própria.
 * Passkeys usam a mesma API (CreatePublicKeyCredentialRequest), mas exigem um servidor
 * (relying party); por isso ficam fora desta demo local.
 */
class CredentialHelper(private val context: Context) {
    private val manager = CredentialManager.create(context)

    /** true se o utilizador confirmou e a credencial foi guardada. */
    suspend fun savePassword(email: String, password: String): Boolean = try {
        manager.createCredential(context, CreatePasswordRequest(id = email, password = password))
        true
    } catch (e: CreateCredentialException) { false } // inclui cancelamento pelo utilizador

    /** Par (email, palavra-passe) escolhido pelo utilizador, ou null se cancelou/não há nenhuma. */
    suspend fun getSavedPassword(): Pair<String, String>? = try {
        val response = manager.getCredential(context, GetCredentialRequest(listOf(GetPasswordOption())))
        (response.credential as? PasswordCredential)?.let { it.id to it.password }
    } catch (e: GetCredentialException) { null }
}
