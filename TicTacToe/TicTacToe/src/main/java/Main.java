import java.util.Random;
import java.util.Scanner;


public class Main {
    private static final int ROWS = 3;
    private static final int COLS = 3;

    static void main() {
        char[][] board = new char[ROWS][COLS];
        Scanner scanner = new Scanner(System.in);
        Random random = new Random();
        BoardStructure.fillBoard(board);
        BoardStructure.printBoard(board);


        while(!IsOver.gameOver(board,'X')
                && !IsOver.gameOver(board,'O')
                && BoardStructure.getEmptyCells(board).length > 0) {
            System.out.print("Please Insert you MOVE: ");
            int inputs = scanner.nextInt();
            if(ValidInput.isValid(inputs,board) || inputs <= 0 || inputs >= 9){
                System.out.print("Wrong Input please choose Again!: ");
                continue;
            }
            int[] plyerMove = Move.numberInBoard(inputs);
            board[plyerMove[0]][plyerMove[1]] = 'X';
            int[] computerMove = Move.getComputerNumber(board, random);
            board[computerMove[0]][computerMove[1]] = 'O';
            BoardStructure.printBoard(board);

        }

        winner(board);

    }

    static void winner(char[][] board) {
        if(IsOver.gameOver(board,'X')) {
            System.out.println("You Won!");
        }else if(IsOver.gameOver(board,'O')) {
            System.out.println("Computer Won!");

        }else {
            System.out.println("It's a Tie");
        }
    }

}
