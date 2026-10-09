import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Random;

import static org.assertj.core.api.Assertions.assertThat;

public class SmartMoveStrategyTest {
    SmartMoveStrategy strategy = new SmartMoveStrategy();

    // Builds a board from 3 rows, e.g. board("OO3", "XX6", "789").
    // A digit means the cell is empty, just like in the real game.
    static char[][] board(String row1, String row2, String row3) {
        return new char[][] {row1.toCharArray(), row2.toCharArray(), row3.toCharArray()};
    }

    @Test
    @DisplayName("findWinningCell finds a row, a column and both diagonals")
    void test1() {
        assertThat(Move.findWinningCell(board("OO3", "XX6", "789"), 'O')).isEqualTo(3);
        assertThat(Move.findWinningCell(board("OO3", "XX6", "789"), 'X')).isEqualTo(6);
        assertThat(Move.findWinningCell(board("X23", "X56", "789"), 'X')).isEqualTo(7);
        assertThat(Move.findWinningCell(board("O23", "4O6", "789"), 'O')).isEqualTo(9);
        assertThat(Move.findWinningCell(board("12O", "4O6", "789"), 'O')).isEqualTo(7);
    }

    @Test
    @DisplayName("findWinningCell returns -1 when there is no winning move")
    void test2() {
        assertThat(Move.findWinningCell(board("123", "456", "789"), 'X')).isEqualTo(-1);
        assertThat(Move.findWinningCell(board("OO3", "X56", "789"), 'X')).isEqualTo(-1);
    }

    @Test
    @DisplayName("findWinningCell puts the board back the way it was")
    void test3() {
        char[][] board = board("OO3", "X56", "789");
        Move.findWinningCell(board, 'X');
        Move.findWinningCell(board, 'O');
        assertThat(board).isDeepEqualTo(board("OO3", "X56", "789"));
    }

    @Test
    @DisplayName("Takes a win before blocking the player")
    void test4() {
        // O can win at 3, X could win at 6: winning is better.
        assertThat(strategy.chooseMove(board("OO3", "XX6", "789"))).isEqualTo(3);
    }

    @Test
    @DisplayName("Blocks the player's winning cell")
    void test5() {
        assertThat(strategy.chooseMove(board("XX3", "4O6", "789"))).isEqualTo(3);
        assertThat(strategy.chooseMove(board("X23", "4O6", "X89"))).isEqualTo(4);
    }

    @Test
    @DisplayName("Takes the center when there is nothing to win or block")
    void test6() {
        assertThat(strategy.chooseMove(board("X23", "456", "789"))).isEqualTo(5);
    }

    @Test
    @DisplayName("Takes a free corner when the center is taken")
    void test7() {
        assertThat(strategy.chooseMove(board("123", "4X6", "789"))).isIn(1, 3, 7, 9);
        // Corner 1 is taken, so it must pick another one.
        assertThat(strategy.chooseMove(board("O23", "4X6", "789"))).isIn(3, 7, 9);
    }

    @Test
    @DisplayName("Picks an empty side cell when no corner is left")
    void test8() {
        assertThat(strategy.chooseMove(board("X2O", "4O6", "X8X"))).isIn(2, 4, 6, 8);
    }

    @Test
    @DisplayName("Choosing a move doesn't change the board")
    void test9() {
        char[][] board = board("XX3", "4O6", "789");
        strategy.chooseMove(board);
        assertThat(board).isDeepEqualTo(board("XX3", "4O6", "789"));
    }

    @Test
    @DisplayName("Only ever picks empty cells in whole games")
    void test10() {
        Random random = new Random(42);   // fixed seed: the same games every run
        for (int i = 0; i < 200; i++) {
            Game game = new Game(strategy);
            game.reset(i % 2 == 0 ? Game.PLAYER : Game.COMPUTER);
            while (game.getStatus() == Game.Status.IN_PROGRESS) {
                if (game.getCurrentTurn() == Game.COMPUTER) {
                    char[][] before = board(game);
                    int cell = game.computerMove();
                    assertThat(Character.isDigit(before[(cell - 1) / 3][(cell - 1) % 3])).isTrue();
                } else {
                    int[] empty = BoardStructure.getEmptyCells(board(game));
                    game.playerMove(empty[random.nextInt(empty.length)]);
                }
            }
        }
    }

    // A copy of the game's board, read through getCell.
    private static char[][] board(Game game) {
        char[][] board = new char[3][3];
        for (int cell = 1; cell <= 9; cell++) {
            board[(cell - 1) / 3][(cell - 1) % 3] = game.getCell(cell);
        }
        return board;
    }
}
