public class Move {
    static int[] numberInBoard (int pickedNumber) {
        int[] res = new int[2];
        pickedNumber--;
        res[0] = pickedNumber / 3; //row
        res[1] = pickedNumber % 3; //column
        return res;
    }

    static int findWinningCell(char[][] board, char symbol) {
        int[] emptyCells = BoardStructure.getEmptyCells(board);
        for(int cell:emptyCells){
            boolean winner = false;
            int[] spot = numberInBoard(cell);
            board[spot[0]][spot[1]] = symbol;
            if(IsOver.gameOver(board,symbol)) {
                winner = true;
            }
            board[spot[0]][spot[1]] =(char) ('0' + cell);
            if(winner) {
                return cell;
            }
        }
        return -1;
    }

}
