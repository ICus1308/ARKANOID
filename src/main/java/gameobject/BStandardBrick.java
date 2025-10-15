package gameobject;

public class BStandardBrick extends Brick {
    public BStandardBrick(double x, double y, double width, double height) {
        super(x, y, width, height, 1, "#e74c3c");
    }

    @Override
    public int hit() {
        return (--hitCount == 0 ? 10 : 0);
    }
}
