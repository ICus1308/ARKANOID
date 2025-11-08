package gameobject.brick;

import gamemanager.manager.GameObject;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;

public abstract class Brick extends GameObject {
    protected int hitCount;
    protected String color;
    private final Rectangle node;

    public Brick(double x, double y, double width, double height, int hitCount, String color) {
        super(x, y, width, height);
        this.hitCount = hitCount;
        this.color = color;
        this.node = new Rectangle(width, height, Color.web(color));
        this.node.setStroke(Color.web("#2c3e50"));
        setX(x);
        setY(y);
    }

    public int getHitCount() { return hitCount; }

    @Override
    public javafx.scene.Node getNode() { return node; }

    public abstract int hit();

    public void updateDraw() {
        if (this instanceof MultiHitBrick multi) {
            if (multi.getHitCount() == 2) {
                node.setFill(Color.web("#e67e22"));
            } else if (multi.getHitCount() == 1) {
                node.setFill(Color.web("#f39c12"));
            }
        }
    }

    public void destroy() {
        this.hitCount = 0;
    }
}

