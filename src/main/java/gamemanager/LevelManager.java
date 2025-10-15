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

public class LevelManager {
    private final List<Brick> bricks = new ArrayList<>();
    private final List<Powerup> powerups = new ArrayList<>();
    public int currentLevel = 1;
    public final int maxLevel = 9;

    public List<Brick> getBricks() { return bricks; }
    public List<Powerup> getPowerups() { return powerups; }

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


    public void removeBrick(Brick brick, Pane root) {
        root.getChildren().remove(brick.getNode());
        bricks.remove(brick);
        if (new Random().nextDouble() < 1) {
            PowerUpType type = PowerUpType.values()
                    [new Random().nextInt(PowerUpType.values().length)];
            Powerup p = new Powerup(brick.getX(), brick.getBottomY(), type);
            powerups.add(p);
            root.getChildren().add(p.getNode());
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
}
