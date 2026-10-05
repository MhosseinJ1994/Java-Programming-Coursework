import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

public class BoardStructureTest {
    static String boardToString(char[][] board) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                sb.append(" ").append(board[i][j]).append(" ");
            }
            sb.append("\n");
        }
        return sb.toString();
    }

    static void printBoard(char[][] board) {
        System.out.print(boardToString(board));
    }

    @Test
    @DisplayName("Fresh board shows numbers 1 to 9")
    void freshBoardShowsNumbers() {
        char[][] board = new char[3][3];
        BoardStructure.fillBoard(board);

        String expected = " 1  2  3 \n"
                + " 4  5  6 \n"
                + " 7  8  9 \n";

        assertThat(BoardStructure.boardToString(board)).isEqualTo(expected);
    }
}
