package gameobject;

import arkanoid.arkanoid.gameconfig.GameConfig.WallSideType;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;

import java.util.Random;

public class Ball extends GameObject {
    private final Circle node;
    private double vx, vy;
    public double speed;
    private final double radius;
    private boolean stuck = true;

    public Ball(double x, double y, double radius, double speed) {
        super(x - radius, y - radius, radius * 2, radius * 2);
        this.radius = radius;
        this.speed = speed;
        this.node = new Circle(radius, Color.SALMON);
        reset(x, y);
    }

    @Override
    public javafx.scene.Node getNode() { return node; }

    @Override
    public void setX(double x) { this.x = x; }
    @Override
    public void setY(double y) { this.y = y; }

    public double getRadius() { return radius; }
    public double getVx() { return vx; }
    public double getVy() { return vy; }
    public void setVx(double vx) { this.vx = vx; }
    public void setVy(double vy) { this.vy = vy; }
    public void setStuck(boolean stuck) { this.stuck = stuck; }

    public void launch() {
        vx = (new Random().nextBoolean() ? 1 : -1) * speed * 0.7;
        vy = -speed;
        stuck = false;
    }

    public void reset(double x, double y) {
        setX(x - radius);
        setY(y - radius);
        stuck = true;
        vx = 0;
        vy = 0;
        node.setCenterX(this.x + radius);
        node.setCenterY(this.y + radius);
    }

    public void update(double tpf, arkanoid.arkanoid.gameobject.Paddle paddle, double gameWidth, double gameHeight) {
        if (stuck) {
            setX(paddle.getX() + paddle.getWidth() / 2 - radius);
            setY(paddle.getY() - height);
        } else {
            setX(x + vx * tpf * 60);
            setY(y + vy * tpf * 60);
        }
        node.setCenterX(x + radius);
        node.setCenterY(y + radius);
    }

    public void bounce(WallSideType wallSide) {
        switch (wallSide) {
            case NORTH:
            case SOUTH:
                vy *= -1;
                break;
            case EAST:
            case WEST:
                vx *= -1;
                break;
            case BOTTOM_HIT:
            case HIT_OUT_OF_BOUNDS:
                break;
        }
    }
}
