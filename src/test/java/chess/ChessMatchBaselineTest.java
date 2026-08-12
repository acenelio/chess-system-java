package chess;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;

/**
 * Captures the CURRENT, known-good behavior of traditional mode before the
 * Chess960 feature is added. If a future change (intentional or not) alters
 * the starting layout, swaps a piece's side, or changes whose turn goes
 * first, one of these should fail.
 */
public class ChessMatchBaselineTest {

	@Test
	void traditionalModeHasSixteenPiecesPerSide() {
		ChessMatch match = new ChessMatch(GameMode.TRADITIONAL);
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
		assertEquals(16, whiteCount, "White should start with 16 pieces");
		assertEquals(16, blackCount, "Black should start with 16 pieces");
	}

	@Test
	void traditionalModeWhiteBackRankIsStandardOrder() {
		ChessMatch match = new ChessMatch(GameMode.TRADITIONAL);
		ChessPiece[][] pieces = match.getPieces();

		// Board row 7 == chess rank 1 (white's back rank). See ChessPosition.toPosition().
		String[] expectedOrder = { "R", "N", "B", "Q", "K", "B", "N", "R" };
		for (int col = 0; col < 8; col++) {
			ChessPiece piece = pieces[7][col];
			assertNotNull(piece, "Expected a piece at column " + col + " on white's back rank");
			assertEquals(expectedOrder[col], piece.toString(), "Unexpected piece at column " + col);
		}
	}

	@Test
	void traditionalModeBlackBackRankIsStandardOrder() {
		ChessMatch match = new ChessMatch(GameMode.TRADITIONAL);
		ChessPiece[][] pieces = match.getPieces();

		// Board row 0 == chess rank 8 (black's back rank).
		String[] expectedOrder = { "R", "N", "B", "Q", "K", "B", "N", "R" };
		for (int col = 0; col < 8; col++) {
			ChessPiece piece = pieces[0][col];
			assertNotNull(piece, "Expected a piece at column " + col + " on black's back rank");
			assertEquals(expectedOrder[col], piece.toString(), "Unexpected piece at column " + col);
		}
	}

	@Test
	void traditionalModeNoPieceHasSwappedSides() {
		ChessMatch match = new ChessMatch(GameMode.TRADITIONAL);
		ChessPiece[][] pieces = match.getPieces();

		for (int col = 0; col < 8; col++) {
			assertEquals(Color.BLACK, pieces[0][col].getColor(), "Row 0 should be all black pieces");
			assertEquals(Color.BLACK, pieces[1][col].getColor(), "Row 1 should be all black pawns");
			assertEquals(Color.WHITE, pieces[6][col].getColor(), "Row 6 should be all white pawns");
			assertEquals(Color.WHITE, pieces[7][col].getColor(), "Row 7 should be all white pieces");
		}
	}

	@Test
	void gameStartsWithWhiteToMoveOnTurnOne() {
		ChessMatch match = new ChessMatch(GameMode.TRADITIONAL);
		assertEquals(Color.WHITE, match.getCurrentPlayer());
		assertEquals(1, match.getTurn());
	}

	@Test
	void noArgConstructorDefaultsToTraditionalMode() {
		// Backward-compatibility check: existing callers using `new ChessMatch()`
		// must keep getting standard chess, unchanged by this feature.
		ChessMatch match = new ChessMatch();
		assertEquals(GameMode.TRADITIONAL, match.getGameMode());
		assertEquals("R", match.getPieces()[7][0].toString());
		assertEquals("K", match.getPieces()[7][4].toString());
	}
}