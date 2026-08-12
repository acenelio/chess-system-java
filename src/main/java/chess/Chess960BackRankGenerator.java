package chess;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

/**
 * Generates a random Fischer Random (Chess960) back-rank layout as an 8-character
 * array using column order a-h (index 0 = column a ... index 7 = column h).
 * Piece codes match ChessPiece.toString(): K, Q, R, B, N.
 *
 * Rules honored (castling legality is intentionally out of scope):
 *  1. Bishops must sit on opposite-colored squares.
 *  2. The king must sit strictly between the two rooks.
 * Kept separate from ChessMatch/Board so it can be unit tested with a seeded
 * Random without needing to spin up a full match.
 */
public class Chess960BackRankGenerator {

	public static char[] generate() {
		return generate(new Random());
	}

	public static char[] generate(Random random) {
		char[] rank = new char[8];
		boolean[] occupied = new boolean[8];

		// 1. Bishops on opposite colored squares.
		// Column index parity mirrors square color on a standard board (a1 is dark = even index here).
		int darkSquareBishop = pickRandomFromParity(occupied, random, 0);
		rank[darkSquareBishop] = 'B';
		occupied[darkSquareBishop] = true;

		int lightSquareBishop = pickRandomFromParity(occupied, random, 1);
		rank[lightSquareBishop] = 'B';
		occupied[lightSquareBishop] = true;

		// 2. Queen on any remaining square.
		int queenIndex = pickRandomFree(occupied, random);
		rank[queenIndex] = 'Q';
		occupied[queenIndex] = true;

		// 3. Two knights on any remaining squares.
		for (int i = 0; i < 2; i++) {
			int knightIndex = pickRandomFree(occupied, random);
			rank[knightIndex] = 'N';
			occupied[knightIndex] = true;
		}

		// 4. King and both rooks fill the remaining three squares, king in the middle.
		List<Integer> remaining = new ArrayList<>();
		for (int i = 0; i < 8; i++) {
			if (!occupied[i]) {
				remaining.add(i);
			}
		}
		Collections.sort(remaining);
		rank[remaining.get(0)] = 'R';
		rank[remaining.get(1)] = 'K';
		rank[remaining.get(2)] = 'R';

		return rank;
	}

	private static int pickRandomFromParity(boolean[] occupied, Random random, int parity) {
		List<Integer> candidates = new ArrayList<>();
		for (int i = 0; i < 8; i++) {
			if (!occupied[i] && i % 2 == parity) {
				candidates.add(i);
			}
		}
		return candidates.get(random.nextInt(candidates.size()));
	}

	private static int pickRandomFree(boolean[] occupied, Random random) {
		List<Integer> candidates = new ArrayList<>();
		for (int i = 0; i < 8; i++) {
			if (!occupied[i]) {
				candidates.add(i);
			}
		}
		return candidates.get(random.nextInt(candidates.size()));
	}
}