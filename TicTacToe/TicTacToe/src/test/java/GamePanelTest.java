import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import javax.swing.SwingUtilities;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Supplier;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

public class GamePanelTest {
    static final int TEST_DELAY_MS = 20;

    GamePanel panel;

    @BeforeEach
    void setUp() throws Exception {
        onEdt(() -> panel = new GamePanel(new Game(new GameTest.LowestCellRandom()), TEST_DELAY_MS));
    }

    // Swing objects must only be touched on the EDT, in tests too.
    private void onEdt(Runnable action) throws Exception {
        SwingUtilities.invokeAndWait(action);
    }

    private <T> T readOnEdt(Supplier<T> read) throws Exception {
        AtomicReference<T> result = new AtomicReference<>();
        onEdt(() -> result.set(read.get()));
        return result.get();
    }

    private void waitForComputer() throws Exception {
        long deadline = System.currentTimeMillis() + 2000;
        while (readOnEdt(() -> panel.getStatusText()).equals("Computer thinking…")) {
            assertThat(System.currentTimeMillis()).isLessThan(deadline);
            Thread.sleep(5);
        }
    }

    // Clicks each cell and lets the computer answer before the next click.
    private void click(int... cells) throws Exception {
        for (int cell : cells) {
            onEdt(() -> panel.getCellButton(cell).doClick(0));
            waitForComputer();
        }
    }

    private String text(int cell) throws Exception {
        return readOnEdt(() -> panel.getCellButton(cell).getText());
    }

    @Test
    @DisplayName("New board has 9 empty, clickable buttons")
    void test1() throws Exception {
        for (int cell = 1; cell <= 9; cell++) {
            int c = cell;
            assertThat(text(c)).isEmpty();
            assertThat(readOnEdt(() -> panel.looksClickable(c))).isTrue();
        }
        assertThat(readOnEdt(() -> panel.getStatusText())).isEqualTo("Your turn (X)");
    }

    @Test
    @DisplayName("Clicking a cell shows X there and the computer's O")
    void test2() throws Exception {
        click(9);
        assertThat(text(9)).isEqualTo("X");
        assertThat(text(1)).isEqualTo("O");
    }

    @Test
    @DisplayName("Taken cells don't look clickable and clicking them changes nothing")
    void test3() throws Exception {
        click(9);
        assertThat(readOnEdt(() -> panel.looksClickable(9))).isFalse();
        assertThat(readOnEdt(() -> panel.looksClickable(1))).isFalse();
        assertThat(readOnEdt(() -> panel.looksClickable(5))).isTrue();

        click(1);
        assertThat(text(1)).isEqualTo("O");
        assertThat(text(2)).isEmpty();
    }

    @Test
    @DisplayName("Player win is shown and the board locks")
    void test4() throws Exception {
        click(9, 8, 7);
        assertThat(readOnEdt(() -> panel.getStatusText())).isEqualTo("You Won!");
        for (int cell = 1; cell <= 9; cell++) {
            int c = cell;
            assertThat(readOnEdt(() -> panel.looksClickable(c))).isFalse();
        }
    }

    @Test
    @DisplayName("Computer win is shown")
    void test5() throws Exception {
        click(9, 8, 5);
        assertThat(readOnEdt(() -> panel.getStatusText())).isEqualTo("Computer Won!");
    }

    @Test
    @DisplayName("Tie is shown")
    void test6() throws Exception {
        click(2, 4, 5, 7, 9);
        assertThat(readOnEdt(() -> panel.getStatusText())).isEqualTo("It's a Tie");
    }

    @Test
    @DisplayName("Clicking a locked board after the game ends does nothing")
    void test7() throws Exception {
        click(9, 8, 7);
        click(3);
        assertThat(text(3)).isEmpty();
    }

    @Test
    @DisplayName("New Game clears the board")
    void test8() throws Exception {
        click(9, 8, 7);
        onEdt(() -> panel.getNewGameButton().doClick(0));
        assertThat(readOnEdt(() -> panel.getStatusText())).isEqualTo("Your turn (X)");
        for (int cell = 1; cell <= 9; cell++) {
            int c = cell;
            assertThat(text(c)).isEmpty();
            assertThat(readOnEdt(() -> panel.looksClickable(c))).isTrue();
        }
    }

    @Test
    @DisplayName("X shows at once, then the computer thinks before answering")
    void test9() throws Exception {
        onEdt(() -> panel.getCellButton(9).doClick(0));

        assertThat(text(9)).isEqualTo("X");
        assertThat(text(1)).isEmpty();
        assertThat(readOnEdt(() -> panel.getStatusText())).isEqualTo("Computer thinking…");
        assertThat(readOnEdt(() -> panel.isComputerThinking())).isTrue();

        waitForComputer();
        assertThat(text(1)).isEqualTo("O");
        assertThat(readOnEdt(() -> panel.getStatusText())).isEqualTo("Your turn (X)");
    }

    @Test
    @DisplayName("Board is locked while the computer is thinking")
    void test10() throws Exception {
        onEdt(() -> {
            panel.getCellButton(9).doClick(0);
            panel.getCellButton(5).doClick(0);   // second click before the computer answers
        });
        assertThat(text(5)).isEmpty();
        assertThat(readOnEdt(() -> panel.looksClickable(5))).isFalse();

        waitForComputer();
        assertThat(text(5)).isEmpty();
        assertThat(readOnEdt(() -> panel.looksClickable(5))).isTrue();
    }

    @Test
    @DisplayName("New Game while the computer is thinking cancels its move")
    void test11() throws Exception {
        onEdt(() -> {
            panel.getCellButton(9).doClick(0);
            panel.getNewGameButton().doClick(0);
        });
        assertThat(readOnEdt(() -> panel.isComputerThinking())).isFalse();

        Thread.sleep(TEST_DELAY_MS * 5);   // past the moment the computer would have moved
        for (int cell = 1; cell <= 9; cell++) {
            assertThat(text(cell)).isEmpty();
        }
        assertThat(readOnEdt(() -> panel.getStatusText())).isEqualTo("Your turn (X)");
    }
}
