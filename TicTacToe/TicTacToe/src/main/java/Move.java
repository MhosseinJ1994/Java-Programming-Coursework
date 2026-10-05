import java.util.Random;

public class Move {
    static int[] numberInBoard (int pickedNumber) {
        int[] res = new int[2];
        pickedNumber--;
        res[0] = pickedNumber / 3; //row
        res[1] = pickedNumber % 3; //column
        return res;
    }

    static int[] getComputerNumber(char[][] board, Random random) {
        int[] emptyCells = BoardStructure.getEmptyCells(board);
        return numberInBoard(emptyCells[random.nextInt(emptyCells.length)]);
    }

}
