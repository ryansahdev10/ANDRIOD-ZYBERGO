package com.zybergo.browser.core

import android.content.Context

/**
 * Custom-built blocker (matches your "custom-built" answer, not a wrapped
 * third-party filter engine like uBlock's declarative rules, which carry
 * more parsing machinery and memory).
 *
 * Memory choice: a flat HashSet<String> of blocked hostnames.
 *  - Lookup is O(1).
 *  - No regex compilation (compiled Pattern objects are surprisingly heavy
 *    and there'd be thousands of them for a real filter list).
 *  - The list loads once at first use and is never duplicated per-tab.
 *
 * assets/blocklist.txt format: one hostname per line, e.g.
 *   doubleclick.net
 *   googlesyndication.com
 *   ads.example.com
 */
class AdBlocker(private val context: Context) {

    private val blockedHosts: HashSet<String> by lazy { loadBlocklist() }
    private var enabled = true

    fun setEnabled(value: Boolean) { enabled = value }

    fun isBlocked(host: String): Boolean {
        if (!enabled) return false
        // Check exact host and parent domains (ads.sub.example.com -> example.com)
        var h = host
        while (true) {
            if (blockedHosts.contains(h)) return true
            val dot = h.indexOf('.')
            if (dot < 0) break
            h = h.substring(dot + 1)
            if (!h.contains('.')) break // stop at bare TLD
        }
        return false
    }

    private fun loadBlocklist(): HashSet<String> {
        val set = HashSet<String>(4096)
        try {
            context.assets.open("blocklist.txt").bufferedReader().useLines { lines ->
                lines.forEach { line ->
                    val trimmed = line.trim()
                    if (trimmed.isNotEmpty() && !trimmed.startsWith("#")) {
                        set.add(trimmed.lowercase())
                    }
                }
            }
        } catch (_: Exception) {
            // Ship with an empty set rather than crash if asset is missing at build time.
        }
        return set
    }
}
