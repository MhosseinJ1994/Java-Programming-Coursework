import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

public class MatchTest {
    Match match;
    Game game;

    @BeforeEach
    void setUp() {
        game = new Game(new RandomMoveStrategy(new GameTest.LowestCellRandom()));
        match = new Match(game);
    }

    // Plays one whole round (the computer takes the lowest empty cell) and records it.
    private void playRound(int... playerCells) {
        if (game.getCurrentTurn() == Game.COMPUTER) {
            game.computerMove();
        }
        for (int cell : playerCells) {
            assertThat(game.playerMove(cell)).isTrue();
            if (game.getStatus() == Game.Status.IN_PROGRESS) {
                game.computerMove();
            }
        }
        assertThat(game.getStatus()).isNotEqualTo(Game.Status.IN_PROGRESS);
        match.recordRound();
    }

    @Test
    @DisplayName("New match starts at round 1 with no score and the player first")
    void test1() {
        assertThat(match.getRound()).isEqualTo(1);
        assertThat(match.getPlayerWins()).isZero();
        assertThat(match.getComputerWins()).isZero();
        assertThat(match.getTies()).isZero();
        assertThat(game.getCurrentTurn()).isEqualTo(Game.PLAYER);
        assertThat(match.isRoundOver()).isFalse();
    }

    @Test
    @DisplayName("Winning a round adds one to the player's score")
    void test2() {
        playRound(9, 8, 7);
        assertThat(match.getPlayerWins()).isEqualTo(1);
        assertThat(match.isRoundOver()).isTrue();
        assertThat(match.isMatchOver()).isFalse();
    }

    @Test
    @DisplayName("A round can't be recorded while it is still being played")
    void test3() {
        game.playerMove(9);
        assertThatThrownBy(() -> match.recordRound())
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("A round can't be recorded twice")
    void test4() {
        playRound(9, 8, 7);
        assertThatThrownBy(() -> match.recordRound())
                .isInstanceOf(IllegalStateException.class);
        assertThat(match.getPlayerWins()).isEqualTo(1);
    }

    @Test
    @DisplayName("Next round can't start before this round is over")
    void test5() {
        assertThatThrownBy(() -> match.nextRound())
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Round 2 starts on a clean board with the computer first")
    void test6() {
        playRound(9, 8, 7);
        match.nextRound();
        assertThat(match.getRound()).isEqualTo(2);
        assertThat(game.getCurrentTurn()).isEqualTo(Game.COMPUTER);
        assertThat(game.getStatus()).isEqualTo(Game.Status.IN_PROGRESS);
        assertThat(game.getCell(9)).isEqualTo('9');
    }

    @Test
    @DisplayName("Player wins the match 2-1 and no fourth round is allowed")
    void test7() {
        playRound(9, 8, 7);       // round 1: player wins
        match.nextRound();
        playRound(9, 8);          // round 2: computer starts and wins with 1-2-3
        match.nextRound();
        playRound(9, 8, 7);       // round 3: player wins

        assertThat(match.isMatchOver()).isTrue();
        assertThat(match.getPlayerWins()).isEqualTo(2);
        assertThat(match.getComputerWins()).isEqualTo(1);
        assertThat(match.getResult()).isEqualTo(Match.Result.PLAYER_WINS);
        assertThatThrownBy(() -> match.nextRound())
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Computer wins the match 2-0 with one tie")
    void test8() {
        playRound(9, 8, 5);       // round 1: computer wins
        match.nextRound();
        playRound(9, 8);          // round 2: computer wins
        match.nextRound();
        playRound(2, 4, 5, 7, 9); // round 3: tie

        assertThat(match.getComputerWins()).isEqualTo(2);
        assertThat(match.getTies()).isEqualTo(1);
        assertThat(match.getResult()).isEqualTo(Match.Result.COMPUTER_WINS);
    }

    @Test
    @DisplayName("One win each and a tie is a tied match")
    void test9() {
        playRound(9, 8, 7);       // round 1: player wins
        match.nextRound();
        playRound(9, 8);          // round 2: computer wins
        match.nextRound();
        playRound(2, 4, 5, 7, 9); // round 3: tie

        assertThat(match.getResult()).isEqualTo(Match.Result.TIE);
    }

    @Test
    @DisplayName("Player wins round 2 even when the computer starts")
    void test10() {
        playRound(9, 8, 7);
        match.nextRound();
        playRound(5, 3, 7);       // computer takes 1, 2, 4; player gets the 3-5-7 diagonal
        assertThat(match.getPlayerWins()).isEqualTo(2);
    }

    @Test
    @DisplayName("Result can't be asked for before the match is over")
    void test11() {
        playRound(9, 8, 7);
        assertThatThrownBy(() -> match.getResult())
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("New match clears the score and goes back to round 1")
    void test12() {
        playRound(9, 8, 7);
        match.nextRound();
        match.newMatch();
        assertThat(match.getRound()).isEqualTo(1);
        assertThat(match.getPlayerWins()).isZero();
        assertThat(match.isRoundOver()).isFalse();
        assertThat(game.getCurrentTurn()).isEqualTo(Game.PLAYER);
    }

    @Test
    @DisplayName("Rounds alternate who starts: player, computer, player")
    void test13() {
        assertThat(Match.firstTurnFor(1)).isEqualTo(Game.PLAYER);
        assertThat(Match.firstTurnFor(2)).isEqualTo(Game.COMPUTER);
        assertThat(Match.firstTurnFor(3)).isEqualTo(Game.PLAYER);
    }
}
