# Grupo 22 — Arquitetura, segurança, autenticação e testes (Android)

App de demonstração: autenticação local, MVVM com fluxo unidirecional, armazenamento cifrado (Android Keystore, AES-256/GCM) e testes automatizados.

| Camada | Kotlin (demo principal) | Equivalente Java |
|---|---|---|
| UI | `MainActivity.kt` (Compose) | — (Compose é Kotlin; em Java usar XML + View Binding) |
| ViewModel | `AuthViewModel.kt` (StateFlow + coroutines) | `AuthViewModelJava.java` (LiveData + ExecutorService) |
| Dados | `Data.kt` (Repository + SecureStorage) | interface partilhada |
| Segurança | `CryptoManager.kt`, `PasswordHasher.kt` | `CryptoManagerJava.java` |

## Executar
Abrir a pasta no Android Studio (Koala+), sincronizar o Gradle e correr em emulador/dispositivo (API 26+).

## Testes
- Unitários (JVM): `./gradlew testDebugUnitTest` — Validator, PasswordHasher, AuthViewModel (3 testes com fake repo).
- Instrumentados: `./gradlew connectedDebugAndroidTest` — round-trip do Keystore e teste de UI Compose.

## Notas de segurança
Sem segredos no repositório. Palavras-passe só guardadas como hash PBKDF2 (salt aleatório, 120 000 iterações) e cifradas com chave do Keystore.
O protocolo experimental (PDF) vai em `docs/`.
