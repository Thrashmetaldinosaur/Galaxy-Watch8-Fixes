# Migration and private backup guide

This guide is deliberately generic. Keep account information and binary backups **off public GitHub**.

## What can be preserved from a working watch

- The exact installed ReVanced YouTube APK and modified GmsCore APK, acquired via `adb shell pm path PACKAGE` and `adb pull` (for an authorized personal device).
- Original source APK, official patch bundle/CLI, **private local** keystore, build scripts, and APK signatures/hashes.
- ReVanced's user-accessible **settings JSON**, exported from the in-app ReVanced settings Import/Export page. Preserve the file privately and review its contents before sharing.
- A snapshot of the display configuration: `adb shell wm density`, `adb shell wm size`, and font scale.
- Local notes identifying the precise patched build and compatible signing certificate.

**Limits:** Without root, ADB generally cannot retrieve another app's private preference database, Google account tokens, or full signed-in session. Copying an APK is not a complete application-data backup. A JSON file previously imported or prepared by the user is not necessarily an export of the *current* live preferences. Re-authenticate normally on the replacement watch.

## Safe restore sequence

1. Save all private backups and keys on trusted storage. Never put a signing keystore, password, refresh token, logcat, ADB serial, or raw account data in this repository.
2. On the replacement watch, enable Developer options / Wireless debugging. Pair ADB and confirm the device with `adb devices`.
3. Install your **matching, privately backed-up** GmsCore APK (only if absent or the signing certificate permits updating). Do not uninstall an existing signed-in GmsCore casually; uninstalling deletes its private account data.
4. Install the privately backed-up custom ReVanced YouTube APK with `adb -s YOUR_WATCH_ADB_SERIAL install -r "PATH_TO_PRIVATE_BACKUP.apk"`.
5. Reapply the chosen watch-wide density if desired, e.g. `adb -s YOUR_WATCH_ADB_SERIAL shell wm density 323`. Reset to OEM default with `adb shell wm density reset`.
6. In ReVanced Settings, use its Import/Export settings interface to import your saved JSON, then check settings individually. Settings keys depend on the patch version.
7. Sign in normally, verify playback and app layout, and only then consider running the profile optimization script when the battery is sufficiently charged.

**The per-app 170-DPI hook and suppressed GmsCore battery warning are properties of the private custom ReVanced APK, not effects of the public optimizer script.** You cannot reproduce them by changing only `wm density`.

## Exact package identities from the tested configuration

- ReVanced YouTube: `app.revanced.android.youtube`, YouTube base version 20.40.45.
- ReVanced GmsCore: `app.revanced.android.gms`, GmsCore 0.3.13.2.250932.
- Stock YouTube: separate package `com.google.android.youtube`; leave installed.
- Google Play services: `com.google.android.gms`; leave installed.

## Privacy review checklist

Before publishing any project files, review file content, metadata, file paths, README examples and Git history for names, home directories, usernames, IP/MAC addresses, serials, account data, credentials, unique device IDs and raw diagnostics. Keep customized signing material, user preference exports, personal device APKs, and private debugging artifacts in a separate, excluded local directory.

## Performance caveat

One before/after cold-launch measurement improved from 9.8 seconds to 3.183 seconds using `speed-profile` compilation. This optimization is reversible through standard Android package lifecycle operations and may be regenerated after reinstalling. Avoid repeatedly forcing `speed` compilation on the watch; it was expensive and contributed to heat and battery drain in the tested session.
