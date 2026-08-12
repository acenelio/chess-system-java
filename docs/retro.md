# Chess960 Feature — Actuals and Retro

**Branch:** `feature/chess960-mode`

> **Note on how actuals were derived.** I did not keep a running timer, so the hours
> below are reconstructed from commit and reflog timestamps in this repository
> (`git reflog --date=format:"%Y-%m-%d %H:%M"`), which record when each chunk of work
> landed. Where a task spanned interleaved work the split is my best attribution, not
> a stopwatch reading. Rows marked *pending* had not been completed at the time of
> writing. Verify these against your own recollection before submitting.

## Time tracking

| # | Task | Estimate | Actual | Delta |
|---|------|----------|--------|-------|
| 1 | Fork repo, verify build/run, create feature branch | 0.5h | 0.2h | −0.3h |
| 2 | Add Maven layout + JUnit 5 test framework | 1.5h | 1.0h | −0.5h |
| 3 | Write baseline tests | 1.5h | 0.9h | −0.6h |
| 4 | GameMode enum + mode-selection prompt | 1.0h | 0.4h | −0.6h |
| 5 | Chess960 back-rank generator | 2.5h | 1.0h | −1.5h |
| 6 | Wire Chess960 setup into ChessMatch, refactor traditional setup | 1.5h | 0.8h | −0.7h |
| — | **Unplanned:** recover files corrupted by a bad editor save | 0h | 0.6h | +0.6h |
| — | **Unplanned:** discover and fix castling breakage in Chess960 | 0h | 0.5h | +0.5h |
| 7 | Write Chess960 tests | 2.0h | 1.0h | −1.0h |
| 8 | Manual playtest both modes, bug fixes | 1.5h | *pending* | — |
| 9 | Record actual time, write retro | 0.5h | 0.3h | −0.2h |
| 10 | Record demo video | 1.0h | *pending* | — |
| | **Total (excl. pending)** | **11.0h** | **6.7h** | **−4.3h** |

## Biggest misses

My two largest misses were both things I never put on the board. First, I edited
`ChessMatch.java` in Notepad and saved a version that had lost its entire header —
package declaration, imports, and the class declaration itself — which turned into a
100-error compile failure that took over half an hour to trace, because the error list
pointed at every line in the file instead of the one thing actually missing. Second, I
scoped castling as "skippable" and therefore never estimated it at all, but skipping it
turned out to require real work: `King.possibleMoves` and `ChessMatch.makeMove` both
hardcode the rook at the king's column ±3/−4, so on a randomized back rank they would
have grabbed the wrong piece or read off the edge of the board, and I had to gate that
logic behind a mode check. The root cause in both cases is the same — I estimated the
happy path of writing new code and budgeted nothing for integrating with code I hadn't
read closely yet.

The per-task estimates themselves ran high across the board, which I'd rather have than
the reverse, but it means my padding was in the wrong place: I over-budgeted the parts I
understood (the generator, the tests) and zero-budgeted the parts I hadn't looked at.

## What I'm most proud of

The back-rank generator. Rather than the common approach of generating random
permutations and rejecting the ones that break the rules, it places pieces
constructively: bishops are drawn from opposite-parity column pools, then the king and
rooks fill the last three squares in sorted order, which makes "king strictly between
the rooks" true by construction instead of by retry. It is also deliberately decoupled
from `Board` and `ChessMatch` and accepts an injected `Random`, so the placement rules
can be swept across 500 seeded layouts in a unit test without instantiating a game.
That seeded sweep is what gives me real confidence the rules hold, rather than one lucky
random board passing.

I'm also happy that traditional mode came through untouched: the six baseline tests were
written *before* the feature and still pass unmodified, and `new ChessMatch()` still
returns a standard game.

## Deliberate-break demo (for the video)

To show the tests catching a real regression on camera, make this one-line change in
`src/main/java/chess/Chess960BackRankGenerator.java`:

```java
// Original:
int lightSquareBishop = pickRandomFromParity(occupied, random, 1);

// Broken (both bishops now drawn from the same square color):
int lightSquareBishop = pickRandomFromParity(occupied, random, 0);
```

Then run `mvn test`. Expected result: `bishopsAlwaysLandOnOppositeColoredSquares` fails
with a message naming the offending seed and the two bishop columns. Revert the line and
the suite goes green again.
