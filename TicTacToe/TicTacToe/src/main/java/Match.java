// A match of three rounds. Keeps the score and decides who starts each round,
// but, like Game, never reads input or draws anything.
public class Match {
    public enum Result { PLAYER_WINS, COMPUTER_WINS, TIE }

    static final int TOTAL_ROUNDS = 3;

    private final Game game;
    private int round;
    private int playerWins;
    private int computerWins;
    private int ties;
    private boolean roundRecorded;

    public Match() {
        this(new Game());
    }

    // Tests pass a Game with a predictable Random.
    Match(Game game) {
        this.game = game;
        newMatch();
    }

    public void newMatch() {
        round = 1;
        playerWins = 0;
        computerWins = 0;
        ties = 0;
        roundRecorded = false;
        game.reset(firstTurnFor(round));
    }

    // Call once when the current round's game has ended, to add it to the score.
    public void recordRound() {
        if (game.getStatus() == Game.Status.IN_PROGRESS) {
            throw new IllegalStateException("The round is still being played");
        }
        if (roundRecorded) {
            throw new IllegalStateException("This round was already recorded");
        }
        switch (game.getStatus()) {
            case X_WINS -> playerWins++;
            case O_WINS -> computerWins++;
            default -> ties++;
        }
        roundRecorded = true;
    }

    public void nextRound() {
        if (!roundRecorded || isMatchOver()) {
            throw new IllegalStateException("There is no next round to start");
        }
        round++;
        roundRecorded = false;
        game.reset(firstTurnFor(round));
    }

    public boolean isRoundOver() {
        return roundRecorded;
    }

    public boolean isMatchOver() {
        return roundRecorded && round == TOTAL_ROUNDS;
    }

    public Result getResult() {
        if (!isMatchOver()) {
            throw new IllegalStateException("The match is not over yet");
        }
        if (playerWins > computerWins) {
            return Result.PLAYER_WINS;
        }
        if (computerWins > playerWins) {
            return Result.COMPUTER_WINS;
        }
        return Result.TIE;
    }

    // Take turns starting so it's fair: player, computer, player.
    static char firstTurnFor(int round) {
        return round % 2 == 1 ? Game.PLAYER : Game.COMPUTER;
    }

    public Game getGame() {
        return game;
    }

    public int getRound() {
        return round;
    }

    public int getPlayerWins() {
        return playerWins;
    }

    public int getComputerWins() {
        return computerWins;
    }

    public int getTies() {
        return ties;
    }
}
