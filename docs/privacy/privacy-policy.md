# Privacy Policy

**WeightFlow**
Last updated: 7 May 2026
Effective date: 7 May 2026

---

## 1. Who We Are

WeightFlow is developed and maintained by Vaibhav Aher ("we", "us", "our").

Contact: vaibhavaher100@gmail.com

For GDPR purposes, Vaibhav Aher is the data controller for personal data processed by WeightFlow.

---

## 2. What Data We Collect and Why

WeightFlow is a **privacy-first, offline app**. All data is stored locally on your device. We do not operate servers, and your data does not leave your device in the current version of the app.

| Data | Why we collect it | Where it's stored |
|------|------------------|-------------------|
| Weight entries (value, date, unit) | Core weight tracking functionality | On-device (Room database) |
| User profile (display name, height, goal weight, year of birth) | Goal tracking, BMI calculation, personalisation | On-device (Room database) |
| Preferences (theme, weight unit, notification settings) | App experience customisation | On-device (DataStore) |

We do not collect:
- Email addresses or account credentials (no registration required)
- Location data
- Device identifiers
- Advertising IDs
- Health data from third-party sources (Apple Health / Google Fit integration is not available in the current version)

---

## 3. Legal Basis for Processing (GDPR)

We process your personal data on the following legal bases under GDPR Article 6:

- **Article 6(1)(b) — Performance of contract:** Processing weight entries and profile data is necessary to deliver the core weight tracking service you requested.
- **Article 6(1)(a) — Consent:** If you enable daily reminder notifications, we process your preference based on your explicit consent. You can withdraw consent at any time by disabling notifications in Settings.

---

## 4. How Long We Keep Your Data

Your data is stored on your device for as long as you use the app. It is deleted immediately when you use the **"Delete all data"** function in Profile → Settings, or when you uninstall the app.

We do not retain any data on external servers.

---

## 5. Your Rights

Under GDPR, you have the following rights regarding your personal data:

- **Right of access (Art. 15):** All your data is visible within the app at all times.
- **Right to data portability (Art. 20):** Export your weight history as a CSV file at any time via Settings → Export data.
- **Right to erasure (Art. 17):** Delete all your data permanently via Profile → Delete all data. This action is immediate and irreversible.
- **Right to rectification (Art. 16):** Edit or delete individual weight entries from the History screen.
- **Right to withdraw consent:** Disable notifications in Settings at any time.

To exercise any other rights, contact us at vaibhavaher100@gmail.com. We will respond within 30 days.

---

## 6. Data Sharing and Third Parties

We do not sell, rent, or share your personal data with third parties.

**Google Play Store:** WeightFlow is distributed via the Google Play Store. Google may collect metadata about your device and installation as part of their platform. This is governed by [Google's Privacy Policy](https://policies.google.com/privacy).

**Future features (not yet active):** We may in future versions add:
- Firebase Crashlytics (crash reporting) — will be opt-in
- AdMob (advertising for free tier) — will include standard ad identifiers per Google's policies
- Google Play Billing (for Pro unlock) — payment is handled by Google; we do not receive payment card details

We will update this policy before activating any of these features.

---

## 7. Data Security

Your data is stored in the app's private storage directory on your device and encrypted using **SQLCipher 4 (AES-256-CBC)**. The encryption key is generated on first launch and stored securely in Android Keystore, which is hardware-backed on supporting devices.

The app declares **no `INTERNET` permission**, so it cannot transmit your data over the network. Cleartext traffic is also disabled at the platform level via `network_security_config`.

**On factory reset or uninstall:** Android removes the app's private storage, including the encrypted Room database and DataStore preferences. Because Auto Backup is disabled for these files (see `backup_rules.xml` and `data_extraction_rules.xml`), no copy is uploaded to Google Drive, and there is no remote copy to delete.

**Data export security:** Settings → Export data offers three formats:

- **Plaintext CSV** — your full weight history as an unencrypted CSV file. A warning dialog is shown before the file picker opens. Only share with apps you trust.
- **Encrypted ZIP (AES-256)** — your weight history encrypted with AES-256 inside a password-protected ZIP file. You choose a password of at least 12 characters; the app never stores or transmits this password. The file can be opened with 7-Zip, WinRAR, or The Unarchiver. Note: macOS Archive Utility does not support AES-encrypted ZIPs.
- **Minimal CSV** — date and weight columns only, with no profile or note data. This format reduces data exposure but is not anonymous: a date+weight series may still identify you when combined with other information.

We recommend keeping your device OS and WeightFlow updated to benefit from the latest security patches.

---

## 8. Children's Privacy

WeightFlow is intended for users aged **13 and over** (COPPA minimum). The app enforces a year-of-birth gate during onboarding — users under 13 cannot proceed.

We do not knowingly collect personal data from individuals under 13. If you believe a person under 13 has provided data, contact us at vaibhavaher100@gmail.com and we will assist with deletion.

---

## 9. International Users

WeightFlow is developed in India. If you use the app from within the European Economic Area (EEA), your rights under GDPR apply as described in this policy. By using the app, you acknowledge that your data is processed locally on your device.

---

## 10. Changes to This Policy

If we make material changes to this policy, we will update the "Last updated" date above and notify you via an in-app notice on the next app launch.

We encourage you to review this policy periodically.

---

## 11. Contact

For privacy enquiries, data subject requests, or to report a concern:

**Vaibhav Aher**
Email: vaibhavaher100@gmail.com

We aim to respond to all requests within 30 days.
