package ball;

public class Velocity {
    private double theDx;
    private double theDy;

    public Velocity(double dx, double dy) {
        this.theDx = dx;
        this.theDy = dy;
    }

    public double getDx() {
        return this.theDx;
    }

    public void setDx(double dx) {
        this.theDx = dx;
    }

    public double getDy() {
        return this.theDy;
    }

    public void setDy(double dy) {
        this.theDy = dy;
    }

    public static Velocity fromAngleAndSpeed(double angle, double speed) {
        double radians = Math.toRadians(angle); //converts an angle measured in degrees into radians
        double dx = speed * Math.cos(radians);
        double dy = speed * Math.sin(radians);
        return new Velocity(dx, dy);
    }
}
