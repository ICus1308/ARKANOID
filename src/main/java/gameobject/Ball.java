package gameobject;

import gameconfig.GameConfig.WallSideType;
import gamemanager.GameObject;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.image.Image;
import javafx.scene.paint.ImagePattern;

import java.io.InputStream;
import java.util.Random;

import static gameconfig.GameConfig.*;

public class Ball extends GameObject {
    private final Circle node;
    private double vx, vy;
    public double speed;
    private final double radius;
    private boolean stuck = true;

    // Skin/resource tracking
    private String currentSkinResource = BALL_DEFAULT_SKIN; // default skin
    private String previousSkinResource = null;

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

    public void update(double tpf, Paddle paddle) {
        update(tpf, paddle, false);
    }

    public void update(double tpf, Paddle paddle, boolean isTopPaddle) {
        if (stuck) {
            setX(paddle.getX() + paddle.getWidth() / 2 - radius);
            setY(isTopPaddle ? paddle.getY() + paddle.getHeight() : paddle.getY() - height);
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

    // Skin management API
    public String getCurrentSkinResource() {
        return currentSkinResource;
    }

    // Apply a skin by resource path (e.g. "/imageball/oneshot.png"). If null or load fails, fall back to default skin.
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
            // fallback to default skin
            applyDefaultSkin();
        }
    }

    // Apply the project's default skin
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

    // Convenience: apply oneshot skin
    public void applyOneshotSkin() {
        applySkin(BALL_ONESHOT_SKIN);
    }

    // Store the current skin so it can be restored later (used when applying temporary skins like oneshot)
    public void storeSkin() {
        this.previousSkinResource = this.currentSkinResource;
    }

    // Restore previously stored skin (or apply default if none stored)
    public void restoreSkin() {
        if (this.previousSkinResource != null) {
            applySkin(this.previousSkinResource);
            this.previousSkinResource = null;
        } else {
            applyDefaultSkin();
        }
    }
}
