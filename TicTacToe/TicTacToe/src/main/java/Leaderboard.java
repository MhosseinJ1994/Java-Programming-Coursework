import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

// Every player's totals across all their matches, saved to a CSV file so the
// scores survive closing the game. Like Game and Match, it never touches the screen.
public class Leaderboard {
    // One line of the leaderboard. A record is a small class that just holds values.
    public record Entry(String name, int played, int won, int lost, int tied, int roundsWon) {}

    static final String HEADER = "name,played,won,lost,tied,roundsWon";
    static final String DEFAULT_NAME = "Player";
    static final int MAX_NAME_LENGTH = 20;

    // Most matches won first; if equal, most rounds won; if still equal, by name.
    private static final Comparator<Entry> RANKING =
            Comparator.comparingInt(Entry::won).reversed()
                    .thenComparing(Comparator.comparingInt(Entry::roundsWon).reversed())
                    .thenComparing(Entry::name);

    private final Path file;
    private final Map<String, Entry> entries = new HashMap<>();   // key: player name

    // An empty leaderboard that will save to this file.
    Leaderboard(Path file) {
        this.file = file;
    }

    // Reads the file. A missing file just means nobody has played yet.
    public static Leaderboard load(Path file) throws IOException {
        Leaderboard leaderboard = new Leaderboard(file);
        if (Files.exists(file)) {
            for (String line : Files.readAllLines(file)) {
                Entry entry = parse(line);
                if (entry != null) {
                    leaderboard.entries.put(entry.name(), entry);
                }
            }
        }
        return leaderboard;
    }

    // Returns null for the header or a broken line, so one bad line doesn't lose everything.
    static Entry parse(String line) {
        String[] parts = line.split(",");
        if (parts.length != 6 || parts[0].isBlank()) {
            return null;
        }
        try {
            return new Entry(parts[0],
                    Integer.parseInt(parts[1]),
                    Integer.parseInt(parts[2]),
                    Integer.parseInt(parts[3]),
                    Integer.parseInt(parts[4]),
                    Integer.parseInt(parts[5]));
        } catch (NumberFormatException e) {
            return null;
        }
    }

    // Commas and line breaks would break the CSV file, so they are removed.
    static String cleanName(String name) {
        if (name == null) {
            return DEFAULT_NAME;
        }
        String cleaned = name.replaceAll("[,\\r\\n]", "").trim();
        if (cleaned.isEmpty()) {
            return DEFAULT_NAME;
        }
        return cleaned.length() > MAX_NAME_LENGTH ? cleaned.substring(0, MAX_NAME_LENGTH) : cleaned;
    }

    // Adds a finished match to this player's totals.
    public void recordMatch(String name, Match match) {
        String player = cleanName(name);
        Match.Result result = match.getResult();
        Entry old = entries.getOrDefault(player, new Entry(player, 0, 0, 0, 0, 0));
        entries.put(player, new Entry(player,
                old.played() + 1,
                old.won() + (result == Match.Result.PLAYER_WINS ? 1 : 0),
                old.lost() + (result == Match.Result.COMPUTER_WINS ? 1 : 0),
                old.tied() + (result == Match.Result.TIE ? 1 : 0),
                old.roundsWon() + match.getPlayerWins()));
    }

    public void save() throws IOException {
        List<String> lines = new ArrayList<>();
        lines.add(HEADER);
        for (Entry e : ranked()) {
            lines.add(e.name() + "," + e.played() + "," + e.won() + ","
                    + e.lost() + "," + e.tied() + "," + e.roundsWon());
        }
        Files.write(file, lines);
    }

    public List<Entry> top(int count) {
        List<Entry> ranked = ranked();
        return ranked.subList(0, Math.min(count, ranked.size()));
    }

    // null if this player hasn't finished a match yet.
    public Entry get(String name) {
        return entries.get(cleanName(name));
    }

    private List<Entry> ranked() {
        List<Entry> list = new ArrayList<>(entries.values());
        list.sort(RANKING);
        return list;
    }
}
