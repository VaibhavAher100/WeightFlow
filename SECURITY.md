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

### Local-First Architecture
- All user data (weight entries, preferences, profile) stored locally on-device
- No cloud backend or remote servers
- Backup disabled to prevent unexpected cloud sync
- Data extraction disabled to prevent system backup of sensitive health data

### Network Security
- Cleartext traffic disabled via `network_security_config`
- Only HTTPS connections permitted
- System certificate pinning enabled by default

### Release Build Hardening
- ProGuard minification + resource shrinking enabled
- Release signing enforced (will not fallback to debug signing)
- APK/AAB code obfuscation required

### Permissions
- Minimal requested permissions: `POST_NOTIFICATIONS`, `VIBRATE`
- No internet access required
- Camera, contacts, location, mic all unrequested

### Authentication & Authorization
- Single-user app (no account required)
- No authentication system (data is local-only)

---

## Known Limitations

1. **Unencrypted local storage:** Room database not encrypted at rest. Threat model assumes app sandbox protection and physical device security.
2. **CSV import robustness:** Custom CSV parser may not handle malformed files gracefully. Validation added for file size (5 MB limit).
3. **Notification permission:** Requested at app startup. Will be moved to in-context request in future versions.

---

## Dependency Updates

WeightFlow uses automated dependency scanning via GitHub Actions. Dependencies are kept current and updated regularly.

Check `build.gradle.kts` for all dependencies and versions.

---

## Questions or Suggestions?

For non-security questions or feature requests, open a GitHub issue or discussion.

For security hardening suggestions, email vaibhavaher100@gmail.com.
