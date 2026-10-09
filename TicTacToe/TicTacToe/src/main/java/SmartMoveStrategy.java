import java.util.Random;

public class SmartMoveStrategy implements MoveStrategy{
    private final RandomMoveStrategy fallback = new RandomMoveStrategy(new Random());
    private static final int[] corners = {1,3,7,9};

    @Override
    public int chooseMove(char[][] board) {
        int win = Move.findWinningCell(board,Game.COMPUTER );

        if(win != -1) {
            return win;
        }

        int block = Move.findWinningCell(board,Game.PLAYER);
        if(block != -1) {
            return block;
        }
        if(!ValidInput.isInvalid(5,board)) {
            return 5;
        }
        for(int corner : corners) {
            if(!ValidInput.isInvalid(corner,board)) {
                return corner;
            }
        }

        return fallback.chooseMove(board);

    }
}
