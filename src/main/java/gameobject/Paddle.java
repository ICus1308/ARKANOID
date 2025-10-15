package gameobject;

import gamemanager.GameObject;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;

import static gameconfig.GameConfig.GAME_WIDTH;
import static gameconfig.GameConfig.PowerUpType;


public class Paddle extends GameObject {
    private final Rectangle node;
    private double speed;
    private boolean expanded = false;
    private double baseWidth;

    public Paddle(double x, double y, double width, double height, double speed) {
        super(x, y, width, height);
        this.speed = speed;
        this.baseWidth = width;
        this.node = new Rectangle(width, height, Color.LIGHTSEAGREEN);
        this.node.setArcWidth(10);
        this.node.setArcHeight(10);
        setX(x);
        setY(y);
    }

    @Override
    public javafx.scene.Node getNode() { return node; }

    public void moveLeft(double tpf) {
        double dx = speed * tpf * 60;
        if (x > 0) {
            setX(Math.max(0, x - dx));
        }
    }

    public void moveRight(double tpf) {
        double dx = speed * tpf * 60;
        if (x + width < GAME_WIDTH) {
            setX(Math.min(GAME_WIDTH - width, x + dx));
        }
    }

    public void reset() {
        setX(GAME_WIDTH / 2 - width / 2);
        if (expanded) {
            this.width = baseWidth;
            this.node.setWidth(baseWidth);
            expanded = false;
        }
    }

    public void applyPowerup(PowerUpType type) {
        if (type == PowerUpType.EXPAND) {
            if (!expanded) {
                double newWidth = this.width * 1.25;
                node.setWidth(newWidth);
                this.width = newWidth;
                expanded = true;
            }
        }
    }
}

