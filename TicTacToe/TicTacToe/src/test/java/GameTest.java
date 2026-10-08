import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Random;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

public class GameTest {
    // Always returns 0, so the computer always takes the lowest empty cell.
    static class LowestCellRandom extends Random {
        @Override
        public int nextInt(int bound) {
            return 0;
        }
    }

    Game game;

    @BeforeEach
    void setUp() {
        game = new Game(new RandomMoveStrategy(new GameTest.LowestCellRandom()));
    }

    private void play(int... cells) {
        for (int cell : cells) {
            assertThat(game.playerMove(cell)).isTrue();
            if (game.getStatus() == Game.Status.IN_PROGRESS) {
                game.computerMove();
            }
        }
    }

    @Test
    @DisplayName("New game is in progress and the player goes first")
    void test1() {
        assertThat(game.getStatus()).isEqualTo(Game.Status.IN_PROGRESS);
        assertThat(game.getCurrentTurn()).isEqualTo('X');
        assertThat(game.getCell(5)).isEqualTo('5');
    }

    @Test
    @DisplayName("Player move puts X and hands the turn to the computer")
    void test2() {
        assertThat(game.playerMove(9)).isTrue();
        assertThat(game.getCell(9)).isEqualTo('X');
        assertThat(game.getCurrentTurn()).isEqualTo('O');
    }

    @Test
    @DisplayName("Computer move puts O and hands the turn back")
    void test3() {
        game.playerMove(9);
        int cell = game.computerMove();
        assertThat(cell).isEqualTo(1);
        assertThat(game.getCell(1)).isEqualTo('O');
        assertThat(game.getCurrentTurn()).isEqualTo('X');
    }

    @Test
    @DisplayName("Player cannot move twice in a row")
    void test4() {
        game.playerMove(9);
        assertThat(game.playerMove(8)).isFalse();
        assertThat(game.getCell(8)).isEqualTo('8');
    }

    @Test
    @DisplayName("Player cannot take a taken cell or a cell outside 1 to 9")
    void test5() {
        play(9);
        assertThat(game.playerMove(1)).isFalse();
        assertThat(game.playerMove(0)).isFalse();
        assertThat(game.playerMove(10)).isFalse();
        assertThat(game.getCurrentTurn()).isEqualTo('X');
    }

    @Test
    @DisplayName("Computer cannot move on the player's turn")
    void test6() {
        assertThatThrownBy(() -> game.computerMove())
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Player wins and the computer cannot move afterwards")
    void test7() {
        play(9, 8, 7);
        assertThat(game.getStatus()).isEqualTo(Game.Status.X_WINS);
        assertThatThrownBy(() -> game.computerMove())
                .isInstanceOf(IllegalStateException.class);
        assertThat(game.getCell(3)).isEqualTo('3');
    }

    @Test
    @DisplayName("Computer wins with the top row")
    void test8() {
        play(9, 8, 5);
        assertThat(game.getStatus()).isEqualTo(Game.Status.O_WINS);
    }

    @Test
    @DisplayName("No moves are allowed after the game is over")
    void test9() {
        play(9, 8, 5);
        assertThat(game.playerMove(4)).isFalse();
    }

    @Test
    @DisplayName("Full board with no winner is a tie, and filling it doesn't crash")
    void test10() {
        play(2, 4, 5, 7, 9);
        assertThat(game.getStatus()).isEqualTo(Game.Status.TIE);
        assertThat(game.boardToString()).isEqualTo(
                " O  X  O \n"
                + " X  X  O \n"
                + " X  O  X \n");
    }

    @Test
    @DisplayName("Reset clears the board and gives the turn back to the player")
    void test11() {
        play(9, 8, 7);
        game.reset();
        assertThat(game.getStatus()).isEqualTo(Game.Status.IN_PROGRESS);
        assertThat(game.getCurrentTurn()).isEqualTo('X');
        assertThat(game.getCell(9)).isEqualTo('9');
    }

    @Test
    @DisplayName("Reset can let the computer go first")
    void test12() {
        game.reset(Game.COMPUTER);
        assertThat(game.getCurrentTurn()).isEqualTo('O');
        assertThat(game.playerMove(5)).isFalse();
        assertThat(game.computerMove()).isEqualTo(1);
        assertThat(game.getCurrentTurn()).isEqualTo('X');
    }
}
