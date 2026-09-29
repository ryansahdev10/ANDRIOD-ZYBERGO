# ZyberGo — Android

Ported from your real `ZyberGo-source-2.0.0` desktop source (Electron), not
reverse-engineered from the installer. Your actual `renderer/*.html` and
`renderer/*.js` files are reused unmodified inside the app; only the native
shell around them (Kotlin, `dev.zybergo.browser`) is new.

## How this actually works
Electron apps are Chromium + Node.js running your HTML/JS/CSS with a
`preload.js` bridge exposing `window.zyber.*`. Android's WebView has the
Chromium part but no Node, so:
- Your pages (`newtab`, `history`, `bookmarks`, `downloads`, `settings`,
  `about`, `passwords`, `game`, `games`, `games-2048`, `games-tictactoe`,
  `setup`, `lock`) ship as-is under `app/src/main/assets/renderer/`, served
  from `https://appassets.androidplatform.net/` via `WebViewAssetLoader`
  (never `file://`, which real Chrome/WebView blocks from calling native code).
- `zyber-bridge.js` replaces `preload.js`: it rebuilds the exact same
  `window.zyber.*` API using `WebViewCompat.addWebMessageListener`, restricted
  to that one origin — an ordinary website loaded in the same WebView can
  never see or call it.
- `Bridge.kt` replaces the `ipcMain.handle(...)` block in `main.js`, channel
  for channel, with the same argument/return shapes.
- `Stores.kt` replaces `settings.js`, `local-login.js`, `history-db.js`, and
  `vault-db.js`. Same defaults, same SQLite columns. Vault passwords are
  encrypted with an AES key held in the Android Keystore (hardware-backed
  where the device supports it) instead of the desktop's OS keychain calls.
- `MainActivity.kt` is the part with no desktop equivalent: the actual
  browser chrome (URL bar, tabs, menu) — Electron's `BrowserWindow` +
  `BrowserView` tab strip doesn't exist on Android, so this is a plain
  `WebView` with a native toolbar built directly on framework widgets (no
  AppCompat/Material) to keep it light.

## What ported faithfully
- Search engines, avatars, defaults — straight port of `src/settings.js`.
- `resolveNavTarget()` (URL vs. shortcut vs. search) — same rules.
- Theme colors — read from your `themes.css` custom-property values.
- Tab-position "Game mode" theme hand-off (`themeBeforeGameMode`) — same logic.
- History/bookmarks/downloads/vault schemas — same tables and fields.
- HTTPS-only upgrade, third-party cookie blocking, clear-on-exit — same
  settings, applied through WebView's equivalent APIs.
- Autofill: same form-filling script as `vault:fill` in `main.js`, triggered
  from the menu instead of a extension-style per-field icon (Android WebView
  has no per-field UI hook to attach that to).

## New since the last build
- **Real sync** (bookmarks + history across your own devices): `SyncManager.kt`
  pairs devices with a short code, merges changes last-write-wins by
  timestamp, and talks to a self-hostable server in `sync-server/` (Node,
  ~130 lines, file-based storage, no account system — the code itself is
  the credential, so treat it like a password and run it over HTTPS). Full
  UI is in Settings → Sync. Deployment instructions are in
  `sync-server/README.md` (Render/Railway/Fly, all free tiers). This wasn't
  in your desktop source at all — it's new.
- **Theme Designer bug fix + polish**: found and fixed a real bug where a
  custom theme's derived colors (the soft highlight, accent-tinted text,
  etc.) silently stayed hardcoded blue no matter what accent color was
  picked, because those four values were never actually saved. They're now
  properly derived from your chosen colors and persisted. Also added: a
  live WCAG contrast checker, 6 one-tap starter presets, and a mobile-usable
  modal (the old one was a fixed 460px width with no phone handling at all).

## What's adapted, not ported
- **Ad/tracker blocking**: your desktop engine (`zlaser.js` +
  `assets/zlaser-engine.bin`) is a serialized `@ghostery/adblocker` engine —
  full filter-list syntax, cosmetic rules, the works. That library needs
  Node; WebView has no such hook. `AdBlocker.kt` here is a flat hostname
  `HashSet` instead — blocks whole ad/tracker domains, not path-level or
  cosmetic rules. See `tools/README.md` for how to build a real list for it.
- **Reader mode**: your desktop reader mode leans on a Readability-style
  extraction library that only exists as a dev-time build step, not a
  shippable asset. `MainActivity.kt`'s `READER_JS` is a small heuristic I
  wrote instead (picks the element with the best text-to-markup ratio) —
  works on most article pages, less robust than a real Readability port.
- **Tab strip**: desktop shows real tab thumbnails in a horizontal strip;
  Android shows a full-screen tab switcher list (closer to how Chrome/Firefox
  do it on phones) since Electron's `BrowserView`-per-tab model doesn't map
  onto a single mobile WebView the same way.
- **Auto-update**: the desktop build polls your GitHub releases; the Android
  build doesn't phone home, so Settings shows "not available on Android"
  rather than a real update check.
- **Avatar photo upload**: stubbed with a message — wiring a real image
  picker + crop UI is straightforward to add but wasn't built here.

## RAM behavior
Same principle as before: one shared WebView, saved/restored per tab via
`WebView.saveState()`/`restoreState()`, and the WebView is torn down
entirely a few minutes after the app leaves the foreground (your existing
`tabDiscardMinutes` setting controls the delay) or immediately under system
memory pressure (`onTrimMemory`).

## Getting the .apk without installing anything
This container has no Android SDK and no network access, so I can't compile
it here. The included `.github/workflows/build-apk.yml` builds it for you on
GitHub's own servers:
1. Create a free GitHub repo, upload this whole folder (keep the folder
   structure) — this can be done entirely from a phone browser.
2. Commit — GitHub starts the build automatically.
3. Actions tab → the run → Artifacts → download **ZyberGo-debug-apk**.
4. Unzip (Files app handles it) and tap the `.apk` to install (allow
   "install unknown apps" once, when prompted).
