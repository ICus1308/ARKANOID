package gamemanager;

import gameconfig.GameConfig;
import gameobject.Brick;
import gameobject.MultiHitBrick;
import gameobject.Powerup;
import gameobject.StandardBrick;
import javafx.scene.layout.Pane;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import static gameconfig.GameConfig.*;

public class LevelManager {
    private final List<Brick> bricks = new ArrayList<>();
    private final List<Powerup> powerups = new ArrayList<>();
    public int currentLevel = 1;
    public final int maxLevel = 3;

    public List<Brick> getBricks() { return bricks; }
    public List<Powerup> getPowerups() { return powerups; }

    public void loadLevel(int level, Pane root) {
        root.getChildren().removeAll(bricks.stream().map(Brick::getNode).toList());
        root.getChildren().removeAll(powerups.stream().map(Powerup::getNode).toList());
        bricks.clear();
        powerups.clear();

        currentLevel = level;
        double brickWidth = (GAME_WIDTH - BRICK_COLS * 2) / BRICK_COLS;
        double brickHeight = 20;
        Random rand = new Random();

        for (int r = 0; r < BRICK_ROWS; r++) {
            for (int c = 0; c < BRICK_COLS; c++) {
                double x = c * (brickWidth + 2) + 1;
                double y = r * (brickHeight + 2) + 50;

                Brick newBrick;
                int type = rand.nextInt(10);

                if (level == 1) {
                    newBrick = new StandardBrick(x, y, brickWidth, brickHeight);
                } else if (level == 2) {
                    if (type < 7) newBrick = new StandardBrick(x, y, brickWidth, brickHeight);
                    else newBrick = new MultiHitBrick(x, y, brickWidth, brickHeight);
                } else {
                    if (type < 4) newBrick = new StandardBrick(x, y, brickWidth, brickHeight);
                    else if (type < 7) newBrick = new MultiHitBrick(x, y, brickWidth, brickHeight);
                    else newBrick = new ExtendedBrick(x, y, brickWidth, brickHeight);
                }

                bricks.add(newBrick);
                root.getChildren().add(newBrick.getNode());
            }
        }
    }

    public void removeBrick(Brick brick, Pane root) {
        root.getChildren().remove(brick.getNode());
        bricks.remove(brick);
        if (new Random().nextDouble() < 0.15) {
            GameConfig.PowerupType type = GameConfig.PowerupType.values()[new Random().nextInt(GameConfig.PowerupType.values().length)];
            Powerup p = new Powerup(brick.getX(), brick.getBottomY(), type);
            powerups.add(p);
            root.getChildren().add(p.getNode());
        }
    }

    public void removePowerup(Powerup p, Pane root) {
        root.getChildren().remove(p.getNode());
        powerups.remove(p);
    }

    public void clearAllPowerups(Pane root) {
        root.getChildren().removeAll(powerups.stream().map(Powerup::getNode).toList());
        powerups.clear();
    }
}
