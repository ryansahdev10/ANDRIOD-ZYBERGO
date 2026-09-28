package com.zybergo.browser.core

import android.os.Bundle
import android.webkit.WebView
import java.util.UUID

/**
 * A single browser tab.
 *
 * The key memory decision: `webView` is nullable. Only the active tab (and
 * maybe one adjacent tab during a swipe transition) has a non-null, attached
 * WebView. Every other tab holds `savedState` (a small Bundle produced by
 * WebView.saveState()) which is cheap — typically a few KB — instead of a
 * full WebView instance (~8-15MB+ each once a page is loaded, more with
 * images/JS heavy pages).
 */
class Tab(
    val id: String = UUID.randomUUID().toString(),
    var title: String = "New Tab",
    var url: String = "zybergo://newtab",
    var faviconUrl: String? = null,
    var isIncognito: Boolean = false
) {
    var webView: WebView? = null
        internal set

    private var savedState: Bundle? = null

    val isFrozen: Boolean
        get() = webView == null

    internal fun captureStateAndDestroy() {
        val wv = webView ?: return
        val bundle = Bundle()
        wv.saveState(bundle)
        savedState = bundle
        wv.stopLoading()
        wv.onPause()
        wv.clearHistory()
        wv.removeAllViews()
        wv.destroy()
        webView = null
    }

    internal fun attachAndRestore(newWebView: WebView) {
        webView = newWebView
        val state = savedState
        if (state != null) {
            newWebView.restoreState(state)
        } else if (url != "zybergo://newtab") {
            newWebView.loadUrl(url)
        }
        newWebView.onResume()
    }
}
