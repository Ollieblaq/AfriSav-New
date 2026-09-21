package com.example.ui

import android.content.Context
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Base64
import java.security.KeyStore
import java.security.SecureRandom
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.SecretKeySpec

object SecureSessionManager {
    private const val ANDROID_KEYSTORE = "AndroidKeyStore"
    private const val KEY_ALIAS = "KoboBasketSecureKey_v3"
    private const val PREFS_NAME = "kobo_secure_session_prefs"
    private const val FALLBACK_KEY_PREFS = "kobo_fallback_key_prefs"
    private const val FALLBACK_KEY_NAME = "kobo_fallback_key_b64"
    private const val TRANSFORMATION = "AES/GCM/NoPadding"

    private const val KEY_PHONE = "sec_phone"
    private const val KEY_PIN = "sec_pin"
    private const val KEY_ROLE = "sec_role"
    private const val KEY_NAME = "sec_name"

    @Synchronized
    private fun getSecretKey(context: Context): SecretKey {
        try {
            val keyStore = KeyStore.getInstance(ANDROID_KEYSTORE).apply { load(null) }
            if (keyStore.containsAlias(KEY_ALIAS)) {
                val entry = keyStore.getEntry(KEY_ALIAS, null) as? KeyStore.SecretKeyEntry
                if (entry?.secretKey != null) {
                    return entry.secretKey
                }
            }
            val keyGenerator = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, ANDROID_KEYSTORE)
            val spec = KeyGenParameterSpec.Builder(
                KEY_ALIAS,
                KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
            )
                .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                .setKeySize(256)
                .build()
            keyGenerator.init(spec)
            return keyGenerator.generateKey()
        } catch (t: Throwable) {
            // AndroidKeyStore unavailable or threw exception in current environment -> Fallback to app-private AES key
            return getFallbackSecretKey(context)
        }
    }

    @Synchronized
    private fun getFallbackSecretKey(context: Context): SecretKey {
        return try {
            val prefs = context.getSharedPreferences(FALLBACK_KEY_PREFS, Context.MODE_PRIVATE)
            var keyB64 = prefs.getString(FALLBACK_KEY_NAME, null)
            if (keyB64.isNullOrEmpty()) {
                val randomBytes = ByteArray(32)
                SecureRandom().nextBytes(randomBytes)
                keyB64 = Base64.encodeToString(randomBytes, Base64.NO_WRAP)
                prefs.edit().putString(FALLBACK_KEY_NAME, keyB64).apply()
            }
            val keyBytes = Base64.decode(keyB64, Base64.NO_WRAP)
            SecretKeySpec(keyBytes, "AES")
        } catch (t: Throwable) {
            // Hardened fallback key if shared preferences fails
            val staticSeed = "KoboBasketFallbackKeySalt2026SecurityKey!".toByteArray(Charsets.UTF_8).copyOf(32)
            SecretKeySpec(staticSeed, "AES")
        }
    }

    private fun encrypt(context: Context, key: String, value: String) {
        try {
            val secretKey = getSecretKey(context)
            val cipher = Cipher.getInstance(TRANSFORMATION)
            cipher.init(Cipher.ENCRYPT_MODE, secretKey)
            val iv = cipher.iv
            val encryptedBytes = cipher.doFinal(value.toByteArray(Charsets.UTF_8))
            
            val ivString = Base64.encodeToString(iv, Base64.NO_WRAP)
            val encryptedString = Base64.encodeToString(encryptedBytes, Base64.NO_WRAP)
            
            val combined = "$ivString:$encryptedString"
            val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            prefs.edit().putString(key, combined).apply()
        } catch (t: Throwable) {
            t.printStackTrace()
        }
    }

    private fun decrypt(context: Context, key: String): String? {
        try {
            val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            val combined = prefs.getString(key, null) ?: return null
            val parts = combined.split(":")
            if (parts.size != 2) return null
            
            val iv = Base64.decode(parts[0], Base64.NO_WRAP)
            val encryptedBytes = Base64.decode(parts[1], Base64.NO_WRAP)
            
            val secretKey = getSecretKey(context)
            val cipher = Cipher.getInstance(TRANSFORMATION)
            val spec = GCMParameterSpec(128, iv)
            cipher.init(Cipher.DECRYPT_MODE, secretKey, spec)
            
            val decryptedBytes = cipher.doFinal(encryptedBytes)
            return String(decryptedBytes, Charsets.UTF_8)
        } catch (t: Throwable) {
            t.printStackTrace()
            return null
        }
    }

    fun saveSession(context: Context, phone: String, pin: String, role: String, name: String) {
        try {
            encrypt(context, KEY_PHONE, phone)
            encrypt(context, KEY_PIN, pin)
            encrypt(context, KEY_ROLE, role)
            encrypt(context, KEY_NAME, name)
        } catch (t: Throwable) {
            t.printStackTrace()
        }
    }

    fun hasSession(context: Context): Boolean {
        return try {
            val phone = decrypt(context, KEY_PHONE)
            val pin = decrypt(context, KEY_PIN)
            !phone.isNullOrEmpty() && !pin.isNullOrEmpty()
        } catch (t: Throwable) {
            false
        }
    }

    fun getSavedPhone(context: Context): String? {
        return try { decrypt(context, KEY_PHONE) } catch (t: Throwable) { null }
    }

    fun getSavedPin(context: Context): String? {
        return try { decrypt(context, KEY_PIN) } catch (t: Throwable) { null }
    }

    fun getSavedRole(context: Context): String? {
        return try { decrypt(context, KEY_ROLE) } catch (t: Throwable) { null }
    }

    fun getSavedName(context: Context): String? {
        return try { decrypt(context, KEY_NAME) } catch (t: Throwable) { null }
    }

    fun clearSession(context: Context) {
        try {
            val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            prefs.edit().clear().apply()
        } catch (t: Throwable) {
            t.printStackTrace()
        }
    }
}
