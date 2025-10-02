package entities;

import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;

public class Paddle extends Rectangle {
    public Paddle(double x, double y, double w, double h) {
        super(w, h);
        setTranslateX(x);
        setTranslateY(y);
        setArcWidth(10);
        setArcHeight(10);
        setFill(Color.DARKSLATEBLUE);
    }
}
