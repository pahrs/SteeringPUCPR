package cars.engine;

public final class Obstacle {
    private final Vector2 position;
    private final double radius;

    public Obstacle(double x, double y, double radius) {
        this.position = new Vector2(x, y);
        this.radius = radius;
    }

    public Vector2 getPosition() { return position.clone(); }
    public double getRadius() { return radius; }
}
