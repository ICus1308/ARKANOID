package entities;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

public class Paddle extends GameObject {

    public Paddle(double x, double y, double width, double height) {
        super();
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.visible = true;
    }

    public void draw(GraphicsContext gc) {
        if (visible) {
            gc.setFill(Color.BLUE);
            gc.fillRect(x, y, width, height);
            gc.setStroke(Color.DARKBLUE);
            gc.setLineWidth(2);
            gc.strokeRect(x, y, width, height);
        }
    }

    public double getX() {
        return x;
    }

    public double getY() {
        return y;
    }

    public void setX(double x) {
        this.x = x;
    }

    public void setY(double y) {
        this.y = y;
    }

    public double getWidth() {
        return width;
    }

    public double getHeight() {
        return height;
    }
}
