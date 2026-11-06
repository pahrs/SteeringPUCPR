package cars.student;

import cars.engine.Car;
import cars.engine.Vector2;
import cars.engine.World;

import java.awt.Color;

import static cars.engine.Vector2.add;
import static cars.engine.Vector2.multiply;

public class HerdCar extends Car {
    private static final double WEIGHT_WANDER = 0.6;
    private static final double WEIGHT_SEPARATION = 2.0;
    private static final double WEIGHT_FLEE = 1.2;
    private static final double WEIGHT_AVOID = 3.0;
    private static final double SEPARATION_RADIUS = 100;
    private static final double PANIC_RADIUS = 150;
    private static final double AVOID_MARGIN = 18;

    private final Behaviors.Wander wander = new Behaviors.Wander(80, 40, 6.0);

    public HerdCar(double x, double y) {
        super(settings ->
            settings
                .color(new Color(30, 160, 60))
                .position(x, y)
                .randomOrientation()
                .maxSpeed(100)
        );
    }

    @Override
    public Vector2 calculateSteering(final World world) {
        final var wandering = multiply(wander.steer(this, world.getSecs()), WEIGHT_WANDER);
        final var separation = multiply(Behaviors.separation(this, world, SEPARATION_RADIUS), WEIGHT_SEPARATION);
        final var flee = multiply(Behaviors.flee(this, world.getMousePos(), PANIC_RADIUS), WEIGHT_FLEE);
        final var avoid = multiply(Behaviors.obstacleAvoidance(this, world.getObstacles(), AVOID_MARGIN), WEIGHT_AVOID);
        return add(add(wandering, separation), add(flee, avoid));
    }
}
