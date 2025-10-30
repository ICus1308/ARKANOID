package gameobject;

import gamemanager.GameObject;
import javafx.scene.paint.Color;
import javafx.scene.shape.Polygon;

public class Indicator extends GameObject {
    private final Polygon triangle;
    private static final double TRIANGLE_SIZE = 20.0;
    private static final double OFFSET_DISTANCE = 50.0;

    private double rotationAngle = -Math.PI / 2;
    private double ballCenterX;
    private double ballCenterY;

    public Indicator(double x, double y) {
        super(x, y, TRIANGLE_SIZE * Math.sqrt(2), TRIANGLE_SIZE);
        this.triangle = new Polygon();
        this.triangle.setFill(Color.YELLOW);
        this.triangle.setStroke(Color.ORANGE);
        this.triangle.setStrokeWidth(2);
        updatePosition(x, y);
    }

    @Override
    public javafx.scene.Node getNode() {
        return triangle;
    }

    public void updatePosition(double targetX, double targetY) {
        this.ballCenterX = targetX;
        this.ballCenterY = targetY;

        if (rotationAngle < -Math.PI) {
            rotationAngle = -Math.PI;
        } else if (rotationAngle > 0) {
            rotationAngle = 0;
        }

        double dirX = Math.cos(rotationAngle);
        double dirY = Math.sin(rotationAngle);

        double hypotenuseX = ballCenterX + dirX * OFFSET_DISTANCE;
        double hypotenuseY = ballCenterY + dirY * OFFSET_DISTANCE;

        double perpX = -dirY;
        double perpY = dirX;

        double tipX = hypotenuseX + dirX * TRIANGLE_SIZE;
        double tipY = hypotenuseY + dirY * TRIANGLE_SIZE;

        double leftX = hypotenuseX + perpX * TRIANGLE_SIZE;
        double leftY = hypotenuseY + perpY * TRIANGLE_SIZE;
        double rightX = hypotenuseX - perpX * TRIANGLE_SIZE;
        double rightY = hypotenuseY - perpY * TRIANGLE_SIZE;

        triangle.getPoints().clear();
        triangle.getPoints().addAll(
            tipX, tipY,
            leftX, leftY,
            rightX, rightY
        );

        this.x = Math.min(tipX, Math.min(leftX, rightX));
        this.y = Math.min(tipY, Math.min(leftY, rightY));
    }

    public void pointAtBall(Ball ball) {
        updatePosition(ball.getX() + ball.getRadius(), ball.getY());
    }

    public void pointAtPaddle(Paddle paddle) {
        updatePosition(paddle.getX() + paddle.getWidth() / 2, paddle.getY());
    }

    public double[] getLaunchDirection() {
        double dirX = Math.cos(rotationAngle);
        double dirY = Math.sin(rotationAngle);
        return new double[]{dirX, dirY};
    }

    public void rotateLeft(double amount) {
        rotationAngle -= amount;
        updatePosition(ballCenterX, ballCenterY);
    }

    public void rotateRight(double amount) {
        rotationAngle += amount;
        updatePosition(ballCenterX, ballCenterY);
    }

    public double getRotationAngle() {
        return rotationAngle;
    }

    public void setColor(Color fillColor, Color strokeColor) {
        triangle.setFill(fillColor);
        triangle.setStroke(strokeColor);
    }

    public void setVisible(boolean visible) {
        triangle.setVisible(visible);
    }
}

