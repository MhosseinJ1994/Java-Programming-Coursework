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

// The clickable board. Every click goes through Game/Match, then refresh() redraws
// everything from their state so the screen can never disagree with the rules.
// Cell buttons are never disabled (Swing would grey out the X/O colours); Game itself
// ignores clicks on taken cells, after the game ends, or while the computer thinks.
public class GamePanel extends JPanel {
    static final int COMPUTER_DELAY_MS = 700;

    private static final Color X_COLOR = new Color(0x1E6FD9);
    private static final Color O_COLOR = new Color(0xD9431E);
    private static final Color CELL_BACKGROUND = Color.WHITE;
    private static final Color CELL_BORDER = new Color(0xB0B0B0);
    private static final Font CELL_FONT = new Font(Font.SANS_SERIF, Font.BOLD, 48);
    private static final Font STATUS_FONT = new Font(Font.SANS_SERIF, Font.PLAIN, 18);
    private static final Font SCORE_FONT = new Font(Font.SANS_SERIF, Font.PLAIN, 14);

    private final Match match;
    private final Game game;
    private final JButton[] cells = new JButton[9];
    private final JLabel status = new JLabel("", SwingConstants.CENTER);
    private final JLabel score = new JLabel("", SwingConstants.CENTER);
    private final JButton nextRound = new JButton("Next Round");
    private final JButton newMatch = new JButton("New Match");
    // Waits without freezing the window, then runs the computer's move on the EDT.
    private final Timer computerTimer;

    public GamePanel(Match match) {
        this(match, COMPUTER_DELAY_MS);
    }

    // Tests pass a short delay so they don't have to wait 0.7s per move.
    GamePanel(Match match, int computerDelayMs) {
        this.match = match;
        this.game = match.getGame();
        computerTimer = new Timer(computerDelayMs, e -> computerTurn());
        computerTimer.setRepeats(false);

        setLayout(new BorderLayout(0, 10));
        setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));

        status.setFont(STATUS_FONT);
        score.setFont(SCORE_FONT);
        JPanel top = new JPanel(new GridLayout(2, 1, 0, 4));
        top.add(score);
        top.add(status);
        add(top, BorderLayout.NORTH);

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

        nextRound.addActionListener(e -> {
            match.nextRound();
            startRound();
        });
        newMatch.addActionListener(e -> {
            computerTimer.stop();   // cancel a computer move that is still waiting
            match.newMatch();
            startRound();
        });
        JPanel buttons = new JPanel(new GridLayout(1, 2, 6, 0));
        buttons.add(nextRound);
        buttons.add(newMatch);
        add(buttons, BorderLayout.SOUTH);

        startRound();
    }

    // In round 2 the computer goes first, so it has to be woken up here.
    private void startRound() {
        refresh();
        if (game.getCurrentTurn() == Game.COMPUTER) {
            computerTimer.restart();
        }
    }

    private void onCellClicked(int cell) {
        if (!game.playerMove(cell)) {
            return;
        }
        afterMove();
        if (game.getStatus() == Game.Status.IN_PROGRESS) {
            computerTimer.restart();
        }
    }

    private void computerTurn() {
        game.computerMove();
        afterMove();
    }

    // The only place a finished round is added to the score.
    private void afterMove() {
        if (game.getStatus() != Game.Status.IN_PROGRESS) {
            match.recordRound();
        }
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
        score.setText("Round " + match.getRound() + " of " + Match.TOTAL_ROUNDS
                + "   ·   You " + match.getPlayerWins()
                + "  Computer " + match.getComputerWins()
                + "  Ties " + match.getTies());
        status.setText(statusMessage());
        nextRound.setEnabled(match.isRoundOver() && !match.isMatchOver());
    }

    private String statusMessage() {
        if (match.isMatchOver()) {
            return switch (match.getResult()) {
                case PLAYER_WINS -> "You won the match!";
                case COMPUTER_WINS -> "Computer won the match!";
                case TIE -> "The match is a tie!";
            };
        }
        return switch (game.getStatus()) {
            case IN_PROGRESS -> game.getCurrentTurn() == Game.PLAYER
                    ? "Your turn (X)"
                    : "Computer thinking…";
            case X_WINS -> "You won round " + match.getRound() + "!";
            case O_WINS -> "Computer won round " + match.getRound() + "!";
            case TIE -> "Round " + match.getRound() + " is a tie";
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

    JButton getNextRoundButton() {
        return nextRound;
    }

    JButton getNewMatchButton() {
        return newMatch;
    }

    String getStatusText() {
        return status.getText();
    }

    String getScoreText() {
        return score.getText();
    }
}
