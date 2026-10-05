import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;

// The clickable board. Every click goes through Game, then refresh() redraws
// everything from the Game state so the screen can never disagree with the rules.
// Buttons are never disabled (Swing would grey out the X/O colours); Game itself
// ignores clicks on taken cells or after the game ends.
public class GamePanel extends JPanel {
    private static final Color X_COLOR = new Color(0x1E6FD9);
    private static final Color O_COLOR = new Color(0xD9431E);
    private static final Color CELL_BACKGROUND = Color.WHITE;
    private static final Color CELL_BORDER = new Color(0xB0B0B0);
    private static final Font CELL_FONT = new Font(Font.SANS_SERIF, Font.BOLD, 48);
    private static final Font STATUS_FONT = new Font(Font.SANS_SERIF, Font.PLAIN, 18);

    private final Game game;
    private final JButton[] cells = new JButton[9];
    private final JLabel status = new JLabel("", SwingConstants.CENTER);
    private final JButton newGame = new JButton("New Game");

    public GamePanel(Game game) {
        this.game = game;
        setLayout(new BorderLayout(0, 10));
        setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));

        status.setFont(STATUS_FONT);
        add(status, BorderLayout.NORTH);

        JPanel grid = new JPanel(new GridLayout(3, 3, 6, 6));
        for (int i = 0; i < cells.length; i++) {
            int cell = i + 1;
            JButton button = new JButton();
            button.setFont(CELL_FONT);
            button.setPreferredSize(new Dimension(100, 100));
            button.setFocusPainted(false);
            // Draw a plain square ourselves so it looks the same on every OS.
            button.setContentAreaFilled(false);
            button.setOpaque(true);
            button.setBackground(CELL_BACKGROUND);
            button.setBorder(BorderFactory.createLineBorder(CELL_BORDER, 2));
            button.addActionListener(e -> onCellClicked(cell));
            cells[i] = button;
            grid.add(button);
        }
        add(grid, BorderLayout.CENTER);

        newGame.addActionListener(e -> {
            game.reset();
            refresh();
        });
        add(newGame, BorderLayout.SOUTH);

        refresh();
    }

    private void onCellClicked(int cell) {
        if (!game.playerMove(cell)) {
            return;
        }
        if (game.getStatus() == Game.Status.IN_PROGRESS) {
            game.computerMove();
        }
        refresh();
    }

    private void refresh() {
        boolean playing = game.getStatus() == Game.Status.IN_PROGRESS;
        for (int i = 0; i < cells.length; i++) {
            char symbol = game.getCell(i + 1);
            JButton button = cells[i];
            boolean empty = Character.isDigit(symbol);
            button.setText(empty ? "" : String.valueOf(symbol));
            button.setForeground(symbol == Game.PLAYER ? X_COLOR : O_COLOR);
            button.setCursor(Cursor.getPredefinedCursor(
                    empty && playing ? Cursor.HAND_CURSOR : Cursor.DEFAULT_CURSOR));
        }
        status.setText(statusMessage(game.getStatus()));
    }

    private static String statusMessage(Game.Status status) {
        return switch (status) {
            case IN_PROGRESS -> "Your turn (X)";
            case X_WINS -> "You Won!";
            case O_WINS -> "Computer Won!";
            case TIE -> "It's a Tie";
        };
    }

    // For tests.
    JButton getCellButton(int cell) {
        return cells[cell - 1];
    }

    boolean looksClickable(int cell) {
        return cells[cell - 1].getCursor().getType() == Cursor.HAND_CURSOR;
    }

    JButton getNewGameButton() {
        return newGame;
    }

    String getStatusText() {
        return status.getText();
    }
}
