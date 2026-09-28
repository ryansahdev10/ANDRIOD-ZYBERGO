package com.zybergo.browser

import android.app.Application
import android.content.ComponentCallbacks2
import com.zybergo.browser.core.AdBlocker
import com.zybergo.browser.core.TabManager

/**
 * Root Application object.
 *
 * RAM strategy overview (target: ~65MB resident at idle, single blank tab):
 *  1. No dependency-injection framework spinning up graphs at startup.
 *  2. AdBlocker rule set is loaded once, lazily, as a single HashSet<String> of
 *     hostnames rather than a heavier trie/regex engine (regex engines retain
 *     large compiled-pattern memory; hostname hash lookup is O(1) and tiny).
 *  3. TabManager freezes (destroys the WebView, keeps only serialized state)
 *     any tab that isn't the currently visible one. Idle with 1 tab open means
 *     exactly 1 live WebView instance in the process.
 *  4. We respond to onTrimMemory aggressively: at the first sign of system
 *     memory pressure we evict every background tab's WebView immediately,
 *     even before Android would force us to.
 */
class ZyberGoApplication : Application() {

    lateinit var tabManager: TabManager
        private set

    lateinit var adBlocker: AdBlocker
        private set

    override fun onCreate() {
        super.onCreate()
        adBlocker = AdBlocker(this)
        tabManager = TabManager(this, adBlocker)
    }

    override fun onTrimMemory(level: Int) {
        super.onTrimMemory(level)
        when (level) {
            ComponentCallbacks2.TRIM_MEMORY_RUNNING_MODERATE,
            ComponentCallbacks2.TRIM_MEMORY_RUNNING_LOW,
            ComponentCallbacks2.TRIM_MEMORY_RUNNING_CRITICAL,
            ComponentCallbacks2.TRIM_MEMORY_UI_HIDDEN,
            ComponentCallbacks2.TRIM_MEMORY_BACKGROUND,
            ComponentCallbacks2.TRIM_MEMORY_MODERATE,
            ComponentCallbacks2.TRIM_MEMORY_COMPLETE -> {
                tabManager.freezeAllInactiveTabs()
            }
        }
    }
}
