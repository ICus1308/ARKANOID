package gameobject.brick;

import static gameconfig.GameConfig.*;

public class MultiHitBrick extends Brick {
    public MultiHitBrick(double x, double y, double width, double height) {
        super(x, y, width, height, 3, "#c0392b");
        applySkin(BRICK_MULTIHIT_3_SKIN);
    }

    @Override
    public int hit() {
        hitCount--;
        updateDraw();
        return 20;
    }

    @Override
    public void updateDraw() {
        if (hitCount == 3) applySkin(BRICK_MULTIHIT_3_SKIN);
        else if (hitCount == 2) applySkin(BRICK_MULTIHIT_2_SKIN);
        else if (hitCount == 1) applySkin(BRICK_MULTIHIT_1_SKIN);
    }
}
