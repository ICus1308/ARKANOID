package gameobject.ball;

import gameconfig.GameConfig.WallSideType;
import gamemanager.manager.GameObject;
import gameobject.paddle.Paddle;
import javafx.scene.Group;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;
import javafx.scene.paint.ImagePattern;
import javafx.scene.shape.Circle;

import java.io.InputStream;
import java.util.LinkedList;
import java.util.Random;

import static gameconfig.GameConfig.BALL_DEFAULT_SKIN;
import static gameconfig.GameConfig.BALL_ONESHOT_SKIN;

/**
 * OPTIMIZED Ball class with reduced trail effect for better performance
 */
public class Ball extends GameObject {
    private final Circle node;
    private double vx, vy;
    public double speed;
    private final double radius;
    private boolean stuck = true;

    // Skin/resource tracking
    private String currentSkinResource = BALL_DEFAULT_SKIN;
    private String previousSkinResource = null;

    // Trail effect - OPTIMIZED
    private final LinkedList<TrailPosition> trailPositions = new LinkedList<>();
    private final Group trailGroup = new Group();
    private static final int MAX_TRAIL_LENGTH = 15; // Giảm từ 50 -> 15 để tăng hiệu suất
    private boolean trailEnabled = true;
    private int frameSkipCounter = 0;
    private static final int TRAIL_FRAME_SKIP = 2; // Chỉ thêm trail mỗi 2 frame

    // Trail position class
    private static class TrailPosition {
        double x, y;
        TrailPosition(double x, double y) {
            this.x = x;
            this.y = y;
        }
    }

    public Ball(double x, double y, double radius, double speed) {
        super(x - radius, y - radius, radius * 2, radius * 2);
        this.radius = radius;
        this.speed = speed;
        this.node = new Circle(radius);
        applyDefaultSkin();
        reset(x, y);
    }

    public javafx.scene.Node getNode() {
        return node;
    }

    public Group getTrailGroup() {
        return trailGroup;
    }

    public void setTrailEnabled(boolean enabled) {
        this.trailEnabled = enabled;
        if (!enabled) {
            trailGroup.getChildren().clear();
            trailPositions.clear();
        }
    }

    public void setX(double x) {
        this.x = x;
    }

    public void setY(double y) {
        this.y = y;
    }

    public double getRadius() {
        return radius;
    }

    public double getVx() {
        return vx;
    }

    public double getVy() {
        return vy;
    }

    public void setVx(double vx) {
        this.vx = vx;
    }

    public void setVy(double vy) {
        this.vy = vy;
    }

    public void setStuck(boolean stuck) {
        this.stuck = stuck;
    }

    public Circle getShape() {
        return node;
    }

    public void launch() {
        vx = (new Random().nextBoolean() ? 1 : -1) * speed * 0.7;
        vy = -speed;
        stuck = false;
    }

    public void launch(double directionX, double directionY) {
        double magnitude = Math.sqrt(directionX * directionX + directionY * directionY);
        if (magnitude > 0) {
            directionX /= magnitude;
            directionY /= magnitude;
        }

        vx = directionX * speed;
        vy = directionY * speed;
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

    public void update(double tpf, Paddle paddle, boolean isTopPaddle) {
        if (stuck) {
            setX(paddle.getX() + paddle.getWidth() / 2 - radius);
            setY(isTopPaddle ? paddle.getY() + paddle.getHeight() : paddle.getY() - height);
            // Clear trail when stuck
            trailGroup.getChildren().clear();
            trailPositions.clear();
            frameSkipCounter = 0;
        } else {
            setX(x + vx * tpf * 60);
            setY(y + vy * tpf * 60);

            // OPTIMIZATION: Update trail with frame skipping
            if (trailEnabled) {
                frameSkipCounter++;
                if (frameSkipCounter >= TRAIL_FRAME_SKIP) {
                    updateTrail();
                    frameSkipCounter = 0;
                }
            }
        }
        node.setCenterX(x + radius);
        node.setCenterY(y + radius);
    }

    private void updateTrail() {
        // Add current position to trail
        trailPositions.addFirst(new TrailPosition(x + radius, y + radius));

        // Remove old positions
        if (trailPositions.size() > MAX_TRAIL_LENGTH) {
            trailPositions.removeLast();
        }

        // OPTIMIZATION: Render trail - skip every other trail circle for better performance
        trailGroup.getChildren().clear();
        int skip = 0;
        for (int i = 1; i < trailPositions.size(); i++) {
            skip++;
            if (skip % 2 == 0) continue; // Chỉ render 1/2 số trail circles

            Circle trailCircle = createTrailCircle(trailPositions.get(i), i);
            if (trailCircle != null) {
                trailGroup.getChildren().add(trailCircle);
            }
        }
    }

    private Circle createTrailCircle(TrailPosition pos, int index) {
        double fadeFactor = 1.0 - ((double) index / MAX_TRAIL_LENGTH);
        double trailRadius = radius * (0.5 + fadeFactor * 0.5);

        Circle circle = new Circle(pos.x, pos.y, trailRadius);

        if (node.getFill() instanceof ImagePattern) {
            circle.setFill(node.getFill());
            circle.setOpacity(fadeFactor * 0.5); // Giảm opacity để nhẹ hơn
        } else if (node.getFill() instanceof Color baseColor) {
            circle.setFill(new Color(
                    baseColor.getRed(),
                    baseColor.getGreen(),
                    baseColor.getBlue(),
                    fadeFactor * 0.5
            ));
        }

        return circle;
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

    // Skin management API
    public String getCurrentSkinResource() {
        return currentSkinResource;
    }

    public void applySkin(String resourcePath) {
        if (resourcePath == null) {
            applyDefaultSkin();
            return;
        }
        Image img = null;
        try (InputStream is = getClass().getResourceAsStream(resourcePath)) {
            if (is != null) img = new Image(is);
        } catch (Exception ignored) {
        }
        if (img != null && img.getWidth() > 0 && img.getHeight() > 0) {
            node.setFill(new ImagePattern(img, 0, 0, 1, 1, true));
            this.currentSkinResource = resourcePath;
        } else {
            applyDefaultSkin();
        }
    }

    public void applyDefaultSkin() {
        Image img = null;
        try (InputStream is = getClass().getResourceAsStream(BALL_DEFAULT_SKIN)) {
            if (is != null) img = new Image(is);
        } catch (Exception ignored) {
        }
        if (img != null && img.getWidth() > 0 && img.getHeight() > 0) {
            node.setFill(new ImagePattern(img, 0, 0, 1, 1, true));
            this.currentSkinResource = BALL_DEFAULT_SKIN;
        } else {
            node.setFill(Color.SALMON);
            this.currentSkinResource = null;
        }
    }

    public void applyOneshotSkin() {
        applySkin(BALL_ONESHOT_SKIN);
    }

    public void storeSkin() {
        this.previousSkinResource = this.currentSkinResource;
    }

    public void restoreSkin() {
        if (this.previousSkinResource != null) {
            applySkin(this.previousSkinResource);
            this.previousSkinResource = null;
        } else {
            applyDefaultSkin();
        }
    }
}
