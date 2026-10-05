import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

public class ValidInputTest {
    char[][] board = new char[3][3];

    @BeforeEach
    void setUp() {
        BoardStructure.fillBoard(board);
    }

    @ParameterizedTest
    @ValueSource(ints = {1, 2, 3, 4, 5, 6, 7, 8, 9})
    @DisplayName("Every cell 1 to 9 is playable on a fresh board")
    void test1(int cell) {
        assertThat(ValidInput.isInvalid(cell, board)).isFalse();
    }

    @ParameterizedTest
    @ValueSource(ints = {0, 10, -1, 100})
    @DisplayName("Numbers outside 1 to 9 are rejected")
    void test2(int cell) {
        assertThat(ValidInput.isInvalid(cell, board)).isTrue();
    }

    @Test
    @DisplayName("Taken cell is rejected")
    void test3() {
        board[2][2] = 'O';
        assertThat(ValidInput.isInvalid(9, board)).isTrue();
    }

    @ParameterizedTest
    @ValueSource(strings = {"a", "", "  ", "5a", "4.5"})
    @DisplayName("Text that is not a number becomes INVALID")
    void test4(String line) {
        assertThat(ValidInput.parseMove(line)).isEqualTo(ValidInput.INVALID);
    }

    @Test
    @DisplayName("Numbers with spaces around them are read")
    void test5() {
        assertThat(ValidInput.parseMove(" 7 ")).isEqualTo(7);
    }
}
