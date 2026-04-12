# Issue 16: History screen (list + swipe-delete + edit sheet)

## Parent PRD
docs/specs/2026-04-12-weightflow-master-prd.md

## What to build
The full history log: reverse-chronological list of all entries, swipe-to-delete with confirmation, tap-to-edit bottom sheet.

End-to-end: user swipes left on an entry → red delete background appears → user releases → confirmation dialog appears → user confirms → entry removed and list updates. User taps an entry → edit sheet opens pre-filled → user changes weight → saves → list updates.

## Acceptance criteria
- [ ] Lazy column, reverse-chronological (most recent first)
- [ ] Each row: date (formatted in locale), weight (in user's unit), chevron icon
- [ ] Swipe-left gesture reveals red delete action with trash icon
- [ ] Delete confirmation dialog before finalising: "Delete this entry?" with Cancel / Delete buttons
- [ ] Tap row opens `ModalBottomSheet` pre-filled with entry's weight + date; Save updates Room
- [ ] Empty state: "No entries yet. Tap + to log your first weight."
- [ ] `HistoryViewModel`: `StateFlow<HistoryUiState>` (entries list, selectedEntry, showDeleteConfirm)
- [ ] RTL-safe swipe gesture (swipe direction mirrors in RTL)
- [ ] Content descriptions on all interactive elements

## Blocked by
- Blocked by #9 (repository layer)
- Blocked by #11 (NavGraph shell)

## User stories addressed
33–37 (all History stories).
