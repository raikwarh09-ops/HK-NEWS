package com.example.util

import android.content.Context
import java.security.MessageDigest

object AppLockManager {

    private const val PREFS_NAME = "hk_news_security_prefs"
    private const val KEY_IS_UNLOCKED = "is_install_unlocked"
    private const val KEY_REQUIRE_EACH_LAUNCH = "require_each_launch"

    // SHA-256 digest of the installation password
    private const val EXPECTED_PASSWORD_HASH =
        "84c390dad5edd0bb361f68747f3a1e5d2cddeb8dde209147b00d929c1a80b7c8"

    fun sha256Hex(input: String): String {
        val digest = MessageDigest.getInstance("SHA-256")
            .digest(input.toByteArray(Charsets.UTF_8))
        return digest.joinToString("") { "%02x".format(it) }
    }

    fun verifyPassword(input: String): Boolean {
        val normalized = input.trim()
        if (normalized.isEmpty()) return false
        return sha256Hex(normalized) == EXPECTED_PASSWORD_HASH
    }

    fun isAppUnlocked(context: Context): Boolean {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        if (prefs.getBoolean(KEY_REQUIRE_EACH_LAUNCH, false)) {
            return false
        }
        return prefs.getBoolean(KEY_IS_UNLOCKED, false)
    }

    fun unlockWithPassword(context: Context, input: String): Boolean {
        if (!verifyPassword(input)) return false
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit()
            .putBoolean(KEY_IS_UNLOCKED, true)
            .apply()
        return true
    }

    fun lockNow(context: Context) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit()
            .putBoolean(KEY_IS_UNLOCKED, false)
            .apply()
    }

    fun isRequireEachLaunch(context: Context): Boolean {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .getBoolean(KEY_REQUIRE_EACH_LAUNCH, false)
    }

    fun setRequireEachLaunch(context: Context, enabled: Boolean) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit()
            .putBoolean(KEY_REQUIRE_EACH_LAUNCH, enabled)
            .apply()
    }
}
