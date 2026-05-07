# Competitor Gap Analysis
_Generated 2026-05-07. Competitors: WeightFit (primary), Zero (design inspiration)._

---

## What WeightFlow Does BETTER

| WeightFlow | WeightFit | Zero |
|-----------|-----------|------|
| Athlete's Journal dark aesthetic | Generic dark | Beautiful but fasting-specific |
| 4 color themes (8 planned) | None | None |
| 12-badge achievement system | None | Achievement hexagons (fasting-only) |
| Goal state machine (active → maintenance → new goal chain) | Simple goal field | Not applicable |
| CSV import from 4 formats | CSV export only | No CSV |
| One-time Pro unlock (no subscription) | "Remove Ads" (implicit sub) | Zero Plus (subscription ~$14.99/mo) |
| GDPR Art.17 delete + Art.20 export | Delete all data only | Download + Delete |
| No ads on free tier (planned AdMob banner only) | Persistent cross-app ad banner | Persistent Zero Plus upsell |

---

## P0 Gaps — Pre-launch critical (v1.0)

These exist in WeightFit and users will notice on first comparison.

### 1. Statistics content in TrendsScreen
WeightFit has a full Statistics tab. WeightFlow's TrendsScreen only shows chart + MIN/MAX/AVG/CHANGE for the selected range.

Missing:
- **All-time Average / High / Low / Total Measurements** (not range-scoped)
- **Change Last 7D / Change Last 30D** (always shown, regardless of range filter)
- **BMI category label + normal range + difference from normal** (we show BMI number, not context)
- **Average change per week / per month** (computed from regression on all data)
- **Estimated Days Until Goal** (most motivating metric — rate × remaining = ETA)

All computable from existing Room data. No schema changes needed.

### 2. BMI context (gauge + category)
We display BMI as a number. WeightFit shows:
- Semicircle gauge: Underweight (16.0–18.5) / Normal (18.5–25.0) / Overweight (25.0–40.0)
- Category label ("Overweight")
- Normal weight range for user's height ("55.1 – 74.4 kg")
- Difference from normal range top/bottom ("+6.2 kg")

This gives the number meaning. Without it, BMI = a number users don't know how to interpret.

### 3. Estimated Days Until Goal
Computed as: `(currentWeight - goalWeight) / avgWeeklyChange × 7`
WeightFit shows "1391 days" — even a large number is informative (shows current trajectory isn't working → motivates behavior change). This is the single most actionable stat in a weight tracking app.

---

## P1 Gaps — v1.1 (high UX value, not launch-blocking)

### 4. Drum roll scroll picker for weight entry
WeightFit uses a vertical scroll picker (79/80/81 integer | 5/6/7 decimal). Our +/- buttons are functional but slower for initial entry. Scroll picker = one gesture to land on weight.

### 5. Individual entry edit + detail view
WeightFit: tap any history row → detail screen (implied by chevron). WeightFlow: swipe-delete only, no edit.
Impact: users who log wrong weight have no recourse except delete + re-log.

### 6. Height in imperial (ft + in)
WeightFit shows height as "5 ft 8 in" for lbs users. WeightFlow shows cm always.
When user selects lbs/st unit, displaying height in cm is jarring.

### 7. Weekly day-dot streak (Zero-style)
Zero shows 7 circles (Sun–Sat), filled = logged. WeightFlow shows streak count only.
Dot pattern = immediate visual feedback on which days you missed. Very engaging.

### 8. 4 missing themes
CLAUDE.md + PRD say 8 themes. Settings currently has only 4 (lime, ocean, ember, violet).
Missing: forest, rose, gold, slate (or equivalent).

### 9. Share app / Rate app links in Settings
WeightFit has "Share this App" + "Write a Review". WeightFlow has donations but no share/rate.
These drive organic installs and Play Store rating — high leverage, low effort.

---

## P2 Gaps — Phase 5 (future roadmap)

| Gap | Source | Notes |
|-----|--------|-------|
| Calendar month view (logged days grid) | Zero | Show which days had entries |
| Google Fit / Health Connect sync | WeightFit, Zero | Phase 5 Firebase |
| BMI trend chart over time | Both | Easy with existing data |
| Height editable after onboarding | WeightFit | Profile edit screen needed |
| Gender field | WeightFit | Affects BMI calc accuracy |
| Multiple metric charts (activity, sleep, calories) | Zero Plus | Phase 5+ |
| Challenges system | Zero | Community feature |
| Educational content (articles/video) | Zero | Content requires curation |

---

## Recommended Pre-launch Action Plan

**Implement P0 this session (Stats + BMI context):**
All are pure computation — no new Room entities, no schema migrations, no new screens.
- Add a "Statistics" section below the Trends chart
- Expand BMI display in Profile/Trends to show category + normal range + diff
- Add estimated days to goal to Home or Trends

**Implement P1 after first AAB upload (v1.0.1):**
- Scroll picker (replaces +/- in LogEntry sheet)
- Entry edit/detail view
- Imperial height display
- Missing 4 themes
- Share/Rate links

**Defer P2 to Phase 5 planning.**
