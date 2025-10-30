package gameobject;

import gamemanager.GameObject;
import javafx.scene.paint.Color;
import javafx.scene.shape.Polygon;

public class Indicator extends GameObject {
    private static final double TRIANGLE_SIZE = 20.0;
    private static final double OFFSET_DISTANCE = 50.0;

    private static final double MIN_ANGLE = -Math.PI;
    private static final double MAX_ANGLE = 0.0;

    private final Polygon triangle;
    private double rotationAngle;
    private double ballCenterX;
    private double ballCenterY;

    public Indicator(double x, double y) {
        super(x, y, TRIANGLE_SIZE * Math.sqrt(2), TRIANGLE_SIZE);
        this.rotationAngle = -Math.PI;
        this.triangle = createTriangle();
        updatePosition(x, y);
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

    private Polygon createTriangle() {
        Polygon tri = new Polygon();
        tri.setFill(Color.YELLOW);
        tri.setStroke(Color.ORANGE);
        tri.setStrokeWidth(2.0);
        return tri;
    }

    private void clampRotationAngle() {
        if (rotationAngle < MIN_ANGLE) {
            rotationAngle = MIN_ANGLE;
        } else if (rotationAngle > MAX_ANGLE) {
            rotationAngle = MAX_ANGLE;
        }
    }

    private void renderTriangle() {
        double cosAngle = Math.cos(rotationAngle);
        double sinAngle = Math.sin(rotationAngle);

        double baseX = ballCenterX + cosAngle * OFFSET_DISTANCE;
        double baseY = ballCenterY + sinAngle * OFFSET_DISTANCE;

        double perpX = -sinAngle;

        double tipX = baseX + cosAngle * TRIANGLE_SIZE;
        double tipY = baseY + sinAngle * TRIANGLE_SIZE;

        double leftX = baseX + perpX * TRIANGLE_SIZE;
        double leftY = baseY + cosAngle * TRIANGLE_SIZE;

        double rightX = baseX - perpX * TRIANGLE_SIZE;
        double rightY = baseY - cosAngle * TRIANGLE_SIZE;

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

