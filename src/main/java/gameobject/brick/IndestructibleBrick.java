package gameobject.brick;

import static gameconfig.GameConfig.*;

public class IndestructibleBrick extends Brick {
    public IndestructibleBrick(double x, double y, double width, double height) {
        super(x, y, width, height, INDESTRUCTIBLE_HIT_COUNT, INDESTRUCTIBLE_COLOR);
        applySkin(BRICK_INDESTRUCTIBLE_SKIN);
    }

    @Override
    public int hit(){
        return 0;
    }

    @Override
    public void updateDraw(){

    }
}
