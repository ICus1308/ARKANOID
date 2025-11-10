package gameobject.paddle;

import gamemanager.manager.GameObject;
import javafx.scene.Node;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;

import static gameconfig.GameConfig.GAME_WIDTH;
import static gameconfig.GameConfig.PowerUpType;


public class Paddle extends GameObject {
    private final Node node; // either imageView or fallback rectangle
    private ImageView imageView;
    private Rectangle rectFallback;
    private final double speed;
    private boolean expanded = false;
    private final double baseWidth;

    public Paddle(double x, double y, double width, double height, double speed) {
        super(x, y, width, height);
        this.speed = speed;
        this.baseWidth = width;

        // Try load default image from resources
        Image img = null;
        try (java.io.InputStream is = getClass().getResourceAsStream("/imagepaddle/default.png")) {
            if (is != null) {
                img = new Image(is);
            }
        } catch (Exception ignored) {
        }

        if (img != null) {
            imageView = new ImageView(img);
            // Do not preserve ratio so image exactly fits paddle rectangle
            imageView.setPreserveRatio(false);
            imageView.setFitWidth(width);
            imageView.setFitHeight(height);
            this.node = imageView;
        } else {
            rectFallback = new Rectangle(width, height, Color.LIGHTSEAGREEN);
            rectFallback.setArcWidth(10);
            rectFallback.setArcHeight(10);
            this.node = rectFallback;
        }

        setX(x);
        setY(y);
    }

    @Override
    public Node getNode() {
        return node;
    }

    public void moveLeft(double tpf) {
        double dx = speed * tpf * 60;
        if (x > 0) {
            setX(Math.max(0, x - dx));
        }
    }

    public void moveRight(double tpf) {
        double dx = speed * tpf * 60;
        if (x + width < GAME_WIDTH) {
            setX(Math.min(GAME_WIDTH - width, x + dx));
        }
    }

    public void reset() {
        setX(GAME_WIDTH / 2 - width / 2);
        if (expanded) {
            this.width = baseWidth;
            if (imageView != null) {
                imageView.setFitWidth(baseWidth);
            } else if (rectFallback != null) {
                rectFallback.setWidth(baseWidth);
            }
            expanded = false;
        }
    }

    public void applyPowerup(PowerUpType type) {
        if (type == PowerUpType.EXPAND) {
            if (!expanded) {
                double newWidth = this.width * 1.25;
                if (imageView != null) {
                    imageView.setFitWidth(newWidth);
                } else if (rectFallback != null) {
                    rectFallback.setWidth(newWidth);
                }
                this.width = newWidth;
                expanded = true;
            }
        }
    }

    /**
     * Apply a visual skin to the paddle. Skins map to images under /imagepaddle.
     * skinId: "default", "skin1", "skin2" (others fallback to default)
     */
    public void applySkin(String skinId) {
        String res = "/imagepaddle/default.png";
        if (skinId != null) {
            switch (skinId) {
                case "skin1":
                    res = "/imagepaddle/skin1.png";
                    break;
                case "skin2":
                    res = "/imagepaddle/skin2.png";
                    break;
                case "default":
                default:
                    res = "/imagepaddle/default.png";
            }
        }

        // try load image
        try (java.io.InputStream is = getClass().getResourceAsStream(res)) {
            if (is != null) {
                Image img = new Image(is);
                if (imageView == null) {
                    // replace fallback rectangle with ImageView
                    ImageView iv = new ImageView(img);
                    iv.setPreserveRatio(false);
                    iv.setFitWidth(this.width);
                    iv.setFitHeight(this.height);
                    // transfer layout
                    if (rectFallback != null) {
                        iv.setLayoutX(rectFallback.getLayoutX());
                        iv.setLayoutY(rectFallback.getLayoutY());
                    }
                    imageView = iv;
                    // cannot reassign final node field; but node is already referencing imageView or rect
                    // If node was rectFallback, replace its parent children externally (caller manages scene graph)
                    // For safety, if rectFallback has a parent, replace it with imageView
                    if (rectFallback != null && rectFallback.getParent() != null) {
                        javafx.scene.Parent p = rectFallback.getParent();
                        if (p instanceof javafx.scene.layout.Pane) {
                            javafx.scene.layout.Pane pane = (javafx.scene.layout.Pane) p;
                            int idx = pane.getChildren().indexOf(rectFallback);
                            if (idx >= 0) pane.getChildren().set(idx, imageView);
                        }
                    }
                }

                if (imageView != null) {
                    imageView.setImage(img);
                    imageView.setFitWidth(this.width);
                    imageView.setFitHeight(this.height);
                }
                return;
            }
        } catch (Exception ignored) {
        }

        // fallback: if image failed, color the rectangle
        if (rectFallback == null) {
            rectFallback = new Rectangle(this.width, this.height, Color.LIGHTSEAGREEN);
            rectFallback.setArcWidth(10);
            rectFallback.setArcHeight(10);
        }
        // set color based on skin id for fallback
        if ("skin1".equals(skinId)) rectFallback.setFill(Color.DARKBLUE);
        else if ("skin2".equals(skinId)) rectFallback.setFill(Color.DARKRED);
        else rectFallback.setFill(Color.LIGHTSEAGREEN);
    }
}
