package cars.student;

import cars.engine.Car;
import cars.engine.Obstacle;
import cars.engine.Path;

import java.util.ArrayList;
import java.util.List;

import static cars.engine.Vector2.vec2;

public class Setup {
    private static final double PILOT_START_X = -330;
    private static final double PILOT_START_Y = -220;

    public List<Car> createCars() {
        final var cars = new ArrayList<Car>();
        cars.add(new PilotCar(
            PILOT_START_X, PILOT_START_Y,
            Math.atan2(-50, 330)
        ));
        cars.add(new HunterCar(200, 150));
        cars.add(new HerdCar(-300, 60));
        cars.add(new HerdCar(-260, 100));
        cars.add(new HerdCar(-220, 140));
        cars.add(new HerdCar(-300, 140));
        cars.add(new HerdCar(-260, 60));
        cars.add(new HerdCar(-340, 100));
        return cars;
    }

    public List<Obstacle> createObstacles() {
        return List.of(
            new Obstacle(-165, -245, 35),
            new Obstacle(365, -110, 30),
            new Obstacle(-120, 20, 50),
            new Obstacle(130, -30, 45),
            new Obstacle(0, 150, 40)
        );
    }

    public Path createPath() {
        return new Path(
            vec2(-330, -220),
            vec2(0, -270),
            vec2(330, -220),
            vec2(400, 0),
            vec2(330, 220),
            vec2(0, 270),
            vec2(-330, 220),
            vec2(-400, 0)
        );
    }
}
