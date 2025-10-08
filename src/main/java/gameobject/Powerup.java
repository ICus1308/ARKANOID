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
            case EXTEND -> Color.GOLD;
            case FREEZE -> Color.AQUA;
            case LASER -> Color.RED;
            default -> Color.PURPLE;
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
        paddle.applyPowerup(powerupType);
        return true;
    }
}
