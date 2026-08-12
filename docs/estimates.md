# Chess960 Feature — Task Estimates

Estimates written before starting work. Every task is scoped under 4 hours.

| # | Task | Estimate |
|---|------|----------|
| 1 | Fork repo, verify build/run, create feature branch | 0.5h |
| 2 | Add Maven layout + JUnit 5 test framework | 1.5h |
| 3 | Write baseline tests (starting position, piece counts, no side-swap, turn order) | 1.5h |
| 4 | Add GameMode enum + mode-selection prompt in UI/Program | 1.0h |
| 5 | Implement Chess960 back-rank generator (bishop-color + king-between-rooks rules) | 2.5h |
| 6 | Wire Chess960 setup into ChessMatch, refactor traditional setup out cleanly | 1.5h |
| 7 | Write Chess960 tests (5-10 tests, incl. deliberate-break demo) | 2.0h |
| 8 | Manual playtest both modes, bug fixes | 1.5h |
| 9 | Record actual time, write retro | 0.5h |
| 10 | Record demo video | 1.0h |
| **Total** | | **13.5h** |

Actuals and the post-project analysis are recorded in [retro.md](retro.md).
