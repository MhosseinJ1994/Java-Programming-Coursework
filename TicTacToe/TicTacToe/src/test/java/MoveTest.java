import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.util.Random;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

public class MoveTest {
    char[][] board = new char[3][3];

    @Test
    @DisplayName("is move equal to 1 has a result of [0,0]")
    void test1() {
        int[] result = Move.numberInBoard(1);
        assertThat(result).containsExactly(0,0);
    }

    @Test
    @DisplayName("is 5 in center")
    void test2() {
        int[] result = Move.numberInBoard(5);
        assertThat(result).containsExactly(1,1);
    }
    @Test
    @DisplayName("is 9 in down right")
    void test3() {
        int[] result = Move.numberInBoard(9);
        assertThat(result).containsExactly(2,2);
    }
    @Test
    @DisplayName("Will computer pick 8")
    void test4() {
        BoardStructure.fillBoard(board);
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                board[i][j] = 'X';
            }
        }
        board[2][1] = '8';
        int[] result = Move.getComputerNumber(board, new Random());
        assertThat(result).containsExactly(2,1);
    }
    @ParameterizedTest
    @CsvSource({
            "1, 0, 0",
            "2, 0, 1",
            "3, 0, 2",
            "4, 1, 0",
            "5, 1, 1",
            "6, 1, 2",
            "7, 2, 0",
            "8, 2, 1",
            "9, 2, 2"
    })
    void numberInBoardConvertsCorrectly(int number, int row, int col) {
        assertThat(Move.numberInBoard(number)).containsExactly(row, col);
    }

    @Test
    @DisplayName("Board will not change after computer pick")
    void test5() {
        BoardStructure.fillBoard(board);
        char[][] copy = new char[3][3];
        for (int i = 0; i < 3; i++) {
            copy[i] = board[i].clone();
        }
        Move.getComputerNumber(board,new Random());
        assertThat(board).isDeepEqualTo(copy);

    }
}
