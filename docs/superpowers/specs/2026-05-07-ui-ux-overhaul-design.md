# WeightFlow UI/UX Overhaul — Design Spec
_2026-05-07 · Approach C: Hero Moments → System → Remaining Screens_

---

## Design North Star

**Zero's ceremony + Whoop's authority + WeightFlow's warmth.**

- Zero: single metric on pure dark void, generous whitespace, full-pill CTA, ritual feel
- Whoop: Bebas Neue for numbers / Outfit for words, one coaching sentence, functional color vocabulary
- WeightFlow: electric lime `#C8FF00` as the single voice, warm dark `#0A0A08` base, "Athlete's Journal" personality

Inspired by: Huashu (dark cinematic), Open Design (max 2 accent uses per screen), Impeccable (one voice rule, flat-by-default, expo-out motion only).

---

## Design Token System

### Colors
| Token | Value | Usage |
|-------|-------|-------|
| `bg` | `#0A0A08` | All screen backgrounds |
| `surface` | `#161614` | Cards, sheet backgrounds |
| `border` | `#1E1E1C` | All card/row borders — lime-tinted |
| `accent` | `#C8FF00` | Single accent — max 2 uses per screen |
| `accent-soft` | `rgba(200,255,0,0.08)` | Tinted chip backgrounds |
| `accent-border` | `rgba(200,255,0,0.22)` | Accent-adjacent borders |
| `text-primary` | `#E8E8DF` | All primary text — warm white, not pure |
| `text-secondary` | `#888880` | Supporting labels — lime-tinted gray |
| `text-tertiary` | `rgba(255,255,255,0.2)` | Whisper text, placeholders |
| `danger` | `#FF6B6B` | Weight increase delta only |
| `success` | `#4DFF91` | Goal achieved states |

**Rules:**
- No pure `#000000` or `#FFFFFF` anywhere
- Lime used for: active state OR primary action — never both on the same screen
- Danger/success are data signals only, never decorative

### Typography
| Role | Font | Size | Weight | Tracking |
|------|------|------|--------|----------|
| Display / Numbers | Bebas Neue | 64–96sp | 900 | −3px |
| Headline | Bebas Neue | 22–28sp | 900 | −1px |
| Title | Outfit SemiBold | 18sp | 600 | 0 |
| Body | Outfit Regular | 14–15sp | 400 | 0 |
| Label | Outfit Bold | 9–11sp | 800 | +2px, ALL CAPS |
| Coaching | Outfit Regular Italic | 11–12sp | 400 | 0 |
| Coaching value | Outfit SemiBold | 11–12sp | 600 | 0 |

All stat/number displays: `fontVariantNumeric = tabular-nums`

### Spacing (8dp grid)
| Token | Value |
|-------|-------|
| Screen horizontal padding | 16dp |
| Card padding | 16dp |
| Section gap | 24dp |
| Row height (list) | 56dp min |
| Min touch target | 48dp |
| Card corner radius | 16dp |
| Button corner radius | 999dp (pill) |
| Chip corner radius | 999dp |

### Motion
| Interaction | Curve | Duration |
|-------------|-------|----------|
| Color / opacity | `FastOutSlowIn` | 150ms |
| Position / scale | `FastOutSlowIn` | 300ms |
| Sheet open | Spring (stiffness 400, damping 0.8) | — |
| Sheet close | Spring | 600ms |
| Chart draw | `FastOutSlowIn` | 600ms left→right |
| Number count-up | `FastOutSlowIn` | 400ms |
| Staggered cards | `FastOutSlowIn` | 300ms, 50ms apart |
| Glow appear | Linear | 400ms |

**Banned:** bounce easing, elastic easing, layout property animation (width/height/margin/padding).

### Haptics
| Trigger | Type |
|---------|------|
| Drum notch scroll | `HapticFeedbackType.VIRTUAL_KEY` |
| Unit tap | `HapticFeedbackType.CLOCK_TICK` |
| Save entry (normal) | `HapticFeedbackType.CONFIRM` |
| New personal low | Double-pulse heavy (custom `VibrationEffect`) |
| Goal achieved | Triple-pulse heavy |

---

## Hero Moment 1 — Log Entry Sheet

### Layout
- Handle bar (top center)
- **Hero zone (top 55%):** 68sp Bebas Neue weight number, unit label below as lime pill (tap to cycle KG→LBS→ST), coaching sentence in 11sp italic Outfit
- **Drum zone (bottom 35%):** Two scroll columns (whole / decimal), separated by a dot. Active row marked by two 1px lime hairlines — no fill. Fade overlays top and bottom.
- **Meta row:** Unit initials (KG / LBS / ST) left, date right — both whisper text inside drum zone
- **Save zone:** Full-width 999dp pill button, 15dp padding

### States
**Default:** Number 68sp warm white. Coaching: "Last logged X kg · N days ago." Underglow: `radial-gradient` rising from below number at 13% opacity, pulsing subtly.

**Scrolling:** Number updates live as drum scrolls. Haptic tick per notch. Unit label cross-fades on unit switch (150ms).

**Save — normal:** Button morphs to "✓ Logged" (250ms). Drum fades to 12% opacity. Sheet spring-closes after 600ms. CONFIRM haptic.

**Save — new personal low:** Number cross-fades to `#C8FF00` (250ms). Text-shadow glow `0 0 40px rgba(200,255,0,0.3)`. Unit label reads "New Low · Kilograms". Underglow radius expands. Coaching: "−X kg from where you started." Double-pulse haptic. Sheet lingers 1.2s before closing.

### Implementation notes
- `WheelPicker` custom composable: `LazyColumn` + `snapFlingBehavior(lazyListState, SnapLayoutInfoProvider)`
- Pre-set drum to last logged weight on open
- Haptics via `LocalHapticFeedback.current` — no-op guard pre-API 29
- Sheet: `ModalBottomSheet` with `skipPartiallyExpanded = true`

---

## Hero Moment 2 — Home + Trends

### Home Screen
**Header:** "Good morning" label (9sp label) + name (16sp Bebas Neue). No gear icon — Settings is accessed from Profile only.

**Current weight block (center top):**
- 64sp Bebas Neue — same visual language as log entry number
- Unit label in lime (10sp)
- Delta chip: pill, lime for down `▼`, danger for up `▲`
- Underglow beneath number at 11% opacity

**Sparkline card:** Chart preview (30-day), `#C8FF00` line + gradient fill, live dot at latest point. On first render: line draws left→right 600ms.

**3 stat cards (grid):** Start / Lost (lime) / Goal. 18sp Bebas Neue values, 7sp label. Stagger fade-up on load (50ms apart).

**Goal progress bar:** 4dp height, lime fill with `0 0 6px rgba(200,255,0,0.35)` glow. Percentage label right-aligned in lime.

### Trends Screen
**Header:** "Trends" (22sp Bebas Neue), subhead (10sp Outfit secondary).

**Range pills:** Scrollable horizontal row. Active pill: lime background + `#0A0A08` text. Inactive: border only.

**Chart card:** Full-width, 16dp radius card. Line draws on tab entry or range change (600ms). Live dot pulses once on arrival (`scale 1→1.4→1`, 400ms). Gradient fill `rgba(200,255,0,0.18→0)`.

**4 stat cards (2×2 grid):** Total Lost (lime), This Week, All-Time Low, Days to Goal. Same card language as Home.

**Coaching card:** 11sp italic Outfit. "At this rate you'll reach X in ~N weeks." Lime accent for the goal value inline. Only shown when goal is set and `weeklyRateKg < 0` (losing weight). Hidden otherwise — no coaching sentence on Home screen.

### Motion on screen entry
1. Weight counts up 0→current in 400ms `FastOutSlowIn`
2. Delta chip fades in 200ms after
3. Sparkline/chart draws 600ms
4. Stat cards stagger fade+translateY(8dp) up, 50ms apart

---

## Hero Moment 3 — Goal Achievement

Full-screen state surfaced on Home when `currentWeight ≤ goalWeight`.

**Glow burst:** `radial-gradient` covers full screen top, `rgba(200,255,0,0.18)` → transparent. CSS `pulse` animation (scale 1→1.08, 2s infinite).

**Content (centered, z-index above glow):**
- Trophy emoji (36sp)
- "Goal Reached" headline (28sp Bebas Neue, lime, text-shadow glow)
- "You did it, [name]" subhead (12sp Outfit secondary)
- Weight number (64sp Bebas Neue, lime, text-shadow glow)
- Unit label
- Journey row: Started / Lost (lime) / Days — 16sp values, 7sp labels
- Coaching sentence: "You averaged −X kg/week. That's a pace you can maintain."
- Single CTA pill: "Set New Goal →"

**Haptic:** Triple-pulse heavy on screen appear.

---

## History Screen

**Header:** "History" (22sp Bebas Neue).

**Entry rows (grouped by month, month label as divider):**
- Day number: 18sp Bebas Neue — largest element
- Day-of-week: 8sp ALL CAPS below day
- Weight: 20sp Bebas Neue + 8sp unit
- Delta chip: pill — lime for down, danger for up, tertiary text for same
- Today row: subtle `rgba(200,255,0,0.04)` background, day number in lime

**Tap to edit:** Tap any row → edit dialog with pre-filled weight value.

---

## Profile Screen

**Header:** "Profile" (22sp Bebas Neue) + gear icon → Settings.

**Journey card:** Start / Now (lime) / Goal values (18sp Bebas Neue). Progress bar (3dp, lime fill + glow). "X% complete · ~N days to go" below.

**BMI card:** BMI value (28sp Bebas Neue) + category (11sp lime) + range note + distance from upper limit.

**Badges row:** Earned badges show color + glow border. Locked badges greyscale at 40% opacity. "X of 12" label. Tap to reveal unlock condition.

---

## Settings Screen

**Header:** Back chevron (28dp circle) + "Settings" (22sp Bebas Neue).

**Weight Unit:** Three chips (KG / LBS / ST) — active chip: `accent-soft` background + `accent-border` + lime text. Inactive: minimal border.

**Theme grid (2-col):** Each tile has a 14dp color dot + theme name. Active tile: lime border + `rgba(200,255,0,0.06)` background.

**Notification toggle:** Row with title + description + custom toggle (lime fill when on).

**Data / Legal rows:** Title + chevron. No description needed — clean.

---

## Onboarding (4 steps: Age Gate → Unit → Weight → Goal)

**Shared template per step:**
- Handle bar
- Step dots (active dot elongates to 16dp pill, others are 5dp circles)
- Eyebrow: "Step N of 4" (9sp lime label)
- Headline: question in 28sp Bebas Neue
- Subhead: context in 12sp Outfit secondary
- Single input card: `accent-border` when focused, value in 22sp Bebas Neue
- Privacy footer: 9sp tertiary text with live links
- Full pill CTA: disabled (6% white) until valid input, lime when ready

**Age gate special:** Inline age display ("26 yrs") appears right of the input as user types a valid 4-digit year.

**Motion:** Each step slides in from right (300ms `FastOutSlowIn`). Back slides from left.

---

## Implementation Strategy

### Agent/Skill Assignment
| Work | Agent/Skill |
|------|-------------|
| `WheelPicker` composable | `kotlin-specialist` |
| Haptics wrapper | `kotlin-specialist` |
| Animation system (count-up, chart draw, stagger) | `kotlin-specialist` |
| All screen layout updates | `frontend-design` skill |
| Design token `WFTokens.kt` update | `kotlin-specialist` |
| Accessibility audit after each screen | `accessibility-tester` |
| Jank audit after animations | `performance-engineer` |
| Code review pre-merge | `code-reviewer` |

### Build Order (matches hero-moment priority)
1. Design tokens (`WFTokens.kt`) — update all color + spacing constants
2. `WheelPicker` composable + haptics wrapper
3. `LogEntrySheet` — replace keyboard input with picker
4. `HomeScreen` — weight block + sparkline + stat cards + goal bar
5. `TrendsScreen` — chart animation + coaching sentence
6. `GoalAchievedScreen` — new full-screen state
7. `HistoryScreen` — row layout + delta chips
8. `ProfileScreen` — journey card + BMI + badges
9. `SettingsScreen` — theme grid + toggle
10. `OnboardingScreen` — shared step template

### Test coverage required per item
- `WheelPicker`: unit tests for snap behaviour, value bounds, haptic trigger
- All ViewModels: new personal low detection, goal achieved detection
- Accessibility: content descriptions on all interactive elements, 48dp touch targets
