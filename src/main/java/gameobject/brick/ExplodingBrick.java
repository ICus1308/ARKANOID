package gameobject.brick;

import gamemanager.core.LevelManager;
import gamemanager.manager.SoundManager;
import javafx.scene.layout.Pane;

import static gameconfig.GameConfig.*;

public class ExplodingBrick extends Brick {

    private LevelManager levelManager;
    private Pane root;

    public ExplodingBrick(double x, double y, double width, double height) {
        super(x, y, width, height, EXPLODING_HIT_COUNT, EXPLODING_COLOR);
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

            // Kiểm tra HÀNG NGANG: Y gần giống nhau (trong cùng 1 hàng)
            double deltaY = Math.abs(brickY - myY);
            if (deltaY < myHeight + 1) {
                double deltaX = Math.abs(brickX - myX);
                if (deltaX > 0 && deltaX <= (EXPLOSION_RANGE + 1) * (myWidth + 1)) {
                    bricksToDestroy.add(brick);
                    continue;
                }
            }
            double deltaX = Math.abs(brickX - myX);

            if (deltaX < myWidth + 1) {
                deltaY = Math.abs(brickY - myY);
                if (deltaY > 0 && deltaY <= (EXPLOSION_RANGE + 1) * (myHeight + 1)) {
                    if (!bricksToDestroy.contains(brick)) {
                        bricksToDestroy.add(brick);
                    }
                }
            }
        }

        for (Brick brick : bricksToDestroy) {
            brick.destroy();
            levelManager.removeBrick(brick, root);

            if (brick instanceof ExplodingBrick) {
                ((ExplodingBrick) brick).explode();
            }
        }
    }
}
