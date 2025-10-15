package gamemanager;

public abstract class GameObject {
    protected double x, y, width, height;

    public GameObject(double x, double y, double width, double height) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
    }

    public abstract javafx.scene.Node getNode();

    public double getX() { return x; }
    public double getY() { return y; }
    public double getWidth() { return width; }
    public double getHeight() { return height; }
    public double getRightX() { return x + width; }
    public double getBottomY() { return y + height; }

    public void setX(double x) { this.x = x; getNode().setLayoutX(x); }
    public void setY(double y) { this.y = y; getNode().setLayoutY(y); }
}