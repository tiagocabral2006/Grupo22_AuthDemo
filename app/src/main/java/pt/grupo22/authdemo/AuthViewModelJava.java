package pt.grupo22.authdemo;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Equivalente em Java do AuthViewModel.kt: LiveData + ExecutorService em vez de
 * StateFlow + coroutines. Mesmas regras de validação, mesmas mensagens, mesmas
 * operações (login, registo e logout).
 */
public class AuthViewModelJava extends ViewModel {
    private final AuthRepository repo;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private final MutableLiveData<AuthUiState> state;

    public AuthViewModelJava(AuthRepository repo) {
        this.repo = repo;
        state = new MutableLiveData<>(new AuthUiState("", "", false, null, repo.currentUser()));
    }

    /** Equivalente a AuthViewModel.factory(ctx): cria o ViewModel com as dependências reais. */
    public static ViewModelProvider.Factory factory(final Context ctx) {
        return new ViewModelProvider.Factory() {
            @SuppressWarnings("unchecked")
            @NonNull @Override public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
                AuthRepository repo = new LocalAuthRepository(
                        new KeystoreSecureStorage(ctx.getApplicationContext(), new CryptoManager("grupo22_aes_key")));
                return (T) new AuthViewModelJava(repo);
            }
        };
    }

    public LiveData<AuthUiState> getState() { return state; }

    public void onEmailChange(String v) {
        AuthUiState s = state.getValue();
        state.setValue(new AuthUiState(v, s.getPassword(), s.isLoading(), null, s.getUser()));
    }

    public void onPasswordChange(String v) {
        AuthUiState s = state.getValue();
        state.setValue(new AuthUiState(s.getEmail(), v, s.isLoading(), null, s.getUser()));
    }

    public void login() { submit(false); }
    public void register() { submit(true); }

    public void logout() {
        repo.logout();
        state.setValue(new AuthUiState("", "", false, null, null));
    }

    private void submit(final boolean register) {
        final AuthUiState s = state.getValue();
        if (!Validator.isEmailValid(s.getEmail())) { fail(s, "Email inválido"); return; }
        if (!Validator.isPasswordValid(s.getPassword())) {
            fail(s, "Palavra-passe: mínimo 8 caracteres, letra e dígito"); return;
        }
        state.setValue(new AuthUiState(s.getEmail(), s.getPassword(), true, null, null));
        executor.execute(() -> {                       // fora da main thread
            AuthResult r = register
                    ? repo.register(s.getEmail(), s.getPassword())
                    : repo.login(s.getEmail(), s.getPassword());
            state.postValue(r.getOk()
                    ? new AuthUiState("", "", false, null, s.getEmail().trim().toLowerCase())
                    : new AuthUiState(s.getEmail(), s.getPassword(), false, r.getError(), null));
        });
    }

    private void fail(AuthUiState s, String msg) {
        state.setValue(new AuthUiState(s.getEmail(), s.getPassword(), false, msg, null));
    }

    @Override protected void onCleared() { executor.shutdown(); }
}
