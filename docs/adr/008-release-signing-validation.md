# ADR 008: Release Signing Validation

## Context

WeightFlow requires a signed APK/AAB for Play Store submission. Missing signing credentials (keystore path, keystore password, key alias, key password) can cause confusing late-stage build failures.

## Decision

Implement **fail-fast release signing validation** in `app/build.gradle.kts`:

1. **Signing config** reads credentials from environment variables (CI priority) or `local.properties` (local dev)
2. **Dedicated validation task** `validateReleaseSigning` checks all 4 credentials before any build attempt
3. **Dependency injection** makes `assembleRelease` depend on `validateReleaseSigning`

## Rationale

- **Fast failure**: Developers see missing credentials immediately, not after 2-5 minutes of compilation
- **Clear messaging**: Error message lists exactly which credentials are missing and where to set them
- **CI-friendly**: Environment variables are preferred for CI (GitHub Actions secrets), local files for dev
- **Precedent**: Follows Android Gradle Plugin conventions for validation tasks

## Implementation

### Build Configuration (`app/build.gradle.kts`)

```kotlin
signingConfigs {
    create("release") {
        // Environment variables (CI) take precedence over local.properties
        val storePathEnv = System.getenv("KEYSTORE_PATH")?.takeIf { it.isNotEmpty() }
        val storePassEnv = System.getenv("KEYSTORE_PASSWORD")?.takeIf { it.isNotEmpty() }
        val keyAliasEnv = System.getenv("KEY_ALIAS")?.takeIf { it.isNotEmpty() }
        val keyPassEnv = System.getenv("KEY_PASSWORD")?.takeIf { it.isNotEmpty() }

        val props = Properties()
        val localPropsFile = rootProject.file("local.properties")
        if (localPropsFile.exists()) props.load(localPropsFile.inputStream())

        val storeFilePath = storePathEnv ?: props.getProperty("KEYSTORE_PATH")
        storeFile = storeFilePath?.let { file(it) }
        storePassword = storePassEnv ?: props.getProperty("KEYSTORE_PASSWORD") ?: ""
        keyAlias = keyAliasEnv ?: props.getProperty("KEY_ALIAS") ?: ""
        keyPassword = keyPassEnv ?: props.getProperty("KEY_PASSWORD") ?: ""
    }
}

afterEvaluate {
    tasks.register("validateReleaseSigning") {
        doFirst {
            val signingConfig = android.signingConfigs.getByName("release")
            val missing = mutableListOf<String>()
            
            if (signingConfig.storeFile == null || !signingConfig.storeFile.exists()) 
                missing.add("storeFile (KEYSTORE_PATH)")
            if (signingConfig.storePassword.isNullOrBlank()) 
                missing.add("storePassword (KEYSTORE_PASSWORD)")
            if (signingConfig.keyAlias.isNullOrBlank()) 
                missing.add("keyAlias (KEY_ALIAS)")
            if (signingConfig.keyPassword.isNullOrBlank()) 
                missing.add("keyPassword (KEY_PASSWORD)")

            if (missing.isNotEmpty()) {
                error("Release signing validation failed: ${missing.joinToString(", ")}")
            }
        }
    }

    tasks.named("assembleRelease").configure {
        dependsOn("validateReleaseSigning")
    }
}
```

## Usage

### Local Development

Create `WeightFlow/local.properties` (not checked in):
```properties
KEYSTORE_PATH=/Users/you/.android/release.jks
KEYSTORE_PASSWORD=your-keystore-password
KEY_ALIAS=your-key-alias
KEY_PASSWORD=your-key-password
```

Then:
```bash
./gradlew assembleRelease
```

### CI/GitHub Actions

Add GitHub repository secrets and reference in workflow:
```yaml
- name: Build release APK
  env:
    KEYSTORE_PATH: ${{ secrets.KEYSTORE_PATH }}
    KEYSTORE_PASSWORD: ${{ secrets.KEYSTORE_PASSWORD }}
    KEY_ALIAS: ${{ secrets.KEY_ALIAS }}
    KEY_PASSWORD: ${{ secrets.KEY_PASSWORD }}
  run: ./gradlew assembleRelease
```

## Testing Validation

```bash
# Test the validation task directly
./gradlew validateReleaseSigning

# Will fail if credentials missing:
# Release signing validation failed. Missing credentials:
#   - storeFile (KEYSTORE_PATH)
#   - storePassword (KEYSTORE_PASSWORD)
#   ...
```

## Alternatives Considered

1. **Silent fallback to debug signing** — rejected: dangerous for Play Store builds, no error visibility
2. **Manual pre-build checks** — rejected: relies on developer discipline, not enforced
3. **Single validation in assembleRelease.doFirst** — rejected: too late if compilation takes 2+ minutes

## Consequences

- Developers cannot accidentally submit debug-signed builds to Play Store
- CI failure is immediate and clear
- All 4 credentials must be provided (no partial support)
- Keystore file must exist on disk (no base64-encoded support for now, but extensible)

## Related

- Task #7: Harden release signing validation
- CLAUDE.md: "Release Signing Setup" section
