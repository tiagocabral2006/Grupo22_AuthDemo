package pt.grupo22.authdemo

import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyInfo
import android.security.keystore.KeyProperties
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.GCMParameterSpec

/**
 * AES-256/GCM com chave guardada no Android Keystore.
 * A chave não é exportável: as operações de cifra são feitas pelo Keystore (TEE/StrongBox
 * quando o dispositivo o suporta; em emuladores é software, ver [isInsideSecureHardware]).
 */
class CryptoManager(private val alias: String = "grupo22_aes_key") {
    private val ks = KeyStore.getInstance("AndroidKeyStore").apply { load(null) }

    private fun key(): SecretKey =
        (ks.getEntry(alias, null) as? KeyStore.SecretKeyEntry)?.secretKey
            ?: KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, "AndroidKeyStore").apply {
                init(KeyGenParameterSpec.Builder(alias,
                    KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT)
                    .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                    .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                    .setKeySize(256).build())
            }.generateKey()

    /** Devolve IV (12 bytes) + texto cifrado (inclui a tag GCM de autenticação). */
    fun encrypt(plain: ByteArray): ByteArray {
        val c = Cipher.getInstance("AES/GCM/NoPadding").apply { init(Cipher.ENCRYPT_MODE, key()) }
        return c.iv + c.doFinal(plain)
    }

    /** Lança exceção se os dados tiverem sido alterados (a tag GCM não confere). */
    fun decrypt(data: ByteArray): ByteArray {
        val c = Cipher.getInstance("AES/GCM/NoPadding").apply {
            init(Cipher.DECRYPT_MODE, key(), GCMParameterSpec(128, data.copyOfRange(0, 12)))
        }
        return c.doFinal(data, 12, data.size - 12)
    }

    /** true se a chave está em hardware seguro (TEE/StrongBox); false em emuladores. */
    @Suppress("DEPRECATION")
    fun isInsideSecureHardware(): Boolean {
        val k = key()
        val info = SecretKeyFactory.getInstance(k.algorithm, "AndroidKeyStore")
            .getKeySpec(k, KeyInfo::class.java) as KeyInfo
        return info.isInsideSecureHardware
    }

    /** Remove a chave (útil em testes). */
    fun deleteKey() { if (ks.containsAlias(alias)) ks.deleteEntry(alias) }
}
