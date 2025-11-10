package gameobject.brick;

import static gameconfig.GameConfig.*;

public class StandardBrick extends Brick {
    public StandardBrick(double x, double y, double width, double height) {
        super(x, y, width, height, 1, "#e74c3c");
        applySkin(BRICK_STANDARD_SKIN);
    }

    @Override
    public int hit() {
        return (--hitCount == 0 ? 10 : 0);
    }
}
