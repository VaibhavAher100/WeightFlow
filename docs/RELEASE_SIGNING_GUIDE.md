# Release Signing Guide

This guide covers setting up release signing for WeightFlow builds to Play Store.

## Quick Start (Local Development)

WeightFlow includes a hardened release signing validation system. Before building for release, ensure you have:

1. **Android Keystore file** (`.jks` or `.keystore`)
2. **Keystore password**
3. **Key alias** (the name inside the keystore)
4. **Key password** (the password for that specific key)

Add these to `local.properties` (at the WeightFlow root):

```properties
KEYSTORE_PATH=/Users/you/.android/release.jks
KEYSTORE_PASSWORD=your-keystore-password
KEY_ALIAS=your-key-alias
KEY_PASSWORD=your-key-password
```

Then build:

```bash
./gradlew assembleRelease
```

## Validation Behavior

Before any release build, `./gradlew assembleRelease` automatically runs `validateReleaseSigning` which:

1. Checks that `KEYSTORE_PATH` is set and the file exists on disk
2. Checks that `KEYSTORE_PASSWORD` is non-empty
3. Checks that `KEY_ALIAS` is non-empty
4. Checks that `KEY_PASSWORD` is non-empty

If **any** credential is missing, the build fails immediately with a clear error:

```
Release signing validation failed. Missing credentials:
  - storeFile (KEYSTORE_PATH)
  - storePassword (KEYSTORE_PASSWORD)

Provide via environment variables (CI) or local.properties (local dev):
  • KEYSTORE_PATH / KEYSTORE_PATH env
  • KEYSTORE_PASSWORD / KEYSTORE_PASSWORD env
  • KEY_ALIAS / KEY_ALIAS env
  • KEY_PASSWORD / KEY_PASSWORD env
```

### Running Validation Without Building

To test your signing setup without a full build:

```bash
./gradlew validateReleaseSigning
```

This completes in ~2 seconds vs. ~60 seconds for a full build.

## CI/GitHub Actions Setup

For automated releases, add four repository secrets to your GitHub repository:

**Settings → Secrets and variables → Actions** → add:

| Secret | Value |
|--------|-------|
| `KEYSTORE_PATH` | Absolute path to `.jks` file on runner, or keystore contents (base64-encoded) |
| `KEYSTORE_PASSWORD` | Keystore password |
| `KEY_ALIAS` | Key alias inside the keystore |
| `KEY_PASSWORD` | Key password |

Then in your workflow (e.g., `.github/workflows/release.yml`):

```yaml
- name: Build release APK
  env:
    KEYSTORE_PATH: ${{ secrets.KEYSTORE_PATH }}
    KEYSTORE_PASSWORD: ${{ secrets.KEYSTORE_PASSWORD }}
    KEY_ALIAS: ${{ secrets.KEY_ALIAS }}
    KEY_PASSWORD: ${{ secrets.KEY_PASSWORD }}
  run: ./gradlew assembleRelease
```

The build system reads environment variables **first**, so CI secrets take precedence over any `local.properties` values.

## Creating a Release Keystore

If you don't have a keystore yet:

```bash
keytool -genkey -v -keystore release.jks \
  -keyalg RSA -keysize 2048 -validity 10000 \
  -alias weightflow
```

You'll be prompted for the keystore password and key password. Store both securely.

## Backup & Security

**Before submitting to Play Store for the first time:**

1. **Back up your keystore** to 3 secure locations:
   - Personal encrypted drive or cloud storage
   - Password manager (e.g., 1Password, LastPass) — store the `.jks` file + passwords
   - Offline backup (e.g., USB drive in a safe)

2. **Never commit** `local.properties` to git

3. **Never share** the keystore file or passwords

4. **If the keystore is lost**, you cannot update the app on Play Store — you'd need to unpublish and republish under a new package name.

## Implementation Details

See `app/build.gradle.kts`:

- **Signing config** (lines 31–49): Reads credentials from environment (CI) or `local.properties` (dev)
- **Validation task** (lines 79–113): `validateReleaseSigning` checks all 4 fields
- **Task dependency** (lines 115–118): `assembleRelease` depends on `validateReleaseSigning`

Architecture Decision Record: `docs/adr/008-release-signing-validation.md`

## Troubleshooting

### "Release signing validation failed. Missing credentials"

Make sure all 4 values are set:
- `KEYSTORE_PATH` points to an existing `.jks` file
- `KEYSTORE_PASSWORD` is the keystore password (not empty)
- `KEY_ALIAS` is the key alias (not empty)
- `KEY_PASSWORD` is the key password (not empty)

### "Keystore was tampered with, or password was incorrect"

Check that both passwords match the keystore. If unsure:

```bash
keytool -list -v -keystore /path/to/release.jks
```

You'll be prompted for the keystore password. This confirms the keystore is valid.

### "Cannot verify keystore integrity"

The keystore file is corrupted or the wrong password was used. Restore from backup.

## References

- [Android Signing Your App](https://developer.android.com/studio/publish/app-signing)
- [Google Play Console Help](https://support.google.com/googleplay/android-developer)
- [KeyTool Documentation](https://docs.oracle.com/en/java/javase/17/docs/specs/man/keytool.html)
