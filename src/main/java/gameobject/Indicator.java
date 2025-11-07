package gameobject;

import gamemanager.GameObject;
import javafx.scene.paint.Color;
import javafx.scene.shape.Polygon;

import static gameconfig.GameConfig.*;

public class Indicator extends GameObject {
    private final Polygon triangle;
    private double rotationAngle;
    private double ballCenterX;
    private double ballCenterY;
    private boolean isTopPaddle = false;

    public Indicator(double x, double y) {
        super(x, y, INDICATOR_TRIANGLE_SIZE * Math.sqrt(2), INDICATOR_TRIANGLE_SIZE);
        this.rotationAngle = -Math.PI;
        this.triangle = createTriangle();
        updatePosition(x, y);
    }

    public void setTopPaddle(boolean isTopPaddle) {
        this.isTopPaddle = isTopPaddle;
        this.rotationAngle = isTopPaddle ? Math.PI / 2 : -Math.PI / 2;
        updatePosition(ballCenterX, ballCenterY);
    }

    @Override
    public javafx.scene.Node getNode() {
        return triangle;
    }

    public void updatePosition(double targetX, double targetY) {
        this.ballCenterX = targetX;
        this.ballCenterY = targetY;

        clampRotationAngle();
        renderTriangle();
    }

    public void pointAtBall(Ball ball) {
        updatePosition(ball.getX() + ball.getRadius(), ball.getY());
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

    public void setRotation(double angle) {
        this.rotationAngle = Math.toRadians(angle);
    }

    private Polygon createTriangle() {
        Polygon tri = new Polygon();
        tri.setFill(Color.YELLOW);
        tri.setStroke(Color.ORANGE);
        tri.setStrokeWidth(2.0);
        return tri;
    }

    private void clampRotationAngle() {
        double minAngle = isTopPaddle ? INDICATOR_MIN_ANGLE_TOP : INDICATOR_MIN_ANGLE_BOTTOM;
        double maxAngle = isTopPaddle ? INDICATOR_MAX_ANGLE_TOP : INDICATOR_MAX_ANGLE_BOTTOM;

        if (rotationAngle < minAngle) {
            rotationAngle = minAngle;
        } else if (rotationAngle > maxAngle) {
            rotationAngle = maxAngle;
        }
    }

    private void renderTriangle() {
        double cosAngle = Math.cos(rotationAngle);
        double sinAngle = Math.sin(rotationAngle);

        double baseX = ballCenterX + cosAngle * INDICATOR_OFFSET_DISTANCE;
        double baseY = ballCenterY + sinAngle * INDICATOR_OFFSET_DISTANCE;

        double perpX = -sinAngle;

        double tipX = baseX + cosAngle * INDICATOR_TRIANGLE_SIZE;
        double tipY = baseY + sinAngle * INDICATOR_TRIANGLE_SIZE;

        double leftX = baseX + perpX * INDICATOR_TRIANGLE_SIZE;
        double leftY = baseY + cosAngle * INDICATOR_TRIANGLE_SIZE;

        double rightX = baseX - perpX * INDICATOR_TRIANGLE_SIZE;
        double rightY = baseY - cosAngle * INDICATOR_TRIANGLE_SIZE;

        triangle.getPoints().clear();
        triangle.getPoints().addAll(
            tipX, tipY,
            leftX, leftY,
            rightX, rightY
        );

        updateBoundingBox(tipX, tipY, leftX, leftY, rightX, rightY);
    }

    private void updateBoundingBox(double tipX, double tipY, double leftX, double leftY,
                                   double rightX, double rightY) {
        this.x = Math.min(tipX, Math.min(leftX, rightX));
        this.y = Math.min(tipY, Math.min(leftY, rightY));
    }
}
