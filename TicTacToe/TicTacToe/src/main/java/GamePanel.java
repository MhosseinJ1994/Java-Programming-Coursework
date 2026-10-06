import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.Timer;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;

// The clickable board. Every click goes through Game, then refresh() redraws
// everything from the Game state so the screen can never disagree with the rules.
// Buttons are never disabled (Swing would grey out the X/O colours); Game itself
// ignores clicks on taken cells, after the game ends, or while the computer thinks.
public class GamePanel extends JPanel {
    static final int COMPUTER_DELAY_MS = 700;

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
    // Waits without freezing the window, then runs the computer's move on the EDT.
    private final Timer computerTimer;

    public GamePanel(Game game) {
        this(game, COMPUTER_DELAY_MS);
    }

    // Tests pass a short delay so they don't have to wait 0.7s per move.
    GamePanel(Game game, int computerDelayMs) {
        this.game = game;
        computerTimer = new Timer(computerDelayMs, e -> computerTurn());
        computerTimer.setRepeats(false);

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
            computerTimer.stop();   // cancel a computer move that is still waiting
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
        refresh();   // show the player's X straight away
        if (game.getStatus() == Game.Status.IN_PROGRESS) {
            computerTimer.restart();
        }
    }

    private void computerTurn() {
        game.computerMove();
        refresh();
    }

    private void refresh() {
        boolean playersTurn = game.getStatus() == Game.Status.IN_PROGRESS
                && game.getCurrentTurn() == Game.PLAYER;
        for (int i = 0; i < cells.length; i++) {
            char symbol = game.getCell(i + 1);
            JButton button = cells[i];
            boolean empty = Character.isDigit(symbol);
            button.setText(empty ? "" : String.valueOf(symbol));
            button.setForeground(symbol == Game.PLAYER ? X_COLOR : O_COLOR);
            button.setCursor(Cursor.getPredefinedCursor(
                    empty && playersTurn ? Cursor.HAND_CURSOR : Cursor.DEFAULT_CURSOR));
        }
        status.setText(statusMessage());
    }

    private String statusMessage() {
        return switch (game.getStatus()) {
            case IN_PROGRESS -> game.getCurrentTurn() == Game.PLAYER
                    ? "Your turn (X)"
                    : "Computer thinking…";
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

    boolean isComputerThinking() {
        return computerTimer.isRunning();
    }

    JButton getNewGameButton() {
        return newGame;
    }

    String getStatusText() {
        return status.getText();
    }
}
