# Grupo 22 — Arquitetura, segurança, autenticação e testes (Android)

App de demonstração: autenticação local, MVVM com fluxo unidirecional, armazenamento cifrado
(Android Keystore, AES-256/GCM), integração com o Credential Manager e testes automatizados.

| Camada | Kotlin (demo principal) | Equivalente Java |
|---|---|---|
| UI | `MainActivity.kt` (Compose) | — (Compose é Kotlin; em Java usar XML + View Binding) |
| ViewModel | `AuthViewModel.kt` (StateFlow + coroutines) | `AuthViewModelJava.java` (LiveData + ExecutorService; login, registo, logout e factory) |
| Dados | `Data.kt` (Repository + SecureStorage) | interface partilhada |
| Segurança | `CryptoManager.kt`, `PasswordHasher.kt` | `CryptoManagerJava.java` |
| Credential Manager | `CredentialHelper.kt` (funções suspend) | `CredentialHelperJava.java` (Executor + callback) |

## Documentação
- `docs/Protocolo_Experimental_Grupo22.pdf` — protocolo experimental (enquadramento, objetivos, configuração, dados, procedimento, métricas, resultados esperados).
- `docs/Guiao_Apresentacao.md` — guião da apresentação e perguntas para a turma.

## Executar
Abrir a pasta no Android Studio, sincronizar o Gradle (wrapper 8.9, AGP 8.7.3, JDK 17) e correr em emulador/dispositivo (API 26+).
A funcionalidade "Guardar no gestor / Usar guardadas" requer Google Play Services e um gestor de palavras-passe configurado.

## Testes
- Unitários (JVM, 16 testes): `./gradlew testDebugUnitTest` — Validator, PasswordHasher, AuthViewModel (repositório falso) e LocalAuthRepository.
- Instrumentados (6 testes): `./gradlew connectedDebugAndroidTest` — Keystore (round-trip, dados adulterados, hardware seguro) e UI Compose ligada ao ViewModel real.

## Notas de segurança
Sem segredos no repositório. Palavras-passe só guardadas como hash PBKDF2-HMAC-SHA256 (salt aleatório, 600 000 iterações),
cifradas com chave do Keystore; os nomes das entradas no SharedPreferences são hashes SHA-256 (o email não fica em claro).
Limitações conhecidas e trabalho futuro: ver secção 8 do protocolo.
