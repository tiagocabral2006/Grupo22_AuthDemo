package pt.grupo22.authdemo;

import android.content.Context;
import androidx.core.content.ContextCompat;
import androidx.credentials.CreateCredentialResponse;
import androidx.credentials.CreatePasswordRequest;
import androidx.credentials.Credential;
import androidx.credentials.CredentialManager;
import androidx.credentials.CredentialManagerCallback;
import androidx.credentials.GetCredentialRequest;
import androidx.credentials.GetCredentialResponse;
import androidx.credentials.GetPasswordOption;
import androidx.credentials.PasswordCredential;
import androidx.credentials.exceptions.CreateCredentialException;
import androidx.credentials.exceptions.GetCredentialException;
import java.util.concurrent.Executor;

/**
 * Equivalente em Java do CredentialHelper.kt. Em Java não há funções suspend:
 * a API expõe métodos *Async com Executor + callback.
 * O context tem de ser uma Activity (o sistema mostra a sua própria UI).
 */
public class CredentialHelperJava {
    public interface Callback<T> { void onResult(T value); }

    public static final class SavedPassword {
        public final String email, password;
        SavedPassword(String email, String password) { this.email = email; this.password = password; }
    }

    private final Context context;
    private final CredentialManager manager;
    private final Executor executor;

    public CredentialHelperJava(Context context) {
        this.context = context;
        this.manager = CredentialManager.create(context);
        this.executor = ContextCompat.getMainExecutor(context);
    }

    /** Devolve true no callback se a credencial foi guardada. */
    public void savePassword(String email, String password, final Callback<Boolean> cb) {
        manager.createCredentialAsync(context, new CreatePasswordRequest(email, password), null, executor,
            new CredentialManagerCallback<CreateCredentialResponse, CreateCredentialException>() {
                @Override public void onResult(CreateCredentialResponse r) { cb.onResult(true); }
                @Override public void onError(CreateCredentialException e) { cb.onResult(false); }
            });
    }

    /** Devolve a credencial escolhida, ou null se cancelou / não há nenhuma. */
    public void getSavedPassword(final Callback<SavedPassword> cb) {
        GetCredentialRequest request = new GetCredentialRequest.Builder()
                .addCredentialOption(new GetPasswordOption()).build();
        manager.getCredentialAsync(context, request, null, executor,
            new CredentialManagerCallback<GetCredentialResponse, GetCredentialException>() {
                @Override public void onResult(GetCredentialResponse r) {
                    Credential c = r.getCredential();
                    if (c instanceof PasswordCredential) {
                        PasswordCredential p = (PasswordCredential) c;
                        cb.onResult(new SavedPassword(p.getId(), p.getPassword()));
                    } else cb.onResult(null);
                }
                @Override public void onError(GetCredentialException e) { cb.onResult(null); }
            });
    }
}
