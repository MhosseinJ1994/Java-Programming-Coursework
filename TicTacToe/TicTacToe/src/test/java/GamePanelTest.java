import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

public class GamePanelTest {
    GamePanel panel;

    @BeforeEach
    void setUp() {
        panel = new GamePanel(new Game(new GameTest.LowestCellRandom()));
    }

    private void click(int... cells) {
        for (int cell : cells) {
            panel.getCellButton(cell).doClick(0);
        }
    }

    @Test
    @DisplayName("New board has 9 empty, clickable buttons")
    void test1() {
        for (int cell = 1; cell <= 9; cell++) {
            assertThat(panel.getCellButton(cell).getText()).isEmpty();
            assertThat(panel.looksClickable(cell)).isTrue();
        }
        assertThat(panel.getStatusText()).isEqualTo("Your turn (X)");
    }

    @Test
    @DisplayName("Clicking a cell shows X there and the computer's O")
    void test2() {
        click(9);
        assertThat(panel.getCellButton(9).getText()).isEqualTo("X");
        assertThat(panel.getCellButton(1).getText()).isEqualTo("O");
    }

    @Test
    @DisplayName("Taken cells don't look clickable and clicking them changes nothing")
    void test3() {
        click(9);
        assertThat(panel.looksClickable(9)).isFalse();
        assertThat(panel.looksClickable(1)).isFalse();
        assertThat(panel.looksClickable(5)).isTrue();

        click(1);
        assertThat(panel.getCellButton(1).getText()).isEqualTo("O");
        assertThat(panel.getCellButton(2).getText()).isEmpty();
    }

    @Test
    @DisplayName("Player win is shown and the board locks")
    void test4() {
        click(9, 8, 7);
        assertThat(panel.getStatusText()).isEqualTo("You Won!");
        for (int cell = 1; cell <= 9; cell++) {
            assertThat(panel.looksClickable(cell)).isFalse();
        }
    }

    @Test
    @DisplayName("Computer win is shown")
    void test5() {
        click(9, 8, 5);
        assertThat(panel.getStatusText()).isEqualTo("Computer Won!");
    }

    @Test
    @DisplayName("Tie is shown")
    void test6() {
        click(2, 4, 5, 7, 9);
        assertThat(panel.getStatusText()).isEqualTo("It's a Tie");
    }

    @Test
    @DisplayName("Clicking a locked board after the game ends does nothing")
    void test7() {
        click(9, 8, 7);
        click(3);
        assertThat(panel.getCellButton(3).getText()).isEmpty();
    }

    @Test
    @DisplayName("New Game clears the board")
    void test8() {
        click(9, 8, 7);
        panel.getNewGameButton().doClick(0);
        assertThat(panel.getStatusText()).isEqualTo("Your turn (X)");
        for (int cell = 1; cell <= 9; cell++) {
            assertThat(panel.getCellButton(cell).getText()).isEmpty();
            assertThat(panel.looksClickable(cell)).isTrue();
        }
    }
}
