import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import java.io.IOException;
import java.nio.file.Path;

public class GameWindow {
    // Saved next to where the game is started (the project folder for ./gradlew run).
    static final Path LEADERBOARD_FILE = Path.of("leaderboard.csv");

    // Swing must be built on its own UI thread (the Event Dispatch Thread).
    static void open() {
        SwingUtilities.invokeLater(() -> {
            String name = Leaderboard.cleanName(JOptionPane.showInputDialog(
                    null, "What's your name?", "Tic-Tac-Toe", JOptionPane.QUESTION_MESSAGE));
            Leaderboard leaderboard = loadLeaderboard();

            JFrame frame = new JFrame("Tic-Tac-Toe – " + name);
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setContentPane(new GamePanel(new Match(), leaderboard, name));
            frame.pack();
            frame.setResizable(false);
            frame.setLocationRelativeTo(null);
            frame.setVisible(true);
        });
    }

    // If the file can't be read, warn and play on with an empty leaderboard.
    private static Leaderboard loadLeaderboard() {
        try {
            return Leaderboard.load(LEADERBOARD_FILE);
        } catch (IOException e) {
            JOptionPane.showMessageDialog(null,
                    "Couldn't read the leaderboard: " + e.getMessage(),
                    "Tic-Tac-Toe", JOptionPane.WARNING_MESSAGE);
            return new Leaderboard(LEADERBOARD_FILE);
        }
    }
}
