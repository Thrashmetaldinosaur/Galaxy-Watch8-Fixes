# Galaxy Watch 8 — ReVanced performance and migration notes

Technical notes for running a separate YouTube ReVanced installation on a Samsung Galaxy Watch 8 (Wear OS). This repository intentionally does **not** contain proprietary YouTube APKs, modified APK binaries, signing material, account data, identifiers, or private debug logs.

## Confirmed performance fix (November 2026)

On a tested Galaxy Watch 8, YouTube ReVanced 20.40.45 showed intermittent slow startup and Android process terminations. Android's package dexopt report indicated `run-from-apk` before optimization. A forced **full-speed** compilation was excessively resource-intensive on the watch; do not use it as the default.

The successful, lightweight operation was:

```sh
adb -s YOUR_WATCH_ADB_SERIAL shell cmd package compile -m speed-profile --primary-dex app.revanced.android.youtube
```

Android returned `Success`. `dumpsys package dexopt` then reported `status=speed-profile` for the app's primary APK. One controlled cold launch improved from **9.8 seconds to 3.183 seconds** (~68% faster) as reported by Android Activity Manager. This is one-device, single-run evidence, not a universal performance guarantee.

### Safe run

1. Charge the watch sufficiently and ensure it is not hot. The tested watch's battery was especially low, so treat lengthy compiler tasks cautiously.
2. Enable Wireless debugging in Developer options, pair ADB, and identify the current address/port. Do not publish these details.
3. Run [Optimize-ReVanced-Watch8.ps1](Optimize-ReVanced-Watch8.ps1) in PowerShell, supplying the ADB device serial.
4. Confirm the compiler reports success and dexopt status changes to `speed-profile`.
5. Test launch and playback manually. The script does **not** delete app data, alter density, or touch account state.

### Observed issues and limitations

- Android recorded a boot-time **ANR** involving `com.google.android.libraries.notifications.platform.entrypoints.restart.RestartReceiver`. Attempting to disable the receiver with unprivileged ADB failed with `SecurityException`. This receiver is **not** disabled by the fix.
- Some background processes were reclaimed by Android's memory manager. A background process kill is not necessarily a foreground app crash.
- The dexopt report also referenced stale secondary dex cache paths after updates. This alone is not proof of corrupt app data. Do not clear app data as a first-line fix.
- The speed-profile optimization improves launch latency; it does not guarantee that every ReVanced patch, background notification, or video playback feature will remain crash-free.
- Never run a forced full-speed compilation repeatedly on a hot or nearly depleted watch.

## Watch-specific display configuration (tested)

| Scope | Value | Mechanism |
| --- | ---: | --- |
| Stock system density | 340 DPI | Device baseline |
| Tested system-wide density | 323 DPI | `adb shell wm density 323` |
| YouTube ReVanced app-only density | 170 DPI | Custom patch in the locally built APK |
| System font scale | 1.0 | Unchanged |

**Important:** System-wide `wm density` is *not* app-specific. The tested ReVanced APK used an app-local Activity resource-density hook. Restoring the 170-DPI appearance requires the separately rebuilt, compatible APK; the performance command here does not recreate that patch.

The [original, standalone Activity resource-density helper source](patches/DensityPatch.java) is published for reference. It is **not** a drop-in installable patch: integrating it requires a compatible, locally built application and preserving the correct final Activity/Application lifecycle method. The custom build also suppressed a GmsCore battery-optimization *warning dialog* without changing system battery allowlists. This does **not** exempt GmsCore from battery controls.

## Restoring to another watch

See [MIGRATION.md](MIGRATION.md) for what can be preserved, the correct installation sequence, and limitations of non-root backups. A [sanitized watch-friendly ReVanced preference template](revanced-settings-watch-template.json) is provided for reference. It is **not a verified export of the live device state**; personal SponsorBlock usage counters were removed. Some patch-version-specific keys may not import successfully.

## Privacy and provenance

Only generic package names, version numbers, commands, measured timings and technical observations are published. No user names, local Windows paths, IP addresses, ADB serials, signing keys, account tokens, private settings exports or device logs are included. Upstream YouTube/ReVanced/GmsCore materials remain the property of their respective authors. No Google credentials should be copied between devices by these scripts.
