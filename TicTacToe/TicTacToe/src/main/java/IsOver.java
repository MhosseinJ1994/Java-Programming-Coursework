public class IsOver {
    public static boolean gameOver(char[][] board, char symbol) {
        for (char[] chars : board) {
            if (chars[0] == symbol && chars[1] == symbol && chars[2] == symbol) {
                return true;
            }
        }
        for (int i = 0; i < board.length; i++) {
            if (board[0][i] == symbol && board[1][i] == symbol && board[2][i] == symbol) {
                return true;
            }
        }

        if (board[0][0] == symbol && board[1][1] == symbol && board[2][2] == symbol) {
            return true;
        }
        return board[0][2] == symbol && board[1][1] == symbol && board[2][0] == symbol;
    }

}
