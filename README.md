# chess-system-java — with Chess960 mode

A console chess application in Java. This branch (`feature/chess960-mode`) adds a
**Chess960 (Fischer Random)** game mode alongside the original traditional game.

## What changed

| Area | Change |
|---|---|
| `chess/GameMode.java` | **New.** Enum with `TRADITIONAL` and `CHESS960`. |
| `chess/Chess960BackRankGenerator.java` | **New.** Generates a valid randomized back rank. |
| `chess/ChessMatch.java` | Takes a `GameMode`; `initialSetup()` now delegates to `initialSetupTraditional()` or `initialSetupChess960()`. Castling gated behind `castlingEnabled()`. |
| `chess/pieces/King.java` | Castling moves only offered in traditional mode. |
| `application/UI.java` | **New** `readGameMode(Scanner)` prompt. |
| `application/Program.java` | Asks for the mode before starting the match. |
| `pom.xml`, `src/**` layout | Converted to standard Maven layout; JUnit 5 added. |
| `src/test/**` | 14 tests: 6 baseline (traditional) + 8 Chess960. |

The no-arg `new ChessMatch()` constructor is preserved and still yields a standard
game, so every pre-existing caller behaves exactly as before.

## Chess960 rules implemented

On startup the back rank is randomized subject to:

1. **Bishops on opposite-colored squares.** Achieved by drawing one bishop from the
   even-indexed columns and one from the odd-indexed columns.
2. **King strictly between the two rooks.** After bishops, queen, and knights are
   placed, the three remaining squares are sorted and filled rook–king–rook.

White and black mirror the *same* layout, as the rules require.

**Castling is intentionally out of scope for Chess960** (permitted by the assignment).
The existing castling implementation hardcodes the rook at the king's column ±3/−4,
which is only meaningful on the standard layout. Rather than leave that misfiring on a
randomized board, castling is disabled when the mode is `CHESS960`. Traditional castling
is untouched and fully functional.

## Running

```bash
mvn compile
mvn exec:java -Dexec.mainClass="application.Program"
```

Or run `application.Program` from your IDE. You'll be prompted:

```
Select game mode:
1 - Traditional Chess
2 - Chess960 (Fischer Random)
Choice:
```

Moves are entered as coordinates, e.g. source `e2`, target `e4`.

## Running the tests

```bash
mvn test
```

- `ChessMatchBaselineTest` (6 tests) — written **before** the feature, pinning the
  traditional starting layout, piece counts, side assignment, and turn order. These are
  the regression net: if Chess960 work altered a square or swapped a piece's side, they
  fail.
- `Chess960Test` (8 tests) — placement rules swept across 500 seeded layouts, plus
  match-level checks on piece counts, mirroring, turn order, and the absence of castling.

See [docs/retro.md](docs/retro.md) for a documented deliberate-break you can use to
demonstrate the tests failing.

## Thoughts on the existing code

The original codebase is a clean teaching implementation with a sensible layered split
— `boardgame` holds generic board mechanics, `chess` holds the rules — and adding a mode
was mostly a matter of extending `initialSetup()`. A few things made the work harder
than it needed to be:

- **Setup was one long hardcoded method.** 32 sequential `placeNewPiece` calls with no
  seam for an alternate arrangement. Splitting it into two named methods behind a
  delegating `initialSetup()` was the first refactor.
- **Special-move logic assumes the standard layout in three places.** The castling
  offsets live in `King.possibleMoves`, `ChessMatch.makeMove`, and `ChessMatch.undoMove`
  and must stay in sync. Encoding "which rook" as arithmetic on the king's column rather
  than as a lookup is what makes Chess960 castling a rewrite instead of an extension.
- **Position lookups aren't bounds-checked.** `board.piece(pos)` in the castling block
  is called without `positionExists`, so a king near an edge could read out of bounds.
  It's unreachable in traditional play, but it's a landmine for anyone changing the setup.
- **`ChessMatch` is doing a lot.** Rules, board state, turn management, captured-piece
  bookkeeping, and setup all in one ~380-line class. Fine at this size, but the setup
  concern was the first thing worth pulling out.

## Documentation

- [docs/estimates.md](docs/estimates.md) — task breakdown and pre-work estimates
- [docs/retro.md](docs/retro.md) — actual hours, analysis, deliberate-break demo
