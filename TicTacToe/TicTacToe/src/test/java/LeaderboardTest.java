import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class LeaderboardTest {
    @TempDir
    Path tempDir;   // a fresh empty folder for each test, deleted afterwards

    Path file;
    Leaderboard leaderboard;

    @BeforeEach
    void setUp() {
        file = tempDir.resolve("leaderboard.csv");
        leaderboard = new Leaderboard(file);
    }

    // Plays a whole 3-round match where the player wins the given rounds
    // (true = player wins that round, false = computer wins).
    private Match matchWhere(boolean... playerWinsRound) {
        Game game = new Game(new RandomMoveStrategy(new GameTest.LowestCellRandom()));
        Match match = new Match(game);
        for (int round = 0; round < Match.TOTAL_ROUNDS; round++) {
            if (round > 0) {
                match.nextRound();
            }
            if (game.getCurrentTurn() == Game.COMPUTER) {
                game.computerMove();   // computer takes 1
            }
            int[] cells = playerWinsRound[round]
                    ? (round == 1 ? new int[] {5, 3, 7} : new int[] {9, 8, 7})
                    : (round == 1 ? new int[] {9, 8} : new int[] {9, 8, 5});
            for (int cell : cells) {
                game.playerMove(cell);
                if (game.getStatus() == Game.Status.IN_PROGRESS) {
                    game.computerMove();
                }
            }
            match.recordRound();
        }
        return match;
    }

    @Test
    @DisplayName("Missing file loads as an empty leaderboard")
    void test1() throws Exception {
        Leaderboard loaded = Leaderboard.load(file);
        assertThat(loaded.top(10)).isEmpty();
    }

    @Test
    @DisplayName("A won match is added to the player's totals")
    void test2() {
        leaderboard.recordMatch("Ali", matchWhere(true, false, true));
        assertThat(leaderboard.get("Ali"))
                .isEqualTo(new Leaderboard.Entry("Ali", 1, 1, 0, 0, 2));
    }

    @Test
    @DisplayName("Several matches add up for the same player")
    void test3() {
        leaderboard.recordMatch("Ali", matchWhere(true, true, true));
        leaderboard.recordMatch("Ali", matchWhere(false, false, true));
        assertThat(leaderboard.get("Ali"))
                .isEqualTo(new Leaderboard.Entry("Ali", 2, 1, 1, 0, 4));
    }

    @Test
    @DisplayName("An unfinished match can't be recorded")
    void test4() {
        Match match = new Match(new Game(new RandomMoveStrategy(new GameTest.LowestCellRandom())));
        assertThatThrownBy(() -> leaderboard.recordMatch("Ali", match))
                .isInstanceOf(IllegalStateException.class);
        assertThat(leaderboard.get("Ali")).isNull();
    }

    @Test
    @DisplayName("Saving and loading gives back the same leaderboard")
    void test5() throws Exception {
        leaderboard.recordMatch("Ali", matchWhere(true, false, true));
        leaderboard.recordMatch("Sara", matchWhere(true, true, true));
        leaderboard.save();

        Leaderboard loaded = Leaderboard.load(file);
        assertThat(loaded.top(10)).isEqualTo(leaderboard.top(10));
        assertThat(Files.readAllLines(file)).containsExactly(
                "name,played,won,lost,tied,roundsWon",
                "Sara,1,1,0,0,3",
                "Ali,1,1,0,0,2");
    }

    @Test
    @DisplayName("Ranking: most matches won, then most rounds won, then name")
    void test6() throws Exception {
        Files.write(file, List.of(
                "name,played,won,lost,tied,roundsWon",
                "Zoe,5,2,3,0,7",
                "Ben,3,3,0,0,8",
                "Amy,4,2,2,0,7",
                "Cat,2,2,0,0,9"));
        List<String> order = Leaderboard.load(file).top(10).stream()
                .map(Leaderboard.Entry::name).toList();
        assertThat(order).containsExactly("Ben", "Cat", "Amy", "Zoe");
    }

    @Test
    @DisplayName("top() returns at most the asked number of players")
    void test7() throws Exception {
        for (int i = 1; i <= 12; i++) {
            leaderboard.recordMatch("P" + i, matchWhere(true, true, true));
        }
        assertThat(leaderboard.top(10)).hasSize(10);
        assertThat(leaderboard.top(50)).hasSize(12);
    }

    @Test
    @DisplayName("Broken lines in the file are skipped, good lines are kept")
    void test8() throws Exception {
        Files.write(file, List.of(
                "name,played,won,lost,tied,roundsWon",
                "Ali,1,1,0,0,2",
                "this line is broken",
                "Sara,two,1,0,0,3",
                ",1,1,0,0,2",
                "",
                "Ben,2,0,2,0,1"));
        List<String> names = Leaderboard.load(file).top(10).stream()
                .map(Leaderboard.Entry::name).toList();
        assertThat(names).containsExactly("Ali", "Ben");
    }

    @Test
    @DisplayName("Names are cleaned so they can't break the file")
    void test9() {
        assertThat(Leaderboard.cleanName("  Ali  ")).isEqualTo("Ali");
        assertThat(Leaderboard.cleanName("A,l\ni")).isEqualTo("Ali");
        assertThat(Leaderboard.cleanName("")).isEqualTo("Player");
        assertThat(Leaderboard.cleanName("   ")).isEqualTo("Player");
        assertThat(Leaderboard.cleanName(null)).isEqualTo("Player");   // Cancel on the name box
        assertThat(Leaderboard.cleanName("A".repeat(30))).hasSize(20);
    }

    @Test
    @DisplayName("Saving fails with an error when the file can't be written")
    void test10() {
        Leaderboard broken = new Leaderboard(tempDir);   // a folder, not a file
        broken.recordMatch("Ali", matchWhere(true, true, true));
        assertThatThrownBy(broken::save).isInstanceOf(java.io.IOException.class);
    }
}
