import java.util.Random;

public class RandomMoveStrategy implements MoveStrategy{
    private final Random random;
    public RandomMoveStrategy(Random random) {
        this.random = random;
    }
    @Override
    public  int chooseMove(char[][] board){
        int[] emptyCells = BoardStructure.getEmptyCells(board);
        int[] pos = Move.numberInBoard(emptyCells[random.nextInt(emptyCells.length)]);
        return pos[0] * BoardStructure.COLS + pos[1] + 1;

    }
}
