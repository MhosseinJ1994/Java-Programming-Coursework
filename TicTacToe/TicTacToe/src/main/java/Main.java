import java.util.Scanner;


public class Main {

    static void main() {
        Game game = new Game();
        Scanner scanner = new Scanner(System.in);
        System.out.print(game.boardToString());


        while (game.getStatus() == Game.Status.IN_PROGRESS) {
            System.out.print("Please Insert your MOVE: ");
            int input = ValidInput.parseMove(scanner.nextLine());
            if (!game.playerMove(input)) {
                System.out.println("Wrong Input! Pick an empty cell from 1 to 9.");
                continue;
            }
            if (game.getStatus() == Game.Status.IN_PROGRESS) {
                game.computerMove();
            }
            System.out.print(game.boardToString());
        }

        winner(game.getStatus());

    }

    static void winner(Game.Status status) {
        switch (status) {
            case X_WINS -> System.out.println("You Won!");
            case O_WINS -> System.out.println("Computer Won!");
            default -> System.out.println("It's a Tie");
        }
    }

}
