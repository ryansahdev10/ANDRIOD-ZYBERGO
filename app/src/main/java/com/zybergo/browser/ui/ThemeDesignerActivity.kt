package com.zybergo.browser.ui

import android.app.Activity
import android.graphics.Color
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.SeekBar
import android.widget.TextView
import com.zybergo.browser.core.ThemeManager

/** A small dependency-free RGB theme editor. */
class ThemeDesignerActivity : Activity() {
    private lateinit var themeManager: ThemeManager

    private var primaryColor = Color.rgb(60, 60, 90)
    private var backgroundColor = Color.rgb(20, 20, 35)
    private var surfaceColor = Color.rgb(35, 35, 65)
    private var textPrimaryColor = Color.WHITE
    private var accentColor = Color.rgb(108, 99, 255)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        themeManager = ThemeManager(this)

        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(32, 32, 32, 32)
        }
        val nameInput = EditText(this).apply { hint = "Theme name" }
        root.addView(nameInput)

        val preview = TextView(this).apply {
            text = "Preview"
            setPadding(24, 48, 24, 48)
            textSize = 18f
        }
        root.addView(preview)

        fun refreshPreview() {
            preview.setBackgroundColor(backgroundColor)
            preview.setTextColor(textPrimaryColor)
        }
        refreshPreview()

        val roles: List<Pair<String, (Int) -> Unit>> = listOf(
            "Primary" to { color: Int -> primaryColor = color },
            "Background" to { color: Int -> backgroundColor = color; refreshPreview() },
            "Surface" to { color: Int -> surfaceColor = color },
            "Text" to { color: Int -> textPrimaryColor = color; refreshPreview() },
            "Accent" to { color: Int -> accentColor = color }
        )

        roles.forEach { (label, onChange) ->
            root.addView(TextView(this).apply { text = label })
            var red = 128
            var green = 128
            var blue = 128
            repeat(3) { channel ->
                root.addView(SeekBar(this).apply {
                    max = 255
                    progress = 128
                    setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
                        override fun onProgressChanged(
                            seekBar: SeekBar?, progress: Int, fromUser: Boolean
                        ) {
                            when (channel) {
                                0 -> red = progress
                                1 -> green = progress
                                else -> blue = progress
                            }
                            onChange(Color.rgb(red, green, blue))
                        }

                        override fun onStartTrackingTouch(seekBar: SeekBar?) = Unit
                        override fun onStopTrackingTouch(seekBar: SeekBar?) = Unit
                    })
                })
            }
        }

        root.addView(Button(this).apply {
            text = "Save Theme"
            setOnClickListener {
                val name = nameInput.text.toString().trim().ifEmpty { "My Theme" }
                // Pass the five ARGB values explicitly. Do not pass Android Drawable
                // values here: ThemeManager stores colors as Int ARGB values.
                themeManager.saveCustomTheme(
                    name,
                    primaryColor,
                    backgroundColor,
                    surfaceColor,
                    textPrimaryColor,
                    accentColor,
                    isColorDark(backgroundColor)
                )
                finish()
            }
        })
        setContentView(root)
    }

    private fun isColorDark(color: Int): Boolean {
        val luminance = 0.299 * Color.red(color) +
            0.587 * Color.green(color) +
            0.114 * Color.blue(color)
        return luminance < 128.0
    }
}
