package cars.student;

import cars.engine.Car;
import cars.engine.Obstacle;
import cars.engine.Path;
import cars.engine.Vector2;
import cars.engine.World;

import java.util.List;
import java.util.random.RandomGenerator;

import static cars.engine.Vector2.add;
import static cars.engine.Vector2.byAngleSize;
import static cars.engine.Vector2.distance;
import static cars.engine.Vector2.multiply;
import static cars.engine.Vector2.normalize;
import static cars.engine.Vector2.perp;
import static cars.engine.Vector2.perpDot;
import static cars.engine.Vector2.subtract;
import static cars.engine.Vector2.vec2;

public final class Behaviors {
    private Behaviors() {}

    public static Vector2 seek(Car car, Vector2 target) {
        final var desired = multiply(normalize(subtract(target, car.getPosition())), car.getMaxSpeed());
        return subtract(desired, car.getVelocity());
    }

    public static Vector2 flee(Car car, Vector2 threat, double panicRadius) {
        if (threat == null || distance(car.getPosition(), threat) > panicRadius) return vec2();
        final var desired = multiply(normalize(subtract(car.getPosition(), threat)), car.getMaxSpeed());
        return subtract(desired, car.getVelocity());
    }

    public static Vector2 arrive(Car car, Vector2 target, double slowRadius) {
        final var toTarget = subtract(target, car.getPosition());
        final var dist = toTarget.size();
        if (dist < 1e-6) return multiply(car.getVelocity(), -1);
        final var speed = car.getMaxSpeed() * Math.min(dist / slowRadius, 1.0);
        final var desired = multiply(normalize(toTarget), speed);
        return subtract(desired, car.getVelocity());
    }

    public static Vector2 separation(Car car, World world, double radius) {
        final var pos = car.getPosition();
        final var push = vec2();
        for (var other : world.getNeighbors((int) radius)) {
            final var away = subtract(pos, other.getPosition());
            final var dist = away.size();
            if (dist < 1e-6) continue;
            push.add(multiply(normalize(away), 1.0 - dist / radius));
        }
        return push.multiply(car.getMaxForce());
    }

    public static Vector2 obstacleAvoidance(Car car, List<Obstacle> obstacles, double margin) {
        final var pos = car.getPosition();
        final var dir = car.getDirection();
        final var lookAhead = 60 + car.getSpeed() * 0.6;
        final var probes = new Vector2[] {
            add(pos, multiply(dir, lookAhead)),
            add(pos, multiply(dir, lookAhead / 2)),
            pos
        };

        Obstacle threat = null;
        var threatDist = Double.MAX_VALUE;
        for (var obstacle : obstacles) {
            final var center = obstacle.getPosition();
            final var limit = obstacle.getRadius() + margin;
            for (var probe : probes) {
                if (distance(probe, center) < limit) {
                    final var d = distance(pos, center);
                    if (d < threatDist) {
                        threat = obstacle;
                        threatDist = d;
                    }
                    break;
                }
            }
        }
        if (threat == null) return vec2();

        final var toCenter = subtract(threat.getPosition(), pos);
        final var lateral = perp(dir);
        if (perpDot(dir, toCenter) > 0) lateral.negate();
        return lateral.multiply(car.getMaxForce());
    }

    public static Vector2 pathFollowing(Car car, Path path, double lookAhead, double predictTime) {
        final var predicted = add(car.getPosition(), multiply(car.getVelocity(), predictTime));
        return seek(car, path.targetAhead(predicted, lookAhead));
    }

    public static final class Wander {
        private final RandomGenerator rnd = RandomGenerator.getDefault();
        private final double circleDistance;
        private final double circleRadius;
        private final double jitter;
        private double angle = rnd.nextDouble(0, 2 * Math.PI);

        public Wander(double circleDistance, double circleRadius, double jitter) {
            this.circleDistance = circleDistance;
            this.circleRadius = circleRadius;
            this.jitter = jitter;
        }

        public Vector2 steer(Car car, double secs) {
            angle += (rnd.nextDouble() * 2 - 1) * jitter * secs;
            final var center = multiply(car.getDirection(), circleDistance);
            final var offset = byAngleSize(angle, circleRadius);
            return add(center, offset).resize(car.getMaxForce());
        }
    }
}
