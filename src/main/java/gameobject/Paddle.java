package gameobject;

import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import static arkanoid.arkanoid.gameconfig.GameConfig.*;
import gameconfig.GameConfig;


public class Paddle extends GameObject {
    private final Rectangle node;
    private double speed;
    private boolean extended = false;
    private double currentWidth;

    public Paddle(double x, double y, double width, double height, double speed) {
        super(x, y, width, height);
        this.speed = speed;
        this.currentWidth = width;
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
        if (extended) {
            this.width = 100;
            this.node.setWidth(100);
            extended = false;
        }
    }

    public void applyPowerup(PowerupType type) {
        if (type == GameConfig.PowerupType.EXTEND) {
            if (!extended) {
                currentWidth = this.width * 1.5;
                node.setWidth(currentWidth);
                this.width = currentWidth;
                extended = true;
            }
        }
    }
}
