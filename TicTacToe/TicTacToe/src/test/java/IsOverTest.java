import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

public class IsOverTest {
    char[][] board = new char[3][3];
    char[][] board1 = boardOf(
            "XXO",
            "OOX",
            "XOX"
    );
    @BeforeEach
    void setUp() {
        BoardStructure.fillBoard(board);
    }
    private char[][] boardOf(String row0, String row1, String row2) {
        return new char[][] {
                row0.toCharArray(),
                row1.toCharArray(),
                row2.toCharArray()
        };
    }

    @Test
    @DisplayName("X wins with diagonal left mid and right")
    void test1() {
        board[0][0] = 'X';
        board[1][1] = 'X';
        board[2][2] = 'X';
        boolean isOver =IsOver.gameOver(board,'X');
        assertThat(isOver).isTrue();
    }
    @Test
    @DisplayName("X is winner in the other Diagonal")
    void test2() {
        board[0][2] = 'X';
        board[1][1] = 'X';
        board[2][0] = 'X';
        boolean isOver = IsOver.gameOver(board,'X');
        assertThat(isOver).isTrue();
    }
    @Test
    @DisplayName("Two in a row is not a win")
    void test3() {
        board[0][0] = 'X';
        board[0][1] = 'X';

        assertThat(IsOver.gameOver(board, 'X')).isFalse();
    }
    @Test
    @DisplayName("X is winner but not the O")
    void test4() {
        board[0][0] = 'X';
        board[0][1] = 'X';
        board[0][2] = 'X';
        assertThat(IsOver.gameOver(board,'O')).isFalse();
    }
    @Test
    @DisplayName("X is winner in Horizental")
    void test5() {
        board[0][0] = 'X';
        board[0][1] = 'X';
        board[0][2] = 'X';
        assertThat(IsOver.gameOver(board,'X')).isTrue();
    }

    @Test
    @DisplayName("X is winner in Vertical")
    void test6() {
        board[0][0] = 'X';
        board[1][0] = 'X';
        board[2][0] = 'X';
        assertThat(IsOver.gameOver(board,'X')).isTrue();
    }
    @Test
    @DisplayName("X is winner But O is not")
    void test7() {
        board[0][0] = 'X';
        board[1][0] = 'X';
        board[2][0] = 'X';
        assertThat(IsOver.gameOver(board,'O')).isFalse();
    }
    @ParameterizedTest
    @CsvSource({
            "1,2,3", "4,5,6", "7,8,9",   // rows
            "1,4,7", "2,5,8", "3,6,9",   // columns
            "1,5,9", "3,5,7"             // diagonals
    })
    @DisplayName("Every line of three X is a win")
    void test8(int a, int b, int c) {
        for (int n : new int[]{a, b, c}) {
            int[] pos = Move.numberInBoard(n);
            board[pos[0]][pos[1]] = 'X';
        }
        assertThat(IsOver.gameOver(board, 'X')).isTrue();
    }
    @Test
    @DisplayName("Nobody Won in full board")
    void test9() {

        assertThat(IsOver.gameOver(board1,'X')).isFalse();
        assertThat(IsOver.gameOver(board1,'O')).isFalse();
    }
    @Test
    @DisplayName("no winner in fresh board")
    void test10() {
        assertThat(IsOver.gameOver(board,'X')).isFalse();
        assertThat(IsOver.gameOver(board,'O')).isFalse();
    }
    @Test
    @DisplayName("Full board with no winner is finished")
    void test11() {
        assertThat(IsOver.isFinished(board1)).isTrue();
    }
    @Test
    @DisplayName("Fresh board is not finished")
    void test12() {
        assertThat(IsOver.isFinished(board)).isFalse();
    }
    @Test
    @DisplayName("Board with a winner is finished even with empty cells")
    void test13() {
        board[0][0] = 'O';
        board[1][1] = 'O';
        board[2][2] = 'O';
        assertThat(IsOver.isFinished(board)).isTrue();
    }
}



