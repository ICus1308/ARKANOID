package gameobject.brick;

import gamemanager.core.LevelManager;
import gamemanager.manager.SoundManager;
import javafx.scene.layout.Pane;

import static gameconfig.GameConfig.*;

public class ExplodingBrick extends Brick {

    private LevelManager levelManager;
    private Pane root;
    private boolean isExploding = false;

    public ExplodingBrick(double x, double y, double width, double height) {
        super(x, y, width, height, EXPLODING_HIT_COUNT, EXPLODING_COLOR);
        applySkin(BRICK_EXPLODING_SKIN1);
    }

    public void setLevelManager(LevelManager levelManager) {
        this.levelManager = levelManager;
    }

    public void setRoot(Pane root) {
        this.root = root;
    }

    @Override
    public int hit() {
        hitCount--;
        if (hitCount <= 0 && !isExploding) {
            explode();
            return 50;
        }
        return 0;
    }

    private void explode() {
        // Prevent multiple explosions
        if (isExploding) return;
        isExploding = true;

        // ========== FIX: Remove self first to prevent being hit again
        if (levelManager != null && root != null) {
            if (getNode() != null && getNode().getParent() != null) {
                root.getChildren().remove(getNode());
            }
            levelManager.getBricks().remove(this);
        }

        SoundManager.getInstance().playSound(SoundManager.SoundType.EXPLOSION);

        // Use HashSet for O(1) lookup
        java.util.Set<Brick> bricksToDestroy = new java.util.HashSet<>();

        double myX = getX();
        double myY = getY();
        double myWidth = getWidth();
        double myHeight = getHeight();

        // Calculate bounds once
        double minX = myX - (EXPLOSION_RANGE + 1) * (myWidth + 1);
        double maxX = myX + myWidth + (EXPLOSION_RANGE + 1) * (myWidth + 1);
        double minY = myY - (EXPLOSION_RANGE + 1) * (myHeight + 1);
        double maxY = myY + myHeight + (EXPLOSION_RANGE + 1) * (myHeight + 1);

        // Single pass, no redundant checks
        for (Brick brick : levelManager.getBricks()) {
            if (brick == this || brick instanceof IndestructibleBrick) continue;

            double brickX = brick.getX();
            double brickY = brick.getY();

            // Quick bounds check first
            if (brickX < minX || brickX > maxX || brickY < minY || brickY > maxY) {
                continue;
            }

            // Check horizontal proximity (same row)
            double deltaY = Math.abs(brickY - myY);
            if (deltaY < myHeight + 1) {
                double deltaX = Math.abs(brickX - myX);
                if (deltaX > 0 && deltaX <= (EXPLOSION_RANGE + 1) * (myWidth + 4)) {
                    bricksToDestroy.add(brick);
                    continue;
                }
            }

            // Check vertical proximity (same column)
            double deltaX = Math.abs(brickX - myX);
            if (deltaX < myWidth + 1) {
                deltaY = Math.abs(brickY - myY);
                if (deltaY > 0 && deltaY <= (EXPLOSION_RANGE + 1) * (myHeight + 4)) {
                    bricksToDestroy.add(brick);
                }
            }
        }

        // Batch removal
        java.util.List<javafx.scene.Node> nodesToRemove = new java.util.ArrayList<>();
        java.util.List<ExplodingBrick> explodingBricks = new java.util.ArrayList<>();

        for (Brick brick : bricksToDestroy) {
            brick.destroy();

            // Collect node for batch removal
            if (brick.getNode() != null && brick.getNode().getParent() != null) {
                nodesToRemove.add(brick.getNode());
            }

            // Collect exploding bricks for chain reaction
            if (brick instanceof ExplodingBrick) {
                explodingBricks.add((ExplodingBrick) brick);
            }

            levelManager.getBricks().remove(brick);
        }

        // Batch remove - much faster!
        if (!nodesToRemove.isEmpty()) {
            root.getChildren().removeAll(nodesToRemove);
        }

        // Delayed chain reaction - prevent stack overflow
        if (!explodingBricks.isEmpty()) {
            javafx.animation.PauseTransition delay = new javafx.animation.PauseTransition(
                    javafx.util.Duration.millis(50)
            );
            delay.setOnFinished(e -> {
                for (ExplodingBrick eb : explodingBricks) {
                    if (!eb.isExploding) {
                        eb.explode();
                    }
                }
            });
            delay.play();
        }
    }
}
