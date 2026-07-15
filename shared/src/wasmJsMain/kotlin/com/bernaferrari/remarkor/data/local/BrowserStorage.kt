package com.bernaferrari.remarkor.data.local

import kotlinx.browser.localStorage
import org.w3c.dom.get
import org.w3c.dom.set

/**
 * Thin wrapper around window.localStorage for wasmJs persistence.
 */
internal object BrowserStorage {
    fun getString(key: String): String? = localStorage[key]

    fun setString(key: String, value: String) {
        localStorage[key] = value
    }

    fun remove(key: String) {
        localStorage.removeItem(key)
    }

    fun getBoolean(key: String, default: Boolean): Boolean =
        when (getString(key)) {
            "true" -> true
            "false" -> false
            else -> default
        }

    fun setBoolean(key: String, value: Boolean) {
        setString(key, value.toString())
    }

    fun getInt(key: String, default: Int): Int =
        getString(key)?.toIntOrNull() ?: default

    fun setInt(key: String, value: Int) {
        setString(key, value.toString())
    }
}
