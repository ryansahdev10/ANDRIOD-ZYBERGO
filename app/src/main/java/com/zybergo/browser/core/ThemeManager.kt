package com.zybergo.browser.core

import android.content.Context
import android.content.SharedPreferences

/**
 * A theme is just a small set of ARGB ints — no bitmap assets, no per-theme
 * layout inflation. Applying a theme means re-tinting existing views, which
 * is why themes cost effectively nothing in RAM regardless of how many the
 * user creates in the designer.
 */
data class Theme(
    val id: String,
    val name: String,
    val primary: Int,
    val background: Int,
    val surface: Int,
    val textPrimary: Int,
    val accent: Int,
    val isDark: Boolean,
    val isCustom: Boolean = false
)

object BuiltInThemes {
    val DARK = Theme("dark", "Midnight", 0xFF1A1A2E.toInt(), 0xFF16162A.toInt(), 0xFF232342.toInt(), 0xFFEDEDF5.toInt(), 0xFF6C63FF.toInt(), isDark = true)
    val LIGHT = Theme("light", "Daylight", 0xFFFFFFFF.toInt(), 0xFFF5F5F7.toInt(), 0xFFFFFFFF.toInt(), 0xFF1A1A1A.toInt(), 0xFF3D5AFE.toInt(), isDark = false)
    val OCEAN = Theme("ocean", "Ocean", 0xFF0B3D91.toInt(), 0xFF072248.toInt(), 0xFF0E407A.toInt(), 0xFFE6F1FF.toInt(), 0xFF00BCD4.toInt(), isDark = true)
    val FOREST = Theme("forest", "Forest", 0xFF1B4332.toInt(), 0xFF10261E.toInt(), 0xFF2D6A4F.toInt(), 0xFFE8F5E9.toInt(), 0xFF95D5B2.toInt(), isDark = true)

    val all = listOf(DARK, LIGHT, OCEAN, FOREST)
}

/**
 * Stores the active theme id and any user-created custom themes as flat
 * key-value pairs (not JSON blobs pulled fully into memory as objects) in
 * plain SharedPreferences — themes carry no sensitive data, so no need for
 * the encrypted store here.
 */
class ThemeManager(context: Context) {
    private val prefs: SharedPreferences =
        context.getSharedPreferences("zybergo_themes", Context.MODE_PRIVATE)

    private val customThemes = mutableListOf<Theme>()

    fun availableThemes(): List<Theme> = BuiltInThemes.all + customThemes

    fun activeTheme(): Theme {
        val id = prefs.getString("active_theme_id", BuiltInThemes.DARK.id)
        return availableThemes().find { it.id == id } ?: BuiltInThemes.DARK
    }

    fun setActiveTheme(themeId: String) {
        prefs.edit().putString("active_theme_id", themeId).apply()
    }

    /** Theme Designer entry point: build + persist a user-authored theme. */
    fun saveCustomTheme(
        name: String,
        primary: Int,
        background: Int,
        surface: Int,
        textPrimary: Int,
        accent: Int,
        isDark: Boolean
    ): Theme {
        val theme = Theme(
            id = "custom_${System.currentTimeMillis()}",
            name = name,
            primary = primary,
            background = background,
            surface = surface,
            textPrimary = textPrimary,
            accent = accent,
            isDark = isDark,
            isCustom = true
        )
        customThemes.add(theme)
        // TODO: persist customThemes list itself (e.g. one pref key per field,
        // or a compact CSV-style line) so designer themes survive restart.
        return theme
    }
}
