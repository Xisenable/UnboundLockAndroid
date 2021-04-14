package hrw.es.unboundlock

import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec

class Encryption {

    private val keystoreAlias: String = "UnboundLock"

    private fun generateSecretKey(): SecretKey {
        val keyGenerator = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, "AndroidKeyStore")
        val spec = KeyGenParameterSpec
                .Builder(keystoreAlias, KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT)
                .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                .build()

        keyGenerator.init(spec)
        return keyGenerator.generateKey()
    }

    private fun getSecretKey(): SecretKey {
        val keyStore = KeyStore.getInstance("AndroidKeyStore").apply { load(null) }
        val secretKeyEntry = keyStore.getEntry(keystoreAlias, null) as? KeyStore.SecretKeyEntry
        // return secretKeyEntry.secretKey
        return if (secretKeyEntry != null) {
            secretKeyEntry.secretKey
        } else {
            generateSecretKey()
        }
    }

    /**
     * encrypt a string with an secret key and return the encrypted string & the initialization vector (iv) as ByteArrays
     * @param data string to encrypt
     * @return pair of encrypted string and initialization vector
     */
    fun encrypt(data: String): Pair<ByteArray, ByteArray> {
        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        cipher.init(Cipher.ENCRYPT_MODE, getSecretKey())
        // iv = cipher.iv
        return Pair(cipher.doFinal(data.toByteArray()), cipher.iv)
        //return cipher.doFinal(data.toByteArray()) // ByteArray?
    }

    /**
     * decrypt a string (as ByteArray) with the secret key and the initialization vector (iv)
     * @param encrypted string to decrypt
     * @param iv initialization vector
     * @return decrypted string
     */
    fun decrypt(encrypted: ByteArray, iv: ByteArray): String {
        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        val spec = GCMParameterSpec(128, iv)
        cipher.init(Cipher.DECRYPT_MODE, getSecretKey(), spec)
        val decoded = cipher.doFinal(encrypted)
        return String(decoded, Charsets.UTF_8)
    }

}