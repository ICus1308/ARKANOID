package gameobject;

import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import gameconfig.GameConfig;

public class Powerup extends GameObject {
    private final GameConfig.PowerupType powerupType;
    private final Rectangle node;
    private final double fallSpeed = 3.0;

    public Powerup(double x, double y, GameConfig.PowerupType type) {
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

    public void move() {
        setY(y + fallSpeed);
    }

    public boolean activate(Object game, Paddle paddle) {
        switch (powerupType) {
            case EXPAND -> paddle.applyPowerup(GameConfig.PowerupType.EXPAND);
            case MULTIPLY -> {
                if (game instanceof gamemanager.ArkanoidApp app) {
                    app.spawnExtraBall();
                }
            }
            case ONESHOT -> {
                if (game instanceof gamemanager.ArkanoidApp app) {
                    app.enableOneshot();
                }
            }
        }
        return true;
    }
}