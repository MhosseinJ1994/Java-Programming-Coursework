import java.util.stream.IntStream;

public class BoardStructure {
    static final int ROWS = 3;
    static final int COLS = 3;
    static String boardToString(char[][] board) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < ROWS; i++) {
            for (int j = 0; j < COLS; j++) {
                sb.append(" ").append(board[i][j]).append(" ");
            }
            sb.append("\n");
        }
        return sb.toString();
    }

    static void printBoard(char[][] board) {
        System.out.print(boardToString(board));
    }

    static void fillBoard(char[][] board) {
        for (int i = 0; i < ROWS; i++) {
            for (int j = 0; j < COLS; j++) {
                board[i][j] = (char) ('1' + i * COLS + j);
            }
        }
    }

    static int[] getEmptyCells(char[][] board) {
        return IntStream.rangeClosed(1, 9)
                .filter(n -> {
                    int[] pos = Move.numberInBoard(n);
                    return Character.isDigit(board[pos[0]][pos[1]]);
                })
                .toArray();
    }
}
