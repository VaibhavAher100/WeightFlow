# ADR-012: SQLCipher encryption at rest — keep it, document the threat model

- **Status:** Accepted
- **Date:** 2026-07-03
- **Relates:** ADR (schema v3 file-level encryption), `AppDatabase.buildDatabase`, `DatabaseKeyManager`

## Context

WeightFlow encrypts the Room database at rest with SQLCipher 4 (schema v3+). The key is a
random 32-byte passphrase held in `EncryptedSharedPreferences` (AES-256-GCM master key in the
Android Keystore), read back as plaintext bytes to avoid the null-`encoded()` failure of
hardware-backed Keystore keys (see `DatabaseKeyManager` KDoc).

A two-model code review (Fable 5 + Codex, cross-checking each other against the code) disagreed
on whether SQLCipher earns its cost:

- **Fable 5's position:** rip it out. The data is "my weight, on my phone" in an app-private
  directory; Android file-based encryption (FBE) already covers device-theft. SQLCipher bought a
  debug/release behavioural fork, a risky plaintext to encrypted file migration, a Keystore ANR
  hazard, and a native lib in the APK budget, to protect data whose realistic threat model is
  empty.
- **Codex's position:** keep it. Weight history is plausibly health data. FBE mainly defends a
  **locked, powered-off** device; it does not answer root/ADB extraction of an app-private DB on
  an unlocked device. The right critique is "document the threat model and fix the migration
  path," not "remove encryption."

Fable 5 conceded the substance in debate: encryption-at-rest is defensible for a health app and
is cheap to keep now that it is written. Both models converged on **keep encryption, document
the rationale, harden the migration/init path.**

## Decision

**Keep SQLCipher encryption at rest.** Record the threat model explicitly (this ADR), and harden
the init ordering so the migration path cannot throw (this ADR's PR).

## Threat model (what encryption-at-rest does and does not buy us)

| Threat | Covered by FBE alone? | Covered by SQLCipher? |
|--------|-----------------------|-----------------------|
| Lost/stolen device, locked, powered off | Yes | Yes |
| Unlocked device, `adb backup` of the app | Mitigated by `allowBackup="false"` | Also encrypted |
| Rooted / unlocked device, direct read of the app-private DB file | No | **Yes** (file is ciphertext without the key) |
| Malicious app on non-rooted device reading our sandbox | No (sandbox already blocks it) | N/A |
| Cloud/off-device backup exfiltration | Closed by `data_extraction_rules.xml` + `backup_rules.xml` | Also encrypted |

The marginal protection SQLCipher adds over FBE is the **rooted/unlocked-device DB read**. For a
health-adjacent app on the Play Store (Data Safety declaration, health-content policy), that is a
defensible posture even though the realistic likelihood is low.

Backup exposure is already closed independently of SQLCipher:
`AndroidManifest.xml` (`allowBackup="false"`), `res/xml/backup_rules.xml`, and
`res/xml/data_extraction_rules.xml`.

## Costs accepted (recorded so the trade-off is deliberate)

1. **Debug/release fork** — debug builds skip SQLCipher (`AppDatabase.buildDatabase`,
   `BuildConfig.DEBUG` branch) to avoid the `EncryptedSharedPreferences` Keystore init blocking
   the main thread on cold start. **Consequence: the encrypted path only runs in release**, so it
   must be soak-tested on a real device before submission (see follow-ups).
2. **Native library** — `libsqlcipher` in the APK (watch the 15 MB download budget).
3. **Plaintext to encrypted migration** — `encryptExistingDatabaseIfNeeded` runs once on the
   first release cold start for users upgrading from a plaintext (v1/v2) DB. It is main-thread
   I/O and is the least-exercised, highest-risk branch in the DB layer.

## Init-order hardening (this ADR's PR)

`buildDatabase` previously called `encryptExistingDatabaseIfNeeded(dbFile)` (which calls
`DatabaseKeyManager.getOrCreateKey()`) **before** `DatabaseKeyManager.init(appContext)`.
`getOrCreateKey()` throws `IllegalStateException` if `init()` has not run, so the migration path
was correct only because `WeightFlowApp.onCreate` happens to call `init` first — a fragile,
implicit contract. The call order is now **init first, then migrate**, making `buildDatabase`
self-sufficient regardless of caller.

## Consequences / follow-ups (deferred, pre-submission — device-gated)

- [ ] **Soak-test the release DB path on a physical device:** install a signed release build,
      cold-start 10x, confirm no black-screen/ANR from Keystore init.
- [ ] **Trial the plaintext to encrypted migration with real data:** seed a v1/v2 plaintext DB
      with entries, upgrade to the encrypted release build, verify data survives and the
      plaintext WAL/SHM/journal siblings are cleaned up.
- [ ] Consider moving DB init off the main thread (Phase 5) to retire the debug/release fork
      entirely, which would let the encrypted path run in debug too.

## Dissent (recorded, not adopted)

Fable 5's opening recommendation was to remove SQLCipher. Not adopted; recorded so the decision
is auditable. If the APK budget or the migration proves too costly, the fallback is to drop to
FBE-only, keeping `allowBackup="false"` and the backup rules as the primary data-exfil defenses.
