package cars.student;

import cars.engine.Car;
import cars.engine.Vector2;
import cars.engine.World;

import java.awt.Color;

import static cars.engine.Vector2.add;
import static cars.engine.Vector2.multiply;

public class PilotCar extends Car {
    private static final double WEIGHT_PATH = 1.0;
    private static final double WEIGHT_AVOID = 3.0;
    private static final double LOOK_AHEAD = 40;
    private static final double PREDICT_TIME = 0.2;
    private static final double AVOID_MARGIN = 18;

    public PilotCar(double x, double y, double orientation) {
        super(settings ->
            settings
                .color(Color.BLUE)
                .position(x, y)
                .orientation(orientation)
                .maxSpeed(140).maxForce(450)
        );
    }

    @Override
    public Vector2 calculateSteering(final World world) {
        final var follow = Behaviors.pathFollowing(this, world.getPath(), LOOK_AHEAD, PREDICT_TIME);
        final var avoid = Behaviors.obstacleAvoidance(this, world.getObstacles(), AVOID_MARGIN);
        return add(multiply(follow, WEIGHT_PATH), multiply(avoid, WEIGHT_AVOID));
    }
}
