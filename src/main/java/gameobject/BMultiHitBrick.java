package gameobject;

public class BMultiHitBrick extends Brick {
    public BMultiHitBrick(double x, double y, double width, double height) {
        super(x, y, width, height, 3, "#c0392b");
    }
    public BMultiHitBrick(double x, double y, double width, double height, int initialHitCount) {
        super(x, y, width, height, initialHitCount, "#c0392b");
    }

    @Override
    public int hit() {
        hitCount--;
        updateDraw();
        return 20;
    }
}
