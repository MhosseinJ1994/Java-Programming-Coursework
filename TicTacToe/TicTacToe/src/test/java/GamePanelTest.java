import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import javax.swing.JTable;
import javax.swing.SwingUtilities;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Supplier;

import static org.assertj.core.api.Assertions.assertThat;

public class GamePanelTest {
    static final int TEST_DELAY_MS = 20;

    @TempDir
    Path tempDir;

    Path file;
    Leaderboard leaderboard;
    GamePanel panel;

    @BeforeEach
    void setUp() throws Exception {
        file = tempDir.resolve("leaderboard.csv");
        leaderboard = new Leaderboard(file);
        onEdt(() -> panel = newPanel(leaderboard));
    }

    private GamePanel newPanel(Leaderboard board) {
        return new GamePanel(new Match(new Game(new GameTest.LowestCellRandom())),
                board, "Ali", TEST_DELAY_MS);
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
        assertThat(readOnEdt(() -> panel.getStatusText())).isEqualTo("You won round 1!");
        for (int cell = 1; cell <= 9; cell++) {
            int c = cell;
            assertThat(readOnEdt(() -> panel.looksClickable(c))).isFalse();
        }
    }

    @Test
    @DisplayName("Computer win is shown")
    void test5() throws Exception {
        click(9, 8, 5);
        assertThat(readOnEdt(() -> panel.getStatusText())).isEqualTo("Computer won round 1!");
    }

    @Test
    @DisplayName("Tie is shown")
    void test6() throws Exception {
        click(2, 4, 5, 7, 9);
        assertThat(readOnEdt(() -> panel.getStatusText())).isEqualTo("Round 1 is a tie");
    }

    @Test
    @DisplayName("Clicking a locked board after the game ends does nothing")
    void test7() throws Exception {
        click(9, 8, 7);
        click(3);
        assertThat(text(3)).isEmpty();
    }

    @Test
    @DisplayName("New Match clears the board")
    void test8() throws Exception {
        click(9, 8, 7);
        onEdt(() -> panel.getNewMatchButton().doClick(0));
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
    @DisplayName("New Match while the computer is thinking cancels its move")
    void test11() throws Exception {
        onEdt(() -> {
            panel.getCellButton(9).doClick(0);
            panel.getNewMatchButton().doClick(0);
        });
        assertThat(readOnEdt(() -> panel.isComputerThinking())).isFalse();

        Thread.sleep(TEST_DELAY_MS * 5);   // past the moment the computer would have moved
        for (int cell = 1; cell <= 9; cell++) {
            assertThat(text(cell)).isEmpty();
        }
        assertThat(readOnEdt(() -> panel.getStatusText())).isEqualTo("Your turn (X)");
    }

    private String status() throws Exception {
        return readOnEdt(() -> panel.getStatusText());
    }

    private boolean nextRoundEnabled() throws Exception {
        return readOnEdt(() -> panel.getNextRoundButton().isEnabled());
    }

    private void pressNextRound() throws Exception {
        onEdt(() -> panel.getNextRoundButton().doClick(0));
        waitForComputer();   // in round 2 the computer moves first
    }

    @Test
    @DisplayName("Score line starts at round 1 with no points")
    void test12() throws Exception {
        assertThat(readOnEdt(() -> panel.getScoreText()))
                .isEqualTo("Round 1 of 3   ·   You 0  Computer 0  Ties 0");
    }

    @Test
    @DisplayName("Next Round is only available after a round ends")
    void test13() throws Exception {
        assertThat(nextRoundEnabled()).isFalse();
        click(9);
        assertThat(nextRoundEnabled()).isFalse();
        click(8, 7);
        assertThat(nextRoundEnabled()).isTrue();
        assertThat(readOnEdt(() -> panel.getScoreText()))
                .isEqualTo("Round 1 of 3   ·   You 1  Computer 0  Ties 0");
    }

    @Test
    @DisplayName("In round 2 the computer moves first by itself")
    void test14() throws Exception {
        click(9, 8, 7);
        onEdt(() -> panel.getNextRoundButton().doClick(0));
        assertThat(status()).isEqualTo("Computer thinking…");
        assertThat(text(9)).isEmpty();

        waitForComputer();
        assertThat(text(1)).isEqualTo("O");
        assertThat(status()).isEqualTo("Your turn (X)");
        assertThat(readOnEdt(() -> panel.getScoreText())).startsWith("Round 2 of 3");
    }

    @Test
    @DisplayName("Full match: player wins 2-1, then New Match starts over")
    void test15() throws Exception {
        click(9, 8, 7);           // round 1: player wins
        pressNextRound();
        click(9, 8);              // round 2: computer wins
        assertThat(status()).isEqualTo("Computer won round 2!");
        pressNextRound();
        click(9, 8, 7);           // round 3: player wins

        assertThat(status()).isEqualTo("You won the match!");
        assertThat(readOnEdt(() -> panel.getScoreText()))
                .isEqualTo("Round 3 of 3   ·   You 2  Computer 1  Ties 0");
        assertThat(nextRoundEnabled()).isFalse();

        onEdt(() -> panel.getNewMatchButton().doClick(0));
        assertThat(readOnEdt(() -> panel.getScoreText()))
                .isEqualTo("Round 1 of 3   ·   You 0  Computer 0  Ties 0");
        assertThat(status()).isEqualTo("Your turn (X)");
    }

    @Test
    @DisplayName("Match result shows a computer win and a tie")
    void test16() throws Exception {
        click(9, 8, 5);           // round 1: computer wins
        pressNextRound();
        click(9, 8);              // round 2: computer wins
        pressNextRound();
        click(2, 4, 5, 7, 9);     // round 3: tie
        assertThat(status()).isEqualTo("Computer won the match!");

        onEdt(() -> panel.getNewMatchButton().doClick(0));
        click(9, 8, 7);
        pressNextRound();
        click(9, 8);
        pressNextRound();
        click(2, 4, 5, 7, 9);
        assertThat(status()).isEqualTo("The match is a tie!");
    }

    // Round 1 player wins, round 2 computer wins, round 3 player wins.
    private void playMatchPlayerWins2to1() throws Exception {
        click(9, 8, 7);
        pressNextRound();
        click(9, 8);
        pressNextRound();
        click(9, 8, 7);
    }

    @Test
    @DisplayName("Nothing is saved before the match is over")
    void test17() throws Exception {
        click(9, 8, 7);
        pressNextRound();
        click(9, 8);
        assertThat(leaderboard.get("Ali")).isNull();
        assertThat(Files.exists(file)).isFalse();
    }

    @Test
    @DisplayName("A finished match is saved to the leaderboard file")
    void test18() throws Exception {
        playMatchPlayerWins2to1();
        assertThat(leaderboard.get("Ali"))
                .isEqualTo(new Leaderboard.Entry("Ali", 1, 1, 0, 0, 2));
        assertThat(Files.readAllLines(file)).contains("Ali,1,1,0,0,2");
    }

    @Test
    @DisplayName("Two matches add up for the same player")
    void test19() throws Exception {
        playMatchPlayerWins2to1();
        onEdt(() -> panel.getNewMatchButton().doClick(0));
        playMatchPlayerWins2to1();
        assertThat(Leaderboard.load(file).get("Ali"))
                .isEqualTo(new Leaderboard.Entry("Ali", 2, 2, 0, 0, 4));
    }

    @Test
    @DisplayName("Leaderboard table shows rank, name and totals")
    void test20() throws Exception {
        playMatchPlayerWins2to1();
        JTable table = readOnEdt(() -> panel.buildLeaderboardTable());
        assertThat(table.getRowCount()).isEqualTo(1);
        assertThat(table.getColumnName(1)).isEqualTo("Name");
        assertThat(table.getValueAt(0, 0)).isEqualTo(1);
        assertThat(table.getValueAt(0, 1)).isEqualTo("Ali");
        assertThat(table.getValueAt(0, 2)).isEqualTo(1);      // won
        assertThat(table.getValueAt(0, 6)).isEqualTo(2);      // rounds won
        assertThat(table.isCellEditable(0, 1)).isFalse();
    }

    @Test
    @DisplayName("If saving fails the game says so and keeps working")
    void test21() throws Exception {
        onEdt(() -> panel = newPanel(new Leaderboard(tempDir)));   // a folder can't be written as a file
        playMatchPlayerWins2to1();
        assertThat(status()).isEqualTo("You won the match! (score not saved)");

        onEdt(() -> panel.getNewMatchButton().doClick(0));
        assertThat(status()).isEqualTo("Your turn (X)");
    }
}
