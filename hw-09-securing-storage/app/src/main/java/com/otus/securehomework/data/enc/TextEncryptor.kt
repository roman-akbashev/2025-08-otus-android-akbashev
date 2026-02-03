package com.otus.securehomework.data.enc

import android.util.Base64
import com.otus.securehomework.data.enc.provider.KeyProvider
import java.security.SecureRandom
import javax.crypto.Cipher
import javax.crypto.spec.GCMParameterSpec
import javax.inject.Inject

class TextEncryptor @Inject constructor(private val keyProvider: KeyProvider) {

    fun encrypt(plainText: String): String {
        val iv = ByteArray(12).apply { SecureRandom().nextBytes(this) }

        val cipher = Cipher.getInstance(AES_GCM_TRANSFORMATION)
        cipher.init(Cipher.ENCRYPT_MODE, keyProvider.secretKey, GCMParameterSpec(128, iv))

        val cipherText = cipher.doFinal(plainText.toByteArray(Charsets.UTF_8))

        val result = iv + cipherText
        return Base64.encodeToString(result, Base64.NO_WRAP)
    }

    fun decrypt(encryptedData: String): String {
        val decoded = Base64.decode(encryptedData, Base64.NO_WRAP)
        val iv = decoded.copyOfRange(0, 12)
        val cipherText = decoded.copyOfRange(12, decoded.size)

        val cipher = Cipher.getInstance(AES_GCM_TRANSFORMATION)
        cipher.init(Cipher.DECRYPT_MODE, keyProvider.secretKey, GCMParameterSpec(128, iv))

        val plainTextBytes = cipher.doFinal(cipherText)
        return String(plainTextBytes, Charsets.UTF_8)
    }

    private companion object {
        const val AES_GCM_TRANSFORMATION = "AES/GCM/NoPadding"
    }
}