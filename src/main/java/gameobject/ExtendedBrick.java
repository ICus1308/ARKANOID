package gameobject;

public class ExtendedBrick extends Brick {
    public ExtendedBrick(double x, double y, double width, double height) {
        super(x, y, width, height, 999, "#95a5a6");
    }

    @Override
    public int hit() {
        if (hitCount < 999) {
            hitCount--;
            return 30;
        }
        return 0;
    }
}

