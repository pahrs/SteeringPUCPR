package cars.engine;

import java.util.ArrayList;
import java.util.List;

import static cars.engine.Vector2.add;
import static cars.engine.Vector2.distance;
import static cars.engine.Vector2.multiply;
import static cars.engine.Vector2.normalize;
import static cars.engine.Vector2.subtract;

public final class Path {
    private final List<Vector2> points = new ArrayList<>();

    public Path(Vector2... points) {
        if (points.length < 3) {
            throw new IllegalArgumentException("a circuit needs at least 3 points");
        }
        for (var p : points) this.points.add(p.clone());
    }

    public List<Vector2> getPoints() {
        return points.stream().map(Vector2::clone).toList();
    }

    public Vector2 targetAhead(Vector2 from, double lookAhead) {
        final var n = points.size();
        var bestSegment = 0;
        var bestPoint = points.get(0);
        var bestDist = Double.MAX_VALUE;

        for (var i = 0; i < n; i++) {
            final var a = points.get(i);
            final var b = points.get((i + 1) % n);
            final var ab = subtract(b, a);
            final var t = Math.clamp(subtract(from, a).dot(ab) / ab.sizeSqr(), 0.0, 1.0);
            final var candidate = add(a, multiply(ab, t));
            final var d = distance(from, candidate);
            if (d < bestDist) {
                bestDist = d;
                bestSegment = i;
                bestPoint = candidate;
            }
        }

        var current = bestPoint.clone();
        var segment = bestSegment;
        var remaining = lookAhead;

        for (var guard = 0; guard < n * 4; guard++) {
            final var end = points.get((segment + 1) % n);
            final var d = distance(current, end);
            if (d >= remaining) {
                return add(current, multiply(normalize(subtract(end, current)), remaining));
            }
            remaining -= d;
            current = end.clone();
            segment = (segment + 1) % n;
        }
        return current;
    }
}
