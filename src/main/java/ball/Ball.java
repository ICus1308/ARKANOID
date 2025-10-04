package ball;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import config.GameConfig;

public class Ball {
    private double x;
    private double y;
    private double radius;
    private Color color;
    private Velocity velocity;

    public Ball(double x, double y, double radius) {
        this.x = x;
        this.y = y;
        this.radius = radius;
        this.color = color.RED;
        this.velocity = Velocity.fromAngleAndSpeed(45, GameConfig.BALL_SPEED);
    }

    public void setVelocity(Velocity v) {
        this.velocity = v;
    }

    public void move(double dt, double width, double height) {
        x += velocity.getDx() * dt;
        y += velocity.getDy() * dt;

        // bounce when hit border(right and left)
        if (x - radius < 0 || x + radius > width) {
            velocity.setDx(-velocity.getDx());
        }
        if (y - radius < 0 || y + radius > height) {
            velocity.setDy(-velocity.getDy());
        }
    }

    public void draw(GraphicsContext gc) {
        gc.setFill(color);
        gc.fillOval(x - radius, y - radius, radius * 2, radius * 2);
    }

    public double getX() {
        return x;
    }

    public double getY() {
        return y;
    }

    public double getRadius() {
        return radius;
    }
}

