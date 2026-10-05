import java.util.Arrays;

public class ValidInput {
    public static boolean isValid(int playerMove, char[][] board) {
        int[] emptyCells = BoardStructure.getEmptyCells(board);

        if(Arrays.stream(emptyCells).anyMatch(x->x == playerMove)){
            return false;
        } else {
            return true;
        }
    }
}
