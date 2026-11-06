package cars.student;

import cars.engine.Car;
import cars.engine.Vector2;
import cars.engine.World;

import java.awt.Color;

import static cars.engine.Vector2.add;
import static cars.engine.Vector2.multiply;

public class HunterCar extends Car {
    private static final double WEIGHT_ARRIVE = 1.0;
    private static final double WEIGHT_WANDER = 0.6;
    private static final double WEIGHT_AVOID = 3.0;
    private static final double SLOW_RADIUS = 150;
    private static final double AVOID_MARGIN = 18;

    private final Behaviors.Wander wander = new Behaviors.Wander(80, 40, 6.0);

    public HunterCar(double x, double y) {
        super(settings ->
            settings
                .color(Color.RED)
                .position(x, y)
                .randomOrientation()
                .maxSpeed(220)
                .maxForce(450)
        );
    }

    @Override
    public Vector2 calculateSteering(final World world) {
        final var click = world.getClickPos();
        final var goal = click == null
            ? multiply(wander.steer(this, world.getSecs()), WEIGHT_WANDER)
            : multiply(Behaviors.arrive(this, click, SLOW_RADIUS), WEIGHT_ARRIVE);
        final var avoid = Behaviors.obstacleAvoidance(this, world.getObstacles(), AVOID_MARGIN);
        return add(goal, multiply(avoid, WEIGHT_AVOID));
    }
}
