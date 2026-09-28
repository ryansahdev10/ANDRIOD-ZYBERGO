package com.zybergo.browser.core

import android.graphics.Color
import android.webkit.WebResourceRequest
import android.webkit.WebResourceResponse
import android.webkit.WebView
import android.webkit.WebViewClient
import java.io.ByteArrayInputStream

class BrowserWebViewClient(private val adBlocker: AdBlocker) : WebViewClient() {

    override fun shouldInterceptRequest(
        view: WebView,
        request: WebResourceRequest
    ): WebResourceResponse? {
        val host = request.url.host ?: return super.shouldInterceptRequest(view, request)
        if (adBlocker.isBlocked(host)) {
            // Return an empty response instead of null: null lets some servers/CDNs
            // retry or fall back to alternate hosts, empty response starves it cleanly.
            return WebResourceResponse(
                "text/plain",
                "utf-8",
                ByteArrayInputStream(ByteArray(0))
            )
        }
        return super.shouldInterceptRequest(view, request)
    }

    override fun onPageFinished(view: WebView, url: String?) {
        super.onPageFinished(view, url)
        // Hook point: reader-mode readability extraction would inject its JS here.
    }
}
