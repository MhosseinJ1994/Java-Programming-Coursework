import javax.swing.JFrame;
import javax.swing.SwingUtilities;

public class GameWindow {
    // Swing must be built on its own UI thread (the Event Dispatch Thread).
    static void open() {
        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("Tic-Tac-Toe");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setContentPane(new GamePanel(new Match()));
            frame.pack();
            frame.setResizable(false);
            frame.setLocationRelativeTo(null);
            frame.setVisible(true);
        });
    }
}
