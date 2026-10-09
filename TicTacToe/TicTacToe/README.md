# Tic-Tac-Toe

A Tic-Tac-Toe game in Java where you play against the computer, in a clickable window or in the terminal.
Games are played as 3-round matches, and every player's results are saved to a leaderboard.

## Features

- **Clickable game window** built with Java Swing
- **3-round matches**: you start rounds 1 and 3, the computer starts round 2
- **Leaderboard**: wins, losses, ties and rounds won are saved per player name and survive closing the game
- **A computer player that thinks**: it takes a winning move, blocks yours, and prefers the center and corners
- **A short "thinking" delay** before the computer moves, without freezing the window
- **Console mode** for playing in the terminal
- **119 automated tests**

## How to run

You need **Java 25** installed. Gradle is included through the wrapper.

```bash
cd TicTacToe/TicTacToe

./gradlew run                       # play in a window
./gradlew run --args="--console"    # play a single game in the terminal
./gradlew test                      # run the tests
```

On Windows, use `gradlew.bat` instead of `./gradlew`.

## How to play

1. Enter your name when the game starts.
2. You are **X** and the computer is **O**. Click an empty cell to make your move.
3. Get three in a row (horizontally, vertically or diagonally) to win the round.
4. After each round, press **Next Round**. After 3 rounds, the player who won more rounds wins the match.
5. Press **Leaderboard** to see the top 10 players, or **New Match** to start over.

The leaderboard is saved in `leaderboard.csv` in the folder the game is started from.

## How the computer chooses a move

The computer follows these rules, in order:

1. **Win:** if it can complete a line, it does.
2. **Block:** if you could complete a line on your next move, it takes that cell.
3. **Center:** takes cell 5 if it is free.
4. **Corner:** takes a free corner.
5. Otherwise, it picks a random empty cell.

It is hard to beat, but not impossible. Try making two threats at once.

## Project structure

```
src/main/java
├── Main.java                 Starts the window, or the console game with --console
├── GameWindow.java           Asks for the player's name and opens the window
├── GamePanel.java            The board, buttons, score line and leaderboard table
├── Game.java                 One round: the board, whose turn it is, and who won
├── Match.java                3 rounds, the score and the match result
├── Leaderboard.java          Players' totals, saved to and loaded from a CSV file
├── MoveStrategy.java         Interface: "choose a cell for the computer"
├── SmartMoveStrategy.java    Win, block, center, corner, then random
├── RandomMoveStrategy.java   Picks any empty cell
├── Move.java                 Cell number ↔ row/column, and finding winning cells
├── BoardStructure.java       Creating, printing and reading the board
├── IsOver.java               Checks rows, columns and diagonals for a winner
└── ValidInput.java           Checks and parses the player's moves

src/test/java                 JUnit tests for every class above
```

## Design

- **Game logic is separate from the screen.** `Game`, `Match` and `Leaderboard` never draw anything,
  so the same logic is used by both the window and the console version, and can be tested without a screen.
- **Strategy pattern.** The computer's way of choosing a move is a `MoveStrategy` object passed into `Game`.
  Adding a new kind of computer player means writing one new class, without changing `Game`.
- **The window never freezes.** Swing runs on a single UI thread (the Event Dispatch Thread), so the
  computer's delay uses a `javax.swing.Timer` instead of `Thread.sleep`.
- **Tested without randomness.** Tests pass in a predictable strategy, so they always know which
  cell the computer will take.

## What I learned

- Object-oriented design: classes, interfaces, records and enums
- Building a GUI with Swing and working with the Event Dispatch Thread
- Reading and writing files, and handling `IOException`
- Writing unit tests with JUnit and AssertJ, including tests for the GUI
- Using Gradle to build, run and test a project
- Using Git to commit and push each step of the project

## Ideas for the future

- An unbeatable computer player using the **minimax** algorithm
- Difficulty levels (easy / medium / hard)
- An AI player using a large language model API
