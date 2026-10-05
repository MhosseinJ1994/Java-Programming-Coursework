import java.util.Random;

// One round of tic-tac-toe. Holds the board and whose turn it is,
// but never reads input or prints, so both the console and a GUI can use it.
public class Game {
    public enum Status { IN_PROGRESS, X_WINS, O_WINS, TIE }

    static final char PLAYER = 'X';
    static final char COMPUTER = 'O';

    private final char[][] board = new char[BoardStructure.ROWS][BoardStructure.COLS];
    private final Random random;
    private char currentTurn;

    public Game() {
        this(new Random());
    }

    // Tests pass their own Random so the computer's moves are predictable.
    Game(Random random) {
        this.random = random;
        reset();
    }

    public void reset() {
        BoardStructure.fillBoard(board);
        currentTurn = PLAYER;
    }

    // Returns false (and changes nothing) if the move isn't allowed right now.
    public boolean playerMove(int cell) {
        if (getStatus() != Status.IN_PROGRESS
                || currentTurn != PLAYER
                || ValidInput.isInvalid(cell, board)) {
            return false;
        }
        place(cell, PLAYER);
        currentTurn = COMPUTER;
        return true;
    }

    // Returns the cell (1-9) the computer picked.
    public int computerMove() {
        if (getStatus() != Status.IN_PROGRESS || currentTurn != COMPUTER) {
            throw new IllegalStateException("It is not the computer's turn");
        }
        int[] pos = Move.getComputerNumber(board, random);
        int cell = pos[0] * BoardStructure.COLS + pos[1] + 1;
        place(cell, COMPUTER);
        currentTurn = PLAYER;
        return cell;
    }

    public Status getStatus() {
        if (IsOver.gameOver(board, PLAYER)) {
            return Status.X_WINS;
        }
        if (IsOver.gameOver(board, COMPUTER)) {
            return Status.O_WINS;
        }
        if (BoardStructure.getEmptyCells(board).length == 0) {
            return Status.TIE;
        }
        return Status.IN_PROGRESS;
    }

    public char getCurrentTurn() {
        return currentTurn;
    }

    // 'X', 'O', or the cell's digit if it's empty.
    public char getCell(int cell) {
        int[] pos = Move.numberInBoard(cell);
        return board[pos[0]][pos[1]];
    }

    public String boardToString() {
        return BoardStructure.boardToString(board);
    }

    private void place(int cell, char symbol) {
        int[] pos = Move.numberInBoard(cell);
        board[pos[0]][pos[1]] = symbol;
    }
}
