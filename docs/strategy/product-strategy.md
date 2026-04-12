# WeightFlow — Product Strategy
_Last updated: 2026-04-12 | Source of truth for product, market, revenue, and launch decisions_

---

## Market Position

**The gap:** Every major weight tracking app is either free + bloated + ads, or a $40–70/year subscription that locks basic features.
**Our position:** Genuinely free, beautiful, offline-first weight tracker with real charts, data ownership (CSV), and migration from any app. No subscription. No guilt.

**Direct competitors:**
| App | Price | Problem |
|-----|-------|---------|
| MyFitnessPal | $20/month premium | Bloated, calorie-obsessed, aggressive upsell |
| Noom | $70–209/year | Psychological pressure tactics |
| Zero (fasting) | $70/year | $300k/month revenue — proves market pays. Fasting only. |
| LoseIt! | $40/year | Feature-locked free tier, dated UI |
| WeightFit | Ads + IAP | The app we're replacing. Bad charts, no customization. |

---

## Revenue Model

### Always Free (core hook — never lock these)
- Unlimited weight logging
- All chart types (line, bar, area, candlestick)
- All time filters (7D / 30D / 3M / 1Y / All)
- Full history
- Goal tracking + achievement badges (8)
- CSV import AND export
- Data migration from any app (WeightFit, Happy Scale, Apple Health, Google Fit)
- All 8 color themes + dark/light
- All languages
- No ads on core screens

### Athlete Pro — One-time ~$2.99
- Body composition tracking (body fat %, muscle mass)
- Home screen widget
- Apple Health / Google Fit deep sync
- Cloud backup (Firebase cross-device)
- Advanced stats (rolling average, trend line, velocity)
- Remove bottom banner ad
- Priority support + early access

### Additional Revenue Streams
- **Ko-fi / Liberapay / GitHub Sponsors** — donation link in Settings → About
- **Small AdMob banner** (free users only, bottom of non-core screens)
- **Google Play Billing** (handles local currencies in 170+ countries automatically)
- **Razorpay** (for Indian users: UPI, net banking, wallets)

### Sustainability Math
- 500 downloads/month × 5% Pro conversion × $2.09 (after 15% Play store cut) = ~$52/month
- 5,000 downloads/month = ~$520/month
- Zero ongoing server costs (offline-first). The app sustains itself.

---

## iOS Path (No $99 Upfront)

1. **Now**: Android only. Build user base.
2. **Month 3**: TestFlight beta via Compose Multiplatform (same Kotlin codebase runs on iOS). 0 cost.
3. **Month 4–6**: BuyMeACoffee/Ko-fi donations fund the $99 Apple Developer account. Then App Store.
4. **Tech**: Kotlin Multiplatform (KMP) — share business logic + Compose Multiplatform for shared UI.

---

## Getting First 10 Users (Exact Playbook)

1. **Personal network (Day 1)**: WhatsApp/DM 5 people who care about fitness. Ask for install + review.
2. **Reddit "I made this"**: Post in r/androidapps + r/loseit. "I was frustrated with WeightFit so I built a free alternative." Story + screenshots + "free forever" = upvotes. _Expected: 50–300 installs._
3. **IndieHackers.com**: 23.1% conversion rate per post. "I built a free weight tracker, here's the story." _Expected: 100–500 targeted users._
4. **Hacker News "Show HN"**: "Show HN: Free open-source weight tracker — tired of $70/year apps." Open source + free + well-built = HN front page. _Expected: 200–2,000 installs._
5. **F-Droid submission**: Privacy-conscious open source Android users. Steady passive drip.
6. **"Switch from WeightFit" migration campaign**: Post in WeightFit reviews/communities. Migration = highest conversion channel. Users bring their data AND tell others.

---

## User Acquisition Strategy (Zero Budget)

1. **ASO (App Store Optimization)** — 65%+ of downloads are organic search. Own keywords: "weight tracker", "weigh in app", "body weight log", "scale tracker daily"
2. **Reddit**: r/loseit (3M members), r/progresspics, r/fitness. Genuinely helpful presence, not spam.
3. **Open source on GitHub**: Community contributes features + translations. Developers become your best marketers.
4. **Product Hunt**: Coordinated launch day. Early users upvote. Proven to drive 1k–5k installs.
5. **Data migration = growth hack**: "Switch from [app] in 30 seconds" — users who migrate bring their data AND tell friends.
6. **F-Droid**: Privacy-first Android users. Loyal, vocal advocates.

---

## Privacy Compliance (International)

**Architectural advantage**: Data stays on device = inherently GDPR compliant on the hardest requirements.

| Region | Law | Status | Action Needed |
|--------|-----|--------|---------------|
| EU/EEA | GDPR | ✅ On-device, CSV export, no tracking | Ad consent dialog, analytics opt-in |
| USA | CCPA/COPPA | ✅ No data selling | Age gate (13+) in onboarding |
| India | DPDP Act 2023 | ✅ On-device = compliant | Hindi translation |
| Brazil/Canada/AU | LGPD/PIPEDA | ✅ GDPR compliance covers these | — |
| China | PIPL | ⚠️ Strictest — no "legitimate interest" | Offline-only mode for CN; skip Firebase sync |
| All regions | App Store rules | ❗ Required before Phase 4 | Privacy Policy URL, Data Safety form (Play), App Privacy Labels (Apple) |

**Strategy**: Implement to GDPR (strictest standard). Add jurisdiction-specific controls (COPPA age gate, CN offline mode) as overlays.

---

## Multi-Language Strategy

- **Launch**: English + German (user's market)
- **Phase 3**: Hindi (user's audience base)
- **Community-driven**: Spanish, French, Portuguese via Crowdin (free for open source)
- **Machine-assisted**: 10+ more languages via DeepL API free tier (500k chars/month)
- **RTL support**: Arabic, Hebrew, Farsi — Android supports RTL with `layoutDirection`. Plan layouts for RTL from Phase 2.

---

## Long-Term Vision

| Year | Milestone |
|------|-----------|
| 2026 | WeightFlow v1 Android + iOS. 1,000 loyal users > 100,000 casual ones. |
| 2027 | + Fasting timer (Zero competitor at $0). Same app, no extra cost. Weight + fasting users never leave. |
| 2027+ | Water intake, steps (no wearable), sleep, mood. Full free health platform. Revenue from Pro + donations. |

---

## Donation Payment Methods (International)

| Platform | Coverage | Fee |
|----------|----------|-----|
| Ko-fi | PayPal + Stripe, 190+ countries | 0% on one-time donations |
| Liberapay | Multi-currency, FOSS-native | 0% |
| GitHub Sponsors | Developer community, global | 0% (GitHub covers) |
| Google Play Billing | 170+ countries, local currency auto | 15% (small biz rate) |
| Razorpay | India: UPI, net banking, PhonePe, Paytm | 2% + GST |
