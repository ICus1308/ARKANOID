package gameobject;

public class IndestructibleBrick extends Brick {
    private static  final int INDESTRUCTIBLE_HIT_COUNT = -1;
    private  static final String INDESTRUCTIBLE_COLOR = "#7f8c8d";

    public IndestructibleBrick(double x, double y, double width, double height) {
        super(x, y, width, height, INDESTRUCTIBLE_HIT_COUNT, INDESTRUCTIBLE_COLOR);
    }
    @Override
    public  int hit(){
        return 0;
    }
    @Override
    public void updateDraw(){

    }
}
