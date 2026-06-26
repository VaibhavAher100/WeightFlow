# WeightFlow — Play Console Data Safety Form Answers

_Generated 2026-06-26. Copy these answers directly into Play Console →
App content → Data safety. Re-verify against app behavior before each submission._

> **Why "no data collected":** Google Play defines **collection** as *transmitting user
> data off the device*, and **sharing** as *transferring it to a third party*. WeightFlow
> is offline-first: all weight, profile, and goal data is stored only in the on-device
> Room database. Nothing is transmitted to us or anyone else. User-initiated CSV export
> writes to the user's own chosen storage location — that is the user moving their own
> data, not the app collecting it. There is no analytics SDK, no ads SDK, and crash
> reporting (Crashlytics) is **not active** (deps commented out). Therefore the truthful
> declaration is: no data collected, no data shared.

---

## Section 1 — Data collection and security

| Question | Answer |
|----------|--------|
| Does your app collect or share any of the required user data types? | **No** |
| Is all of the user data collected by your app encrypted in transit? | **N/A** — no data is transmitted. (If the form forces a choice, select "Yes" — there is no unencrypted transmission because there is no transmission.) |
| Do you provide a way for users to request that their data is deleted? | **Yes** — in-app: Profile → Delete all data (one tap, wipes the local database). No server data exists to delete. |

Because the first answer is **No**, Play hides the per-type data matrix. The matrix below
is recorded here only as the audit rationale for *why* each category is "Not collected."

---

## Section 2 — Data type rationale (audit record — all "Not collected / Not shared")

| Play data category | Item | Collected? | Rationale |
|--------------------|------|-----------|-----------|
| Personal info | Name, email, user IDs, address, phone | **No** | No account, no sign-in, none requested. |
| Personal info | Race, political, religious, sexual orientation | **No** | Never requested. |
| **Health & fitness** | Health info (weight, BMI, height) | **No (on-device only)** | Stored solely in the local encrypted Room DB. Never transmitted. |
| **Health & fitness** | Fitness info | **No** | Not collected. |
| Financial info | Purchase history | **No** | One-time Pro IAP is processed by Google Play Billing; the app does not collect or store payment data. |
| Location | Approx / precise | **No** | No location permission requested. |
| Messages, photos, audio, files | — | **No** | None accessed except user-chosen CSV import/export files (transient, user-initiated). |
| App activity | In-app actions, search history | **No** | No analytics SDK. |
| App info & performance | Crash logs, diagnostics | **No** | Crashlytics not active (deps commented). |
| Device or other IDs | Device ID, advertising ID | **No** | No ads SDK, no analytics, no ad ID read. |

---

## Section 3 — Security practices (informational answers)

| Prompt | Answer |
|--------|--------|
| Data encrypted in transit | N/A (no transmission). Release build uses HTTPS-only network config + cleartext blocked. |
| Data encrypted at rest | **Yes** — release build uses SQLCipher (AES) on the Room DB; key in Android Keystore / EncryptedSharedPreferences. |
| Users can request deletion | **Yes** — in-app full delete. |
| Committed to Play Families Policy | N/A unless targeting children; age gate is 13+. |
| Independent security review | Not claimed. (Internal security audit completed; see `SECURITY.md`.) |

---

## ⚠️ Update triggers — re-do this form if ANY of these ship

The "no collection" declaration is **only valid while the app stays offline-only.**
Update Data Safety BEFORE releasing any of:

1. **Firebase Crashlytics** (Phase 5) → must declare **Crash logs + Diagnostics**, and
   likely **Device IDs**. Collected + (with Google) shared.
2. **Firebase / cloud sync** (Phase 5) → must declare **Health & fitness data collected**
   (and shared with Google as processor), encrypted in transit = Yes.
3. **Any analytics SDK** → declare App activity + device IDs.
4. **AdMob** (explicitly deferred, not in v1.0) → declare advertising ID + app activity.

Keep this file in lockstep with `WeightFlowApp.kt` wiring. Partial wiring (a dependency
present but unconfigured) still requires disclosure if it transmits anything.

---

## Quick-fill summary (paste order in Play Console)

1. Data collection: **No**
2. Data sharing: **No**
3. Deletion request method: **Yes — in-app delete**
4. Submit. Matrix is skipped automatically.
