package pt.grupo22.authdemo;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/** Equivalente em Java do AuthViewModel.kt: LiveData + ExecutorService em vez de StateFlow + coroutines. */
public class AuthViewModelJava extends ViewModel {
    private final AuthRepository repo;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private final MutableLiveData<AuthUiState> state;

    public AuthViewModelJava(AuthRepository repo) {
        this.repo = repo;
        state = new MutableLiveData<>(new AuthUiState("", "", false, null, repo.currentUser()));
    }

    public LiveData<AuthUiState> getState() { return state; }

    public void onEmailChange(String v) { AuthUiState s = state.getValue();
        state.setValue(new AuthUiState(v, s.getPassword(), false, null, s.getUser())); }

    public void onPasswordChange(String v) { AuthUiState s = state.getValue();
        state.setValue(new AuthUiState(s.getEmail(), v, false, null, s.getUser())); }

    public void login() {
        final AuthUiState s = state.getValue();
        if (!Validator.isEmailValid(s.getEmail())) { fail(s, "Email inválido"); return; }
        if (!Validator.isPasswordValid(s.getPassword())) { fail(s, "Palavra-passe inválida"); return; }
        state.setValue(new AuthUiState(s.getEmail(), s.getPassword(), true, null, null));
        executor.execute(() -> {                       // fora da main thread
            AuthResult r = repo.login(s.getEmail(), s.getPassword());
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
