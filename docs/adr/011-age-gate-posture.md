# ADR-011: Age-gate posture — keep 13+, soften health framing

- **Status:** Accepted
- **Date:** 2026-06-26
- **Supersedes/relates:** age-gate reconciliation (PR #72, gate aligned to 13 across code + strings)

## Context

WeightFlow gates onboarding by year-of-birth. The enforced minimum is **13** (COPPA
minimum), matching ADR intent and the in-app strings. The app surfaces weight logging,
weight-loss **goals**, and **BMI** category/context.

A second-opinion cross-review by Codex (GPT-5.5, with Google Play policy citations)
**disagreed** with a 13+ gate for a body-weight/BMI app:

> "13 is legally minimal, not product-safe, for a weight/BMI app; minors + BMI/weight-loss
> goals creates eating-disorder and harmful-health-content exposure. Play bans apps
> promoting eating disorders and requires health apps to avoid harmful health functionality."
> — cites Play Inappropriate-Content and Health-content policies.

Recommended either 18+ at launch, or keeping 13 while **softening** the weight-loss/BMI
framing and strengthening medical guidance.

## Decision

**Keep the 13+ gate, and soften the health framing** (option chosen by the product owner
over a hard 18+ gate, accepting the residual risk knowingly).

Concretely:
1. **Strengthen the medical disclaimer** (this ADR's change) to explicit non-diagnostic
   language, EN + DE:
   *"WeightFlow is not a medical device and does not diagnose, treat, cure, or prevent any
   condition. Consult a healthcare professional before making health decisions."*
2. **Retain the existing safe-messaging** at goal entry, which already reads:
   *"Set realistic goals. Rapid weight loss can be harmful. If you're struggling with
   disordered eating, please seek professional support."* (`onboarding_goal_disclaimer`).
3. Avoid aggressive/encouraging weight-loss language in copy; keep goals neutral and optional.

## Why (rationale for keeping 13)

- COPPA-minimum maximizes addressable audience and matches the original product intent.
- The app does **not** push aggressive weight-loss tactics; goals are optional and the
  tone is neutral tracking, not coaching toward a target.
- Safe-messaging + non-diagnostic disclaimer materially reduce the "harmful health
  functionality" surface that the policy targets.

## Codex dissent (recorded, not adopted)

Codex's strongest recommendation was **18+ at launch**. We are not adopting that; this ADR
records the dissent so the decision is deliberate and auditable. If Play review flags the
content rating or rejects under the eating-disorder / health-content policy, the fast
remediation is to raise the gate to 18 (one constant `MIN_AGE_YEARS` + the gate strings).

## Consequences / follow-ups (deferred, pre-submission)

- [ ] Complete Play Console **Health-apps declaration**; keep privacy policy + Data Safety
      + store listing + in-app text perfectly consistent (Google enforces discrepancies).
- [ ] Set the Play **content rating** questionnaire honestly (health/weight context).
- [ ] If rejected on age/health grounds → raise gate to 18 (`MIN_AGE_YEARS = 18`) + update
      gate strings (EN/DE) and re-submit.

## Change in this ADR's PR

- `home_medical_disclaimer` (EN + DE) strengthened to explicit non-diagnostic wording.
