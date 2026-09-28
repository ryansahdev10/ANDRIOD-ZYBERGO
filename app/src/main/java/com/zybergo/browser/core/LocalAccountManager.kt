package com.zybergo.browser.core

import android.content.Context
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import java.security.SecureRandom
import java.security.spec.KeySpec
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.PBEKeySpec

/**
 * Local-only account: a username + password gate for the browser profile.
 * No network calls, no server — this only protects local data (saved
 * passwords, history, sync-cache) at rest on-device.
 *
 * EncryptedSharedPreferences is backed by the Android Keystore, so the
 * actual encryption key never lives in app memory as raw bytes for long,
 * and there's no custom crypto code to get wrong for the storage layer.
 */
class LocalAccountManager(context: Context) {

    private val prefs by lazy {
        val masterKey = MasterKey.Builder(context)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build()

        EncryptedSharedPreferences.create(
            context,
            "zybergo_account",
            masterKey,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
    }

    fun hasAccount(): Boolean = prefs.contains("pwd_hash")

    fun createAccount(password: CharArray) {
        val salt = ByteArray(16).also { SecureRandom().nextBytes(it) }
        val hash = hashPassword(password, salt)
        prefs.edit()
            .putString("salt", salt.joinToString(",") { it.toString() })
            .putString("pwd_hash", hash)
            .apply()
    }

    fun verify(password: CharArray): Boolean {
        val saltStr = prefs.getString("salt", null) ?: return false
        val storedHash = prefs.getString("pwd_hash", null) ?: return false
        val salt = saltStr.split(",").map { it.toByte() }.toByteArray()
        return hashPassword(password, salt) == storedHash
    }

    private fun hashPassword(password: CharArray, salt: ByteArray): String {
        val spec: KeySpec = PBEKeySpec(password, salt, 120_000, 256)
        val factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256")
        val hash = factory.generateSecret(spec).encoded
        return hash.joinToString("") { "%02x".format(it) }
    }
}
