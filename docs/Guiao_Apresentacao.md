# Guião da apresentação — Grupo 22 (15–20 min)

Tema 8 — Arquitetura, segurança, autenticação e testes. Sugestão de tempos (total ≈ 18 min).

| Min | Parte | Conteúdo | Quem |
|---|---|---|---|
| 0–2 | Enquadramento | Porque é que arquitetura e segurança importam; o que a demo mostra (autenticação local + MVVM + Keystore + testes) | |
| 2–6 | Conceitos | Camadas e MVVM; fluxo unidirecional (`UI → evento → ViewModel → estado → UI`); PBKDF2 + salt; AES-GCM e Keystore; Credential Manager/passkeys (conceito) | |
| 6–10 | Código Kotlin vs Java | `AuthViewModel.kt` vs `AuthViewModelJava.java` (StateFlow/coroutines vs LiveData/Executor); `CryptoManager.kt` vs `CryptoManagerJava.java`; `CredentialHelper` (suspend vs callback). Explicar que Compose é só Kotlin e a alternativa Java é XML + View Binding | |
| 10–14 | Demonstração | Ver roteiro abaixo | |
| 14–16 | Testes | Correr `./gradlew testDebugUnitTest` (16 testes); mostrar um teste com fake (`AuthViewModelTest`) e um instrumentado (`AuthInstrumentedTest`) | |
| 16–18 | Vantagens, limitações, erros frequentes | Ver abaixo | |
| 18–20 | Discussão | As duas perguntas abaixo | |

## Roteiro da demonstração (≈ 4 min)
1. Registar `ana@ubi.pt` / `segredo123`: mostrar a barra de progresso (PBKDF2 é lento de propósito) e a sessão iniciada.
2. Inserir um email inválido: erro vindo do ViewModel (sem chamar o repositório).
3. Escrever nos campos e rodar o ecrã: o estado mantém-se (ViewModel).
4. Terminar sessão, fechar a app e reabrir; entrar de novo.
5. No terminal: `adb shell run-as pt.grupo22.authdemo cat shared_prefs/secure_prefs.xml` → nomes em hexadecimal e valores opacos, sem email nem palavra-passe.
6. "Guardar no gestor" / "Usar guardadas" (Credential Manager).

> Faça um ensaio completo antes (emulador com Google Play Services para o passo 6) e tenha um plano B (capturas de ecrã) caso o emulador falhe.

## Vantagens
- Lógica testável sem dispositivo (ViewModel + interfaces + fakes).
- Um único estado imutável → UI previsível; sobrevive à rotação.
- Segredos nunca em claro; chave não exportável (Keystore).

## Limitações
- Autenticação local: sem servidor, sem tokens com expiração, sem passkeys reais.
- Sem limite de tentativas nem biometria.
- Em emulador a chave do Keystore é por software (`isInsideSecureHardware = false`).
- Se a chave for invalidada, os dados cifrados perdem-se.

## Erros frequentes
- Guardar a palavra-passe em claro ou só com `SHA-256` sem salt.
- Reutilizar o IV em AES-GCM (aqui o Keystore gera um IV aleatório por cifra).
- Fazer trabalho pesado (hash, cifra, I/O) na thread principal.
- Meter lógica na Activity/Composable em vez de no ViewModel, o que a torna intestável.
- Ativar `allowBackup` com dados cifrados por uma chave que não é restaurada.
- Testes que só verificam o que o próprio teste configurou (não testam o código real).

## Duas perguntas para a turma
1. Guardamos o hash da palavra-passe **e** ciframo-lo com o Keystore. Isto é redundância inútil ou defesa em profundidade? Que ameaça cada camada resolve?
2. Numa app com *backend*, o Credential Manager com passkeys elimina a necessidade de a app gerir palavras-passe? Que responsabilidades passam para o servidor?
