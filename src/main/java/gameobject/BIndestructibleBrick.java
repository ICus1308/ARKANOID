package gameobject;

import static gameconfig.GameConfig.*;

public class BIndestructibleBrick extends Brick {
    public BIndestructibleBrick(double x, double y, double width, double height) {
        super(x, y, width, height, INDESTRUCTIBLE_HIT_COUNT, INDESTRUCTIBLE_COLOR);
    }

    @Override
    public int hit(){
        return 0;
    }

    @Override
    public void updateDraw(){

    }
}
