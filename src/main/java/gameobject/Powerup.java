package gameobject;

import gamemanager.GameObject;
import gamemanager.GamePlay;
import gamemanager.SoundManager;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import gameconfig.GameConfig;

public class Powerup extends GameObject {
    private final GameConfig.PowerUpType powerupType;
    private final Rectangle node;

    public Powerup(double x, double y, GameConfig.PowerUpType type) {
        super(x, y, 20, 15);
        this.powerupType = type;
        this.node = new Rectangle(width, height);
        this.node.setArcWidth(5);
        this.node.setArcHeight(5);

        Color color = switch (type) {
            case EXPAND -> Color.GOLD;
            case MULTIPLY -> Color.PURPLE;
            case ONESHOT -> Color.CRIMSON;
        };
        this.node.setFill(color);
        this.node.setStroke(Color.WHITE);
        setX(x);
        setY(y);
    }

    @Override
    public javafx.scene.Node getNode() { return node; }

    public void update() {
        double fallSpeed = 1.0;
        setY(y + fallSpeed);
    }

    public void activate(Object game, Paddle paddle) {
        SoundManager.getInstance().playSound(SoundManager.SoundType.POWERUP_COLLECT);

        switch (powerupType) {
            case EXPAND -> paddle.applyPowerup(GameConfig.PowerUpType.EXPAND);
            case MULTIPLY -> {
                if (game instanceof GamePlay app) {
                    app.spawnExtraBall();
                }
            }
            case ONESHOT -> {
                if (game instanceof GamePlay app) {
                    app.enableOneshot();
                }
            }
        }
    }
}
