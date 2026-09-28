package com.zybergo.browser

import android.app.Activity
import android.os.Bundle
import android.view.Gravity
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.LinearLayout
import androidx.core.view.isVisible

class MainActivity : Activity() {

    private lateinit var app: ZyberGoApplication
    private lateinit var webViewContainer: FrameLayout
    private lateinit var urlBar: EditText

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        app = application as ZyberGoApplication

        val root = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL }

        urlBar = EditText(this).apply {
            hint = "Search or type a URL"
            setOnEditorActionListener { _, _, _ ->
                loadCurrentInput()
                true
            }
        }
        root.addView(urlBar)

        webViewContainer = FrameLayout(this).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, 0, 1f
            )
        }
        root.addView(webViewContainer)

        setContentView(root)

        if (app.tabManager.tabCount == 0) {
            app.tabManager.newTab()
        }
        app.tabManager.switchTo(app.tabManager.activeTab()!!.id, webViewContainer)
    }

    private fun loadCurrentInput() {
        val input = urlBar.text.toString().trim()
        if (input.isEmpty()) return
        val url = if (input.contains(".") && !input.contains(" ")) {
            if (!input.startsWith("http")) "https://$input" else input
        } else {
            "https://www.google.com/search?q=${android.net.Uri.encode(input)}"
        }
        app.tabManager.activeTab()?.webView?.loadUrl(url)
    }

    override fun onPause() {
        super.onPause()
        // Background-safe: freeze inactive tabs immediately, matching the
        // memory-pressure behavior in ZyberGoApplication.onTrimMemory.
        app.tabManager.freezeAllInactiveTabs()
    }
}
