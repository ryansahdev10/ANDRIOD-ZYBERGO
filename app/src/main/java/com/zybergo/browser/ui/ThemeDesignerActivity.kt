package com.zybergo.browser.ui

import android.app.Activity
import android.graphics.Color
import android.os.Bundle
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.SeekBar
import android.widget.TextView
import com.zybergo.browser.core.ThemeManager

/**
 * Minimal, dependency-free theme designer: one slider trio (R/G/B) per
 * color role, built with plain Android widgets — no color-picker library
 * dependency, keeping the "no extra libs" RAM discipline from the rest of
 * the app. Swap in a nicer picker widget later without touching ThemeManager.
 */
class ThemeDesignerActivity : Activity() {

    private lateinit var themeManager: ThemeManager

    private var primary = Color.rgb(60, 60, 90)
    private var background = Color.rgb(20, 20, 35)
    private var surface = Color.rgb(35, 35, 65)
    private var textPrimary = Color.WHITE
    private var accent = Color.rgb(108, 99, 255)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        themeManager = ThemeManager(this)

        val root = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL; setPadding(32, 32, 32, 32) }

        val nameInput = EditText(this).apply { hint = "Theme name" }
        root.addView(nameInput)

        val preview = TextView(this).apply {
            text = "Preview"
            setPadding(24, 48, 24, 48)
            textSize = 18f
        }
        root.addView(preview)

        fun refreshPreview() {
            preview.setBackgroundColor(background)
            preview.setTextColor(textPrimary)
        }
        refreshPreview()

        // One RGB slider group per role, wired generically.
        val roles = listOf(
            "Primary" to { c: Int -> primary = c },
            "Background" to { c: Int -> background = c; refreshPreview() },
            "Surface" to { c: Int -> surface = c },
            "Text" to { c: Int -> textPrimary = c; refreshPreview() },
            "Accent" to { c: Int -> accent = c }
        )

        roles.forEach { (label, onChange) ->
            root.addView(TextView(this).apply { text = label })
            var r = 128; var g = 128; var b = 128
            listOf("R", "G", "B").forEachIndexed { i, channel ->
                val seek = SeekBar(this).apply {
                    max = 255
                    progress = 128
                    setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
                        override fun onProgressChanged(sb: SeekBar?, progress: Int, fromUser: Boolean) {
                            when (i) { 0 -> r = progress; 1 -> g = progress; 2 -> b = progress }
                            onChange(Color.rgb(r, g, b))
                        }
                        override fun onStartTrackingTouch(sb: SeekBar?) {}
                        override fun onStopTrackingTouch(sb: SeekBar?) {}
                    })
                }
                root.addView(seek)
            }
        }

        val saveButton = android.widget.Button(this).apply {
            text = "Save Theme"
            setOnClickListener {
                val name = nameInput.text.toString().ifBlank { "My Theme" }
                themeManager.saveCustomTheme(
                    name = name,
                    primary = primary,
                    background = background,
                    surface = surface,
                    textPrimary = textPrimary,
                    accent = accent,
                    isDark = isColorDark(background)
                )
                finish()
            }
        }
        root.addView(saveButton)

        setContentView(root)
    }

    private fun isColorDark(color: Int): Boolean {
        val luminance = (0.299 * Color.red(color) + 0.587 * Color.green(color) + 0.114 * Color.blue(color))
        return luminance < 128
    }
}
