package gameobject.powerup;

import gamemanager.manager.GameObject;
import gamemanager.core.GameEngine;
import gamemanager.manager.SoundManager;
import gameobject.paddle.Paddle;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import gameconfig.GameConfig;

import java.util.Objects;

import static gameconfig.GameConfig.*;

public class Powerup extends GameObject {
    private final GameConfig.PowerUpType powerupType;
    private final ImageView imageView;

    public Powerup(double x, double y, GameConfig.PowerUpType type) {
        super(x, y, POWERUP_WIDTH, POWERUP_HEIGHT);
        this.powerupType = type;

        // Load image based on powerup type
        String imagePath = switch (type) {
            case MULTIPLY -> "/iamgepowerup/doubleup.png";
            case ONESHOT -> "/iamgepowerup/onehit.png";
            case EXPAND -> "/iamgepowerup/shield.png";
        };

        try {
            Image image = new Image(Objects.requireNonNull(
                getClass().getResourceAsStream(imagePath)));
            this.imageView = new ImageView(image);
            this.imageView.setFitWidth(width);
            this.imageView.setFitHeight(height);
            this.imageView.setPreserveRatio(true);

            System.out.println("Powerup image loaded: " + imagePath);
        } catch (Exception e) {
            System.err.println("Error loading powerup image: " + imagePath);
            e.printStackTrace();
            throw new RuntimeException("Failed to load powerup image: " + imagePath);
        }

        setX(x);
        setY(y);

    }

    @Override
    public javafx.scene.Node getNode() { return imageView; }

    public void update() {
        setY(y + POWERUP_FALL_SPEED);
    }

    public void activate(Object game, Paddle paddle) {
        SoundManager.getInstance().playSound(SoundManager.SoundType.POWERUP_COLLECT);

        switch (powerupType) {
            case EXPAND -> paddle.applyPowerup(GameConfig.PowerUpType.EXPAND);
            case MULTIPLY -> {
                if (game instanceof GameEngine engine) {
                    engine.spawnExtraBall();
                }
            }
            case ONESHOT -> {
                if (game instanceof GameEngine engine) {
                    engine.enableOneshot();
                }
            }
        }
    }
}
