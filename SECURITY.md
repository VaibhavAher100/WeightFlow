# Security Policy

## Reporting a Vulnerability

**Do not open a public GitHub issue for security vulnerabilities.**

If you find a security issue in WeightFlow, please email **vaibhavaher100@gmail.com** with:

- Description of the vulnerability
- Steps to reproduce
- Potential impact
- Your name / affiliation (optional)

We will acknowledge within 48 hours and work on a fix promptly.

---

## Security Practices

WeightFlow follows these security principles:

### Database Encryption at Rest
- Room database encrypted with **SQLCipher 4** (AES-256-CBC + HMAC-SHA2 integrity)
- Encryption key is a 256-bit AES key generated on first launch and stored permanently in the **Android Keystore** (hardware-backed on supporting devices)
- The raw key material never leaves the Keystore secure enclave — only an in-process handle is used
- Existing plaintext databases are automatically migrated to encrypted files on first upgrade — no data loss, no user action required
- DataStore preferences are protected by the OS app sandbox (not SQLCipher — they contain only non-sensitive settings like theme and unit preference)

### Local-First Architecture
- All user data (weight entries, preferences, profile) stored locally on-device in app-private storage
- No cloud backend or remote servers, no analytics SDKs, no tracking
- Auto Backup excluded for `weightflow.db`, DataStore, and SharedPreferences
- Device-to-device transfer excluded (users migrate via CSV export)
- On factory reset or uninstall, all app-private data is removed by the OS

### Network Security
- The app declares **no `INTERNET` permission** in `AndroidManifest.xml` — the process cannot open sockets
- Cleartext traffic is disabled via `network_security_config.xml` (`cleartextTrafficPermitted="false"`)
- No certificate pinning is configured because there are no production network endpoints. If networking is added in a future phase, pinning will be evaluated at that time.

### Release Build Hardening
- `isMinifyEnabled = true` and `isShrinkResources = true` for the `release` build type
- R8 obfuscation via `proguard-android-optimize.txt` + project `proguard-rules.pro`
- Release signing enforced — `assembleRelease` fails if signing credentials are not configured

### Permissions
- Declared permissions limited to: `POST_NOTIFICATIONS`, `VIBRATE`
- No `INTERNET`, `ACCESS_NETWORK_STATE`, camera, contacts, location, microphone, or storage permissions
- `POST_NOTIFICATIONS` is requested in-context from the Settings reminder toggle

### Authentication & Authorization
- Single-user app; no account, no credentials, no auth system (data is local-only)

### Supply Chain / Dependencies
- GitHub Actions CI runs `actions/dependency-review-action` on every pull request to flag new vulnerable or non-permissive dependencies before merge
- Dependabot is enabled (`.github/dependabot.yml`) for weekly Gradle and GitHub Actions updates
- All GitHub Actions are pinned to full commit SHAs (not mutable tags)
- See `gradle/libs.versions.toml` and `app/build.gradle.kts` for the full dependency set

---

### CSV Export Security

WeightFlow offers three export formats. Their security properties are documented here.

#### Format 1 — Plaintext CSV
Full weight history as a plain UTF-8 CSV. No encryption. Suitable for import into spreadsheet apps. The file contains all weight entries and the selected unit column. A confirmation dialog warns the user before the system file picker is launched.

#### Format 2 — Encrypted ZIP (AES-256)
The CSV is placed inside a WinZip AES v2 (AES-256) encrypted ZIP file.

Technical properties:
- Encryption: AES-256, WinZip AES v2 (`EncryptionMethod.AES`, `AesKeyStrength.KEY_STRENGTH_256`, `AesVersion.TWO`)
- No ZipCrypto fallback — ZipCrypto is known to be weak and is explicitly disabled
- Password is handled as a `CharArray`; the array is overwritten with spaces immediately after the zip4j call completes, whether the export succeeded or failed
- Password is never stored in DataStore, SharedPreferences, or any durable storage
- Password is never copied to the clipboard (no convenience shortcut is provided)
- Minimum password length: 12 characters, enforced before any file I/O begins
- After writing, the ZIP is re-opened with the password to verify the inner CSV CRC — a corrupted or partially-written file is deleted before success is reported
- File size estimate uses `csv_bytes * 0.5` and is labeled "approximate" in the UI

**Known limitation — ZIP central directory leakage:**
The ZIP central directory (the file's metadata index) is not encrypted. This means the filename of the inner CSV (`weightflow_export_YYYY-MM-DD.csv`) and its uncompressed size are visible to anyone who has the ZIP file, even without the password. The file *contents* remain encrypted. This is a structural property of the ZIP format and is documented as an accepted risk for v1.

**Compatibility:**
AES-encrypted ZIPs are readable by: 7-Zip, WinRAR, The Unarchiver (macOS).
**macOS Archive Utility does not support AES-encrypted ZIPs** — it silently fails or shows a corrupted-file error. This limitation is documented in the in-app export dialog.

#### Format 3 — Minimal CSV
Date and weight_kg columns only. Profile data, goal data, and notes are omitted.

**Privacy note:** A date+weight time-series is **quasi-identifying**. When combined with external information — such as a known weigh-in event, a distinctive starting weight, or a known date range — it may be linked back to an individual. The UI labels this format "Minimal CSV" (not "Anonymous") and includes a warning to this effect. Users should treat this file with the same care as the plaintext export.

---

## Known Limitations

1. **CSV import robustness:** Custom CSV parser may not handle every malformed file gracefully. A 5 MB file size limit is enforced.
2. **DataStore not encrypted:** `UserPrefsDataStore` stores non-sensitive settings (theme, unit preference, reminder toggle) as plaintext. Full DataStore encryption can be added in a future hardening pass if the threat model requires it.
3. **ZIP central directory not encrypted:** The filename and size of the inner CSV are visible in an AES-encrypted ZIP without the password. See the CSV Export Security section above.
4. **zip4j dependency:** `net.lingala.zip4j:zip4j:2.11.5` is added for AES-ZIP support. A full supply-chain audit (CVE scan, license review) is pending before the first production release.

---

## Questions or Suggestions?

For non-security questions or feature requests, open a GitHub issue or discussion.

For security hardening suggestions, email vaibhavaher100@gmail.com.
