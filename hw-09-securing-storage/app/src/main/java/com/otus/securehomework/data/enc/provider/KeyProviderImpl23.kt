package com.otus.securehomework.data.enc.provider

import android.os.Build
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import androidx.annotation.RequiresApi
import com.otus.securehomework.data.enc.provider.KeyProvider.Companion.KEY_PROVIDER
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey

@RequiresApi(Build.VERSION_CODES.M)
class KeyProviderImpl23 : KeyProvider {

    override val secretKey: SecretKey
        get() = KeyProvider.keyStore.getKey(KEY_ALIAS, null) as? SecretKey ?: generateKey()

    private fun generateKey(): SecretKey {
        val keyGenerator = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, KEY_PROVIDER)
        val spec = KeyGenParameterSpec.Builder(
            KEY_ALIAS,
            KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
        )
            .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
            .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
            .setRandomizedEncryptionRequired(false)
            .build()
        keyGenerator.init(spec)
        return keyGenerator.generateKey()
    }

    private companion object {
        const val KEY_ALIAS = "otus_alias"
    }
}