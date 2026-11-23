// AGP 9 já inclui suporte a Kotlin (não é preciso o plugin org.jetbrains.kotlin.android).
// O plugin do compilador Compose tem a versão do Kotlin.
plugins {
    id("com.android.application") version "9.3.3" apply false
    id("org.jetbrains.kotlin.plugin.compose") version "2.4.21" apply false
}
