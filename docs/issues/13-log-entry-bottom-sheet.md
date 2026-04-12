# Issue 13: Log entry bottom sheet (FAB → number pad → save)

## Parent PRD
docs/specs/2026-04-12-weightflow-master-prd.md

## What to build
The primary logging action: FAB opens a `ModalBottomSheet` with a number pad for weight entry and an editable date. Save writes to Room instantly.

End-to-end: user taps FAB from any tab → bottom sheet slides up → number pad focused → user types weight → taps save → sheet dismisses → Home screen reflects new entry immediately.

## Acceptance criteria
- [ ] `ModalBottomSheet` opens on FAB tap from any of the 4 main tabs
- [ ] Number pad: large digit buttons, decimal point, backspace; displays entry in selected unit
- [ ] Date field: pre-filled to today; tappable to open date picker (DatePickerDialog)
- [ ] Save button: disabled until valid weight > 0 entered
- [ ] On save: entry written to Room via `WeightRepository.insert()`; sheet dismisses; no navigation change
- [ ] Validation: empty input, zero, negative all show inline error — no crash
- [ ] `LogEntryViewModel`: `StateFlow<LogEntryUiState>` (weightInput, selectedDate, isLoading, error) + events
- [ ] `LogEntryViewModelTest`: save → success state; invalid input → error state; date edit reflected in state
- [ ] RTL-safe; all elements have content descriptions
- [ ] Weight displayed in user's current unit (reads from DataStore via ViewModel)

## Blocked by
- Blocked by #9 (repository layer)
- Blocked by #11 (NavGraph shell)

## User stories addressed
8–13 (all logging stories).
