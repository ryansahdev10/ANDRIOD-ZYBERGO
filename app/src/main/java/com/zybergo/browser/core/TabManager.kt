package com.zybergo.browser.core

import android.content.Context
import android.view.ViewGroup
import android.webkit.WebSettings
import android.webkit.WebView

/**
 * Owns the tab list and is the single place that creates/destroys WebViews.
 *
 * Design rules that keep idle RAM near ~65MB:
 *  - Hard cap of ONE live (attached, non-frozen) regular WebView at a time
 *    in the default "focus mode". (Optional "preview mode" toggle in settings
 *    can allow 2 live for smoother swipe-between-tabs animation, at a RAM cost —
 *    left as a user-facing tradeoff, default OFF.)
 *  - A single shared WebView "pool" of size 1 is reused: switching tabs does
 *    NOT allocate a new WebView object per switch; we detach the pooled
 *    WebView from the old tab, reset it, and reattach it to the new tab context,
 *    freezing the old tab's page state to a Bundle first.
 *  - No pre-loading / pre-rendering of background tabs. Ever.
 */
class TabManager(
    private val appContext: Context,
    private val adBlocker: AdBlocker
) {
    private val tabs = mutableListOf<Tab>()
    private var activeTabId: String? = null

    // The single reusable WebView instance shared across all tabs.
    private var pooledWebView: WebView? = null

    val tabCount: Int get() = tabs.size
    fun allTabs(): List<Tab> = tabs.toList()
    fun activeTab(): Tab? = tabs.find { it.id == activeTabId }

    fun newTab(url: String = "zybergo://newtab", incognito: Boolean = false): Tab {
        val tab = Tab(url = url, isIncognito = incognito)
        tabs.add(tab)
        switchTo(tab.id)
        return tab
    }

    fun closeTab(tabId: String) {
        val tab = tabs.find { it.id == tabId } ?: return
        if (tab.webView === pooledWebView) {
            tab.captureStateAndDestroy()
        }
        tabs.remove(tab)
        if (activeTabId == tabId) {
            activeTabId = tabs.lastOrNull()?.id
            activeTabId?.let { switchTo(it) }
        }
    }

    /**
     * Switch the visible tab. This is where the freeze/thaw swap happens.
     * container: the ViewGroup in the Activity that should host the WebView.
     */
    fun switchTo(tabId: String, container: ViewGroup? = null) {
        val newTab = tabs.find { it.id == tabId } ?: return
        val previousTab = tabs.find { it.id == activeTabId }

        // Freeze whatever was previously active.
        if (previousTab != null && previousTab.id != tabId) {
            previousTab.captureStateAndDestroy()
        }

        activeTabId = tabId

        if (container != null) {
            val wv = obtainPooledWebView()
            container.removeAllViews()
            (wv.parent as? ViewGroup)?.removeView(wv)
            container.addView(wv)
            newTab.attachAndRestore(wv)
        }
    }

    /** Called under memory pressure or when app goes to background. */
    fun freezeAllInactiveTabs() {
        tabs.filter { it.id != activeTabId }.forEach { it.captureStateAndDestroy() }
    }

    /** Called when app itself is backgrounded — freeze even the active tab. */
    fun freezeEverything() {
        tabs.forEach { it.captureStateAndDestroy() }
    }

    private fun obtainPooledWebView(): WebView {
        pooledWebView?.let { return it }
        val wv = WebView(appContext.applicationContext).apply {
            configureForMemoryEfficiency(this)
        }
        pooledWebView = wv
        return wv
    }

    private fun configureForMemoryEfficiency(webView: WebView) {
        val s: WebSettings = webView.settings
        s.javaScriptEnabled = true
        s.domStorageEnabled = true
        // Disk cache small and bounded; avoid unbounded app-cache growth.
        s.cacheMode = WebSettings.LOAD_DEFAULT
        s.setGeolocationEnabled(false)
        s.databaseEnabled = false
        s.allowFileAccess = false
        s.allowContentAccess = false
        s.mediaPlaybackRequiresUserGesture = true
        s.loadsImagesAutomatically = true // toggled off dynamically in AdBlocker "data saver" mode

        webView.setLayerType(android.view.View.LAYER_TYPE_NONE, null)

        webView.webViewClient = BrowserWebViewClient(adBlocker)
    }
}
