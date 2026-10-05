import java.util.Arrays;

public class ValidInput {
    static final int INVALID = -1;

    public static boolean isInvalid(int playerMove, char[][] board) {
        int[] emptyCells = BoardStructure.getEmptyCells(board);
        return Arrays.stream(emptyCells).noneMatch(x -> x == playerMove);
    }

    // Turns typed text into a cell number, or INVALID if it isn't a number.
    public static int parseMove(String line) {
        try {
            return Integer.parseInt(line.trim());
        } catch (NumberFormatException e) {
            return INVALID;
        }
    }
}
