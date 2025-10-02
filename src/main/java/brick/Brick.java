package brick;

import javafx.scene.paint.Color;

import javafx.scene.canvas.GraphicsContext;
import javafx.geometry.Rectangle2D;



public class Brick {
    private double x;
    private double y;
    private double width;
    private double height;
    private boolean visible;

    public Brick(double x, double y, double width, double height) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.visible = true;
    }

    public void draw(GraphicsContext gc) {
        if (visible) {
            gc.setFill(Color.RED);
            gc.fillRect(x, y, width, height);
        }
    }

    public Rectangle2D getBounds() {
        return new Rectangle2D(x, y, width, height);
    }

    public boolean isVisible() {
        return visible;
    }

    public void setVisible(boolean visible) {
        this.visible = visible;
    }
}
