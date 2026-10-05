import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

public class BoardStructureTest {
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
