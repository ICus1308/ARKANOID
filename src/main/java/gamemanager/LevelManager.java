package gamemanager;

import gameobject.*;
import javafx.scene.layout.Pane;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import static gameconfig.GameConfig.*;

public class LevelManager extends GamePlay {
    private final List<Brick> bricks = new ArrayList<>();
    private final List<Powerup> powerups = new ArrayList<>();
    public int currentLevel = 1;
    public final int maxLevel = 9;

    private static final String[] PATTERN_ROWS = {
        "11111111111111",
        "22222222222222",
        "UUUUUUUUUUUUUU",
        "U1U1U1U1U1U1U1",
        "U2U2U2U2U2U2U2",
        "555555555555555",
        "1212121212121212",
        "UUU1111111UUU",
        "00011111110000",
        "U555555555555U",
        "UU5222222225UU",
        "UUUU521125UUUU",
        "UBBBBBBBBBBBU",
        "B1B1B1B1B1B1B",
        "00000000000000",
        "1111BB11BB1111",
        "2U2U2U2U2U2U2",
        "5555U2U2U5555",
    };

    private final Random random = new Random();

    public List<Brick> getBricks() { return bricks; }
    public List<Powerup> getPowerups() { return powerups; }

    public List<String> generateRandomLevel(double density) {
        List<String> level = new ArrayList<>();

        int emptyTopRows = 6;
        int emptyBottomRows = 2;

        for (int i = 0; i < BRICK_ROWS; i++) {
            if (i < emptyTopRows || i >= (BRICK_ROWS - emptyBottomRows)) {
                level.add(PATTERN_ROWS[14]);
            } else {
                if (random.nextDouble() < density) {
                    int patternIndex = random.nextInt(PATTERN_ROWS.length - 1);
                    if (patternIndex >= 14) patternIndex++;
                    level.add(PATTERN_ROWS[patternIndex]);
                } else {
                    level.add(PATTERN_ROWS[14]);
                }
            }
        }

        return level;
    }

    public void loadLevel(int level, Pane root) {
        root.getChildren().removeAll(bricks.stream().map(Brick::getNode).toList());
        root.getChildren().removeAll(powerups.stream().map(Powerup::getNode).toList());
        bricks.clear();
        powerups.clear();

        currentLevel = level;
        String fileName = "/levels/level" + level + ".txt";
        try(InputStream is = getClass().getResourceAsStream(fileName)){
            if(is == null){
                System.out.println("Level file not found: " + fileName);
                return;
            }
            BufferedReader reader = new BufferedReader(new InputStreamReader(is));
            java.util.List<String> lines = reader.lines().toList();

            double brickWidth = (GAME_WIDTH - BRICK_COLS * 2) / BRICK_COLS;
            double brickHeight = 20;

            for (int r = 0; r < BRICK_ROWS && r < lines.size(); r++) {
                String line = lines.get(r);
                for (int c = 0; c < BRICK_COLS && c < line.length(); c++) {
                    char typeChar = line.charAt(c);
                    double x = c * (brickWidth + 2) + 1;
                    double y = r * (brickHeight + 2) + 50;

                    Brick newBrick = null;

                    switch (typeChar) {
                        case '1':
                            newBrick = new BStandardBrick(x, y, brickWidth, brickHeight);
                            break;
                        case '2':
                            newBrick = new BMultiHitBrick(x, y, brickWidth, brickHeight);
                            break;
                        case 'U':
                            newBrick = new BIndestructibleBrick(x, y, brickWidth, brickHeight);
                            break;
                        case '5':
                            newBrick = new BMultiHitBrick(x, y, brickWidth, brickHeight,5);
                            break;
                        case 'B':
                            BExplodingBrick explodingBrick = new BExplodingBrick(x, y, brickWidth, brickHeight);
                            explodingBrick.setLevelManager(this);
                            explodingBrick.setRoot(root);
                            newBrick = explodingBrick;
                            break;
                        case '0':
                        default:
                            break;
                    }

                    if (newBrick != null) {
                        bricks.add(newBrick);
                        root.getChildren().add(newBrick.getNode());
                    }
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("Lỗi khi đọc file level " + level);
        }
    }

    public void loadRandomLevel(double density, Pane root) {
        root.getChildren().removeAll(bricks.stream().map(Brick::getNode).toList());
        root.getChildren().removeAll(powerups.stream().map(Powerup::getNode).toList());
        bricks.clear();
        powerups.clear();

        List<String> level = generateRandomLevel(density);
        loadLevelFromPattern(level, root, 50); // Use default Y-offset of 50
    }

    public void removeBrick(Brick brick, Pane root) {
        root.getChildren().remove(brick.getNode());
        bricks.remove(brick);
        if (new Random().nextDouble() < 0.1) {
            PowerUpType type = PowerUpType.values()
                    [new Random().nextInt(PowerUpType.values().length)];
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

    /**
     * Loads a special level for 1v1 mode with 3 rows of indestructible bricks
     * positioned in the middle of the screen with spacing between rows.
     */
    public void loadOneVOneLevel(Pane root) {
        // Clear existing bricks
        root.getChildren().removeAll(bricks.stream().map(Brick::getNode).toList());
        root.getChildren().removeAll(powerups.stream().map(Powerup::getNode).toList());
        bricks.clear();
        powerups.clear();

        // Create the 1v1 level pattern: Brick -> 2 Empty -> Brick -> 2 Empty -> Brick
        List<String> levelPattern = new ArrayList<>();

        // Pattern: 7 rows total positioned in the middle area
        // Start from row 3 (leaving space for top paddle and some clearance)
        for (int i = 0; i < BRICK_ROWS; i++) {
            if (i == 3) {
                // First brick row - use indestructible bricks with 3-5 bricks
                levelPattern.add(getRandomIndestructibleBrickRow());
            } else if (i == 4 || i == 5) {
                // Two empty rows
                levelPattern.add("00000000000000");
            } else if (i == 6) {
                // Second brick row - use indestructible bricks with 3-5 bricks
                levelPattern.add(getRandomIndestructibleBrickRow());
            } else if (i == 7 || i == 8) {
                // Two empty rows
                levelPattern.add("00000000000000");
            } else if (i == 9) {
                // Third brick row - use indestructible bricks with 3-5 bricks
                levelPattern.add(getRandomIndestructibleBrickRow());
            } else {
                // Empty rows for top and bottom clearance
                levelPattern.add("00000000000000");
            }
        }

        // Load the pattern with Y-offset of 150 (moved down by 100px)
        loadLevelFromPattern(levelPattern, root, 150);
    }

    /**
     * Generates a random row with 3-5 indestructible bricks placed at random positions.
     * Used for 1v1 mode to create unpredictable obstacle patterns.
     *
     * @return A string pattern with 'U' (indestructible) bricks and '0' (empty) spaces
     */
    private String getRandomIndestructibleBrickRow() {
        // Generate a row with 3-5 indestructible bricks ('U') randomly placed
        int numBricks = 3 + random.nextInt(3); // 3 to 5 bricks

        // Create array to hold the row (14 columns for BRICK_COLS)
        char[] row = new char[14];

        // Fill with empty spaces first
        for (int i = 0; i < 14; i++) {
            row[i] = '0';
        }

        // Randomly place the bricks
        java.util.Set<Integer> usedPositions = new java.util.HashSet<>();
        for (int i = 0; i < numBricks; i++) {
            int position;
            do {
                position = random.nextInt(14);
            } while (usedPositions.contains(position));

            usedPositions.add(position);
            row[position] = 'U'; // U = Indestructible brick
        }

        return new String(row);
    }

    /**
     * Loads a level from a pattern list with a custom Y-offset.
     * This overloaded version allows positioning bricks at different vertical positions.
     *
     * @param lines The pattern lines defining brick layout
     * @param root The pane to add bricks to
     * @param yOffset The Y-offset for brick positioning (e.g., 50 for normal, 150 for 1v1)
     */
    private void loadLevelFromPattern(List<String> lines, Pane root, int yOffset) {
        double brickWidth = (GAME_WIDTH - BRICK_COLS * 2) / BRICK_COLS;
        double brickHeight = 20;

        for (int r = 0; r < BRICK_ROWS && r < lines.size(); r++) {
            String line = lines.get(r);
            for (int c = 0; c < BRICK_COLS && c < line.length(); c++) {
                char typeChar = line.charAt(c);
                double x = c * (brickWidth + 2) + 1;
                double y = r * (brickHeight + 2) + yOffset;

                Brick newBrick = null;

                switch (typeChar) {
                    case '1':
                        newBrick = new BStandardBrick(x, y, brickWidth, brickHeight);
                        break;
                    case '2':
                        newBrick = new BMultiHitBrick(x, y, brickWidth, brickHeight);
                        break;
                    case 'U':
                        newBrick = new BIndestructibleBrick(x, y, brickWidth, brickHeight);
                        break;
                    case '5':
                        newBrick = new BMultiHitBrick(x, y, brickWidth, brickHeight, 5);
                        break;
                    case 'B':
                        BExplodingBrick explodingBrick = new BExplodingBrick(x, y, brickWidth, brickHeight);
                        explodingBrick.setLevelManager(this);
                        explodingBrick.setRoot(root);
                        newBrick = explodingBrick;
                        break;
                    case '0':
                    default:
                        break;
                }

                if (newBrick != null) {
                    bricks.add(newBrick);
                    root.getChildren().add(newBrick.getNode());
                }
            }
        }
    }
}
