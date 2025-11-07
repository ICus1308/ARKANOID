package gameobject;

import gamemanager.LevelManager;
import gamemanager.SoundManager;
import javafx.scene.image.Image;
import javafx.scene.layout.Pane;

import static gameconfig.GameConfig.*;

public class BExplodingBrick extends Brick {

    private LevelManager levelManager;
    private Pane root;
    private Image explosionImage;
    private boolean hasGlowEffect = true;

    public BExplodingBrick(double x, double y, double width, double height) {
        super(x, y, width, height, EXPLODING_HIT_COUNT, EXPLODING_COLOR);
    }

    public BExplodingBrick(double x, double y, double width, double height, LevelManager levelManager) {
        super(x, y, width, height, EXPLODING_HIT_COUNT, EXPLODING_COLOR);
        this.levelManager = levelManager;
    }

    /**
     * Thiết lập LevelManager để truy cập các brick khác
     */
    public void setLevelManager(LevelManager levelManager) {
        this.levelManager = levelManager;
    }

    /**
     * Thiết lập Root Pane để xóa brick khỏi UI
     */
    public void setRoot(Pane root) {
        this.root = root;
    }


    @Override
    public int hit() {
        hitCount--;
        if (hitCount <= 0) {
            explode();
            return 50;
        }
        return 0;
    }

    private void explode() {
        SoundManager.getInstance().playSound(SoundManager.SoundType.EXPLOSION);

        // QUAN TRỌNG: Tạo bản sao để tránh ConcurrentModificationException
        java.util.List<Brick> allBricks = new java.util.ArrayList<>(levelManager.getBricks());
        java.util.List<Brick> bricksToDestroy = new java.util.ArrayList<>();

        double myX = getX();
        double myY = getY();
        double myWidth = getWidth();
        double myHeight = getHeight();

        // Tìm các brick trong phạm vi nổ
        for (Brick brick : allBricks) {
            if (brick == this) continue;


            double brickX = brick.getX();
            double brickY = brick.getY();
            double brickWidth = brick.getWidth();
            double brickHeight = brick.getHeight();

            // Kiểm tra HÀNG NGANG: Y gần giống nhau (trong cùng 1 hàng)
            double deltaY = Math.abs(brickY - myY);
            if (deltaY < myHeight + 2) {
                double deltaX = Math.abs(brickX - myX);
                if (deltaX > 0 && deltaX <= (EXPLOSION_RANGE + 1) * (myWidth + 2)) {
                    bricksToDestroy.add(brick);
                    continue;
                }
            }
            double deltaX = Math.abs(brickX - myX);

            if (deltaX < myWidth + 2) {
                deltaY = Math.abs(brickY - myY);
                if (deltaY > 0 && deltaY <= (EXPLOSION_RANGE + 1) * (myHeight + 2)) {
                    if (!bricksToDestroy.contains(brick)) {
                        bricksToDestroy.add(brick);
                    }
                }
            }
        }

        for (Brick brick : bricksToDestroy) {
            brick.destroy();
            levelManager.removeBrick(brick, root);

            if (brick instanceof BExplodingBrick) {
                ((BExplodingBrick) brick).explode();
            }
        }
    }

    public static int getExplosionRange() {
        return EXPLOSION_RANGE;
    }

}
