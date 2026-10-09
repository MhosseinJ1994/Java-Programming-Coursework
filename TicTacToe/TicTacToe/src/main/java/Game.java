// One round of tic-tac-toe. Holds the board and whose turn it is,
// but never reads input or prints, so both the console and a GUI can use it.
public class Game {
    public enum Status { IN_PROGRESS, X_WINS, O_WINS, TIE }

    static final char PLAYER = 'X';
    static final char COMPUTER = 'O';

    private final char[][] board = new char[BoardStructure.ROWS][BoardStructure.COLS];
    private final MoveStrategy moveStrategy;
    private char currentTurn;

    public Game() {
        this(new SmartMoveStrategy());
    }

    // Lets you choose how the computer plays, e.g. RandomMoveStrategy or SmartMoveStrategy.
    // Tests pass a predictable strategy so they know which cell the computer takes.
    Game(MoveStrategy moveStrategy) {
        this.moveStrategy = moveStrategy;
        reset();
    }

    public void reset() {
        reset(PLAYER);
    }

    // firstTurn is PLAYER or COMPUTER: who makes the first move of this round.
    public void reset(char firstTurn) {
        BoardStructure.fillBoard(board);
        currentTurn = firstTurn;
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
        int cell = moveStrategy.chooseMove(board);
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
