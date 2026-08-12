package chess;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import org.junit.jupiter.api.Test;

/**
 * Tests for the Chess960 (Fischer Random) game mode.
 *
 * The two placement rules that actually define Chess960 are checked exhaustively
 * over many seeded generations rather than on a single random board, because a
 * one-shot random test can pass by luck while the generator is still wrong.
 */
public class Chess960Test {

	/** How many independent layouts the rule tests sweep over. */
	private static final int SAMPLE_SIZE = 500;

	@Test
	void bishopsAlwaysLandOnOppositeColoredSquares() {
		for (int seed = 0; seed < SAMPLE_SIZE; seed++) {
			char[] rank = Chess960BackRankGenerator.generate(new Random(seed));

			int firstBishop = -1;
			int secondBishop = -1;
			for (int i = 0; i < 8; i++) {
				if (rank[i] == 'B') {
					if (firstBishop == -1) firstBishop = i;
					else secondBishop = i;
				}
			}

			assertNotEquals(-1, secondBishop, "seed " + seed + ": expected exactly two bishops");
			assertNotEquals(firstBishop % 2, secondBishop % 2,
					"seed " + seed + ": bishops must sit on opposite colored squares, got columns "
							+ firstBishop + " and " + secondBishop + " in " + new String(rank));
		}
	}

	@Test
	void kingAlwaysSitsStrictlyBetweenTheTwoRooks() {
		for (int seed = 0; seed < SAMPLE_SIZE; seed++) {
			char[] rank = Chess960BackRankGenerator.generate(new Random(seed));

			int king = -1;
			int firstRook = -1;
			int secondRook = -1;
			for (int i = 0; i < 8; i++) {
				if (rank[i] == 'K') king = i;
				if (rank[i] == 'R') {
					if (firstRook == -1) firstRook = i;
					else secondRook = i;
				}
			}

			assertNotEquals(-1, king, "seed " + seed + ": expected a king");
			assertNotEquals(-1, secondRook, "seed " + seed + ": expected exactly two rooks");
			assertTrue(firstRook < king && king < secondRook,
					"seed " + seed + ": king must be strictly between the rooks, got "
							+ new String(rank));
		}
	}

	@Test
	void backRankAlwaysContainsTheCorrectPieceMultiset() {
		for (int seed = 0; seed < SAMPLE_SIZE; seed++) {
			char[] rank = Chess960BackRankGenerator.generate(new Random(seed));
			assertEquals(8, rank.length, "seed " + seed + ": back rank must be 8 squares");

			Map<Character, Integer> counts = new HashMap<>();
			for (char c : rank) {
				counts.merge(c, 1, Integer::sum);
			}

			assertEquals(1, counts.getOrDefault('K', 0), "seed " + seed + ": exactly one king");
			assertEquals(1, counts.getOrDefault('Q', 0), "seed " + seed + ": exactly one queen");
			assertEquals(2, counts.getOrDefault('R', 0), "seed " + seed + ": exactly two rooks");
			assertEquals(2, counts.getOrDefault('B', 0), "seed " + seed + ": exactly two bishops");
			assertEquals(2, counts.getOrDefault('N', 0), "seed " + seed + ": exactly two knights");
		}
	}

	@Test
	void chess960MatchStartsWithSixteenPiecesPerSideAndNoSideSwap() {
		ChessMatch match = new ChessMatch(GameMode.CHESS960);
		ChessPiece[][] pieces = match.getPieces();

		int whiteCount = 0;
		int blackCount = 0;
		for (ChessPiece[] row : pieces) {
			for (ChessPiece p : row) {
				if (p != null) {
					if (p.getColor() == Color.WHITE) whiteCount++;
					else blackCount++;
				}
			}
		}
		assertEquals(16, whiteCount, "White should start with 16 pieces in Chess960");
		assertEquals(16, blackCount, "Black should start with 16 pieces in Chess960");

		// Same rows as traditional: no piece may end up on the wrong side of the board.
		for (int col = 0; col < 8; col++) {
			assertNotNull(pieces[0][col], "Black back rank must be filled");
			assertNotNull(pieces[7][col], "White back rank must be filled");
			assertEquals(Color.BLACK, pieces[0][col].getColor(), "Row 0 should be all black pieces");
			assertEquals(Color.BLACK, pieces[1][col].getColor(), "Row 1 should be all black pawns");
			assertEquals("P", pieces[1][col].toString(), "Row 1 should be pawns");
			assertEquals(Color.WHITE, pieces[6][col].getColor(), "Row 6 should be all white pawns");
			assertEquals("P", pieces[6][col].toString(), "Row 6 should be pawns");
			assertEquals(Color.WHITE, pieces[7][col].getColor(), "Row 7 should be all white pieces");
		}
	}

	@Test
	void bothColorsMirrorTheSameBackRankLayout() {
		// In Chess960 white and black use the SAME randomized layout, mirrored across
		// the board -- they are not randomized independently.
		ChessMatch match = new ChessMatch(GameMode.CHESS960);
		ChessPiece[][] pieces = match.getPieces();

		for (int col = 0; col < 8; col++) {
			assertEquals(pieces[7][col].toString(), pieces[0][col].toString(),
					"Black and white back ranks must match at column " + col);
		}
	}

	@Test
	void generatorProducesMoreThanOneDistinctLayout() {
		// Guards against the generator silently collapsing to a fixed arrangement,
		// which would still satisfy every placement rule above.
		Set<String> layouts = new HashSet<>();
		for (int seed = 0; seed < 50; seed++) {
			layouts.add(new String(Chess960BackRankGenerator.generate(new Random(seed))));
		}
		assertTrue(layouts.size() > 1,
				"Generator should produce varied layouts, only saw: " + layouts);
	}

	@Test
	void chess960GameStartsWithWhiteToMoveOnTurnOne() {
		ChessMatch match = new ChessMatch(GameMode.CHESS960);
		assertEquals(GameMode.CHESS960, match.getGameMode());
		assertEquals(Color.WHITE, match.getCurrentPlayer());
		assertEquals(1, match.getTurn());
	}

	@Test
	void chess960KingIsNeverOfferedACastlingMove() {
		// Castling is out of scope for Chess960 (the rook offsets assume the standard
		// layout). The king should therefore never be offered a two-square move.
		ChessMatch match = new ChessMatch(GameMode.CHESS960);
		ChessPiece[][] pieces = match.getPieces();

		int kingColumn = -1;
		for (int col = 0; col < 8; col++) {
			if ("K".equals(pieces[7][col].toString())) {
				kingColumn = col;
			}
		}
		assertNotEquals(-1, kingColumn, "White king should be on the back rank");

		// Called directly rather than through match.possibleMoves(), which rejects a
		// piece that currently has no legal move -- true for any king on move one.
		boolean[][] moves = pieces[7][kingColumn].possibleMoves();

		for (int col = 0; col < 8; col++) {
			if (Math.abs(col - kingColumn) == 2) {
				assertFalse(moves[7][col],
						"Chess960 king must not be offered a castling move to column " + col);
			}
		}
	}
}
