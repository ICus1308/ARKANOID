package gameobject;

public class StandardBrick extends Brick {
    public StandardBrick(double x, double y, double width, double height) {
        super(x, y, width, height, 3, "#e74c3c");
    }

    @Override
    public int hit() {
        return (--hitCount == 0 ? 10 : 0);
    }
}
