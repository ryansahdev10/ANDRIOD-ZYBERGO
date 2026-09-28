# ZyberGo — Android (Kotlin)

Rebuilt from your feature spec since the original source was lost and the
.exe installer can't be reverse-engineered into usable source.

## Built and working in this scaffold
- **TabManager**: single-WebView pool, freezes (destroys + saves state)
  every tab except the active one. This is the main lever behind the ~65MB
  idle target — background tabs cost KBs (saved Bundle), not MBs (live WebView).
- **AdBlocker**: hostname HashSet blocker wired into `shouldInterceptRequest`.
- **LocalAccountManager**: local password gate via Android Keystore +
  EncryptedSharedPreferences + PBKDF2. No network, no cloud account.
- **ThemeManager**: 4 built-in themes + `saveCustomTheme()` entry point for
  the theme designer (color values only, no per-theme assets).
- **TicTacToeAI**: full minimax, genuinely unbeatable.

## Stubbed / architecture-only (needs your input to finish)
- **Snake, 2048**: not implemented yet — same `games/` package pattern as
  Tic-Tac-Toe, plug in a Canvas-based or simple View-based game loop.
- **Theme Designer UI**: `ThemeManager.saveCustomTheme()` exists; needs a
  color-picker screen calling it, plus persistence of the custom list
  (currently in-memory only — noted with a TODO in the file).
- **Custom sync backend**: needs your server API contract. TabManager/
  bookmarks would serialize to whatever payload your backend expects.
- **Download manager + scanning**: needs a virus-scan API or on-device
  heuristic you choose (e.g. hash against a known-bad list, or a vendor SDK).
- **Reader mode**: hook point marked in `BrowserWebViewClient.onPageFinished`;
  typically implemented via injecting a Readability.js-style content
  extraction script and re-rendering into a plain text view.
- **Vertical tabs / tab groups UI**: `TabManager` already supports arbitrary
  tab count and ordering; only the RecyclerView-based tab strip UI is missing.
- **Custom NTP with widgets**: needs a widget framework decision (fixed set
  of native widgets vs. a mini plugin system).

## Why not a literal Chromium build?
A full Chromium-for-Android build (like Brave/Vivaldi do) is a multi-GB
source checkout, multi-hour builds, and a dedicated build-infra investment —
not something scaffolded in a single project. This WebView-based approach
reuses the Chromium engine already on the device (Android's System WebView
*is* Chromium) and gets you 90% of the behavior with a fraction of the
maintenance burden. If you outgrow WebView (e.g. need custom V8 flags), that's
a deliberate later migration, not a starting point.
