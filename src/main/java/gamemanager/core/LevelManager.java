package gamemanager.core;

import gamemanager.manager.SoundManager;
import gameobject.brick.*;
import gameobject.powerup.Powerup;
import javafx.scene.layout.Pane;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import static gameconfig.GameConfig.*;

public class LevelManager {
    private final List<Brick> bricks = new ArrayList<>();
    private final List<Powerup> powerups = new ArrayList<>();
    public int currentLevel = 1;
    public final int maxLevel = 9;

    private static final String[] PATTERN_ROWS = {
        "11111111111111",
        "22222222222222",
        "U1U1U1U1U1U1U1",
        "U2U2U2U2U2U2U2",
        "55555555555555",
        "12121212121212",
        "UUU11111111UUU",
        "00011111110000",
        "U555555555555U",
        "UU5222222225UU",
        "UUUU521125UUUU",
        "UBBBBBBBBBBBBU",
        "B1B1B1B1B1B1B1",
        "00000000000000",
        "1111BB11BB1111",
        "2U2U2U2U2U2U2U",
        "5555U2U2U25555",
    };

    private static final double BRICK_SPACING = 4;
    // BRICK_HEIGHT is already defined in GameConfig (= 20)

    private final Random random = new Random();

    public List<Brick> getBricks() { return bricks; }
    public List<Powerup> getPowerups() { return powerups; }

    /**
     * Clears all bricks and powerups from the game
     */
    private void clearLevel(Pane root) {
        root.getChildren().removeAll(bricks.stream().map(Brick::getNode).toList());
        root.getChildren().removeAll(powerups.stream().map(Powerup::getNode).toList());
        bricks.clear();
        powerups.clear();
    }

    /**
     * Calculates the width of a brick based on game dimensions
     */
    private double calculateBrickWidth() {
        return (GAME_WIDTH - BRICK_COLS * BRICK_SPACING) / BRICK_COLS;
    }

    /**
     * Creates a brick based on the type character at the specified position
     */
    private Brick createBrick(char typeChar, double x, double y, double brickWidth, Pane root) {
        Brick newBrick = null;

        switch (typeChar) {
            case '1':
                newBrick = new StandardBrick(x, y, brickWidth, BRICK_HEIGHT);
                break;
            case '2':
                newBrick = new MultiHitBrick(x, y, brickWidth, BRICK_HEIGHT);
                break;
            case 'U':
                newBrick = new IndestructibleBrick(x, y, brickWidth, BRICK_HEIGHT);
                break;
            case '5':
                newBrick = new MultiHitBrick(x, y, brickWidth, BRICK_HEIGHT, 5);
                break;
            case 'B':
                ExplodingBrick explodingBrick = new ExplodingBrick(x, y, brickWidth, BRICK_HEIGHT);
                explodingBrick.setLevelManager(this);
                explodingBrick.setRoot(root);
                newBrick = explodingBrick;
                break;
            case '0':
            default:
                break;
        }

        return newBrick;
    }

    /**
     * Adds a brick to the level and displays it
     */
    private void addBrick(Brick brick, Pane root) {
        if (brick != null) {
            bricks.add(brick);
            root.getChildren().add(brick.getNode());
        }
    }

    public void loadLevel(int level, Pane root) {
        clearLevel(root);
        currentLevel = level;
        String fileName = "/levels/level" + level + ".txt";

        try (InputStream is = getClass().getResourceAsStream(fileName)) {
            if (is == null) {
                System.out.println("Level file not found: " + fileName);
                return;
            }

            BufferedReader reader = new BufferedReader(new InputStreamReader(is));
            List<String> lines = reader.lines().toList();
            loadLevelFromPattern(lines, root, 50);

        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("Error loading level " + level);
        }
    }

    public void removeBrick(Brick brick, Pane root) {
        root.getChildren().remove(brick.getNode());
        bricks.remove(brick);

        if (random.nextDouble() < 1) {
            PowerUpType type = PowerUpType.values()[random.nextInt(PowerUpType.values().length)];
            Powerup p = new Powerup(brick.getX(), brick.getBottomY(), type);
            powerups.add(p);
            root.getChildren().add(p.getNode());
            SoundManager.getInstance().playSound(SoundManager.SoundType.POWERUP_SPAWN);
        }
    }

    public boolean isLevelComplete() {
        return bricks.isEmpty() || bricks.stream().allMatch(brick -> brick.getHitCount() < 0);
    }

    public void removePowerup(Powerup p, Pane root) {
        root.getChildren().remove(p.getNode());
        powerups.remove(p);
    }

    public void clearAllPowerups(Pane root) {
        root.getChildren().removeAll(powerups.stream().map(Powerup::getNode).toList());
        powerups.clear();
    }

    public void clearAllBricks(Pane root) {
        root.getChildren().removeAll(bricks.stream().map(Brick::getNode).toList());
        bricks.clear();
    }

    // ===== 1v1 Level Generation Methods =====

    public void loadOneVOneLevel(Pane root) {
        clearLevel(root);

        List<String> levelPattern = new ArrayList<>();
        for (int i = 0; i < BRICK_ROWS; i++) {
            if (i == 3 || i == 6 || i == 9) {
                levelPattern.add(getRandomIndestructibleBrickRow());
            } else {
                levelPattern.add("00000000000000");
            }
        }

        loadLevelFromPattern(levelPattern, root, 150);
    }

    private String getRandomIndestructibleBrickRow() {
        int numBricks = 3 + random.nextInt(3);
        char[] row = new char[BRICK_COLS];
        java.util.Arrays.fill(row, '0');

        java.util.Set<Integer> usedPositions = new java.util.HashSet<>();
        for (int i = 0; i < numBricks; i++) {
            int position;
            do {
                position = random.nextInt(BRICK_COLS);
            } while (usedPositions.contains(position));

            usedPositions.add(position);
            row[position] = 'U';
        }

        return new String(row);
    }

    /**
     * Loads a level from a pattern of strings, where each string represents a row of bricks
     */
    private void loadLevelFromPattern(List<String> lines, Pane root, int yOffset) {
        double brickWidth = calculateBrickWidth();

        for (int r = 0; r < BRICK_ROWS && r < lines.size(); r++) {
            String line = lines.get(r);
            for (int c = 0; c < BRICK_COLS && c < line.length(); c++) {
                char typeChar = line.charAt(c);
                double x = c * (brickWidth + BRICK_SPACING) + 1;
                double y = r * (BRICK_HEIGHT + BRICK_SPACING) + yOffset;

                Brick newBrick = createBrick(typeChar, x, y, brickWidth, root);
                addBrick(newBrick, root);
            }
        }
    }

    public void generateEndlessLevel(Pane root) {
        clearLevel(root);

        List<String> levelPattern = new ArrayList<>();
        for (int i = 0; i < 8; i++) {
            int patternIndex;
            do {
                patternIndex = random.nextInt(PATTERN_ROWS.length);
            } while (patternIndex == 13); // Skip empty row pattern

            levelPattern.add(PATTERN_ROWS[patternIndex]);
        }

        loadLevelFromPattern(levelPattern, root, 50);
    }
}
