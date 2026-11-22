package pt.grupo22.authdemo

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

private class FakeRepo : AuthRepository {
    var session: String? = null; var calls = 0
    override fun register(email: String, password: String) = AuthResult(true)
    override fun login(email: String, password: String): AuthResult { calls++
        return if (password == "correta123") AuthResult(true) else AuthResult(false, "Credenciais inválidas") }
    override fun currentUser() = session
    override fun logout() { session = null }
}

@OptIn(ExperimentalCoroutinesApi::class)
class AuthViewModelTest {
    private val dispatcher = UnconfinedTestDispatcher()
    @Before fun setUp() = Dispatchers.setMain(dispatcher)
    @After fun tearDown() = Dispatchers.resetMain()

    @Test fun invalidEmail_showsError_withoutCallingRepo() {
        val repo = FakeRepo(); val vm = AuthViewModel(repo, dispatcher)
        vm.onEmailChange("invalido"); vm.onPasswordChange("correta123"); vm.login()
        assertEquals("Email inválido", vm.state.value.error); assertEquals(0, repo.calls)
    }

    @Test fun validLogin_updatesUserInState() {
        val vm = AuthViewModel(FakeRepo(), dispatcher)
        vm.onEmailChange("Ana@ubi.pt"); vm.onPasswordChange("correta123"); vm.login()
        assertEquals("ana@ubi.pt", vm.state.value.user); assertFalse(vm.state.value.isLoading)
    }

    @Test fun wrongPassword_showsRepoError() {
        val vm = AuthViewModel(FakeRepo(), dispatcher)
        vm.onEmailChange("ana@ubi.pt"); vm.onPasswordChange("errada1234"); vm.login()
        assertNull(vm.state.value.user); assertEquals("Credenciais inválidas", vm.state.value.error)
    }
}
