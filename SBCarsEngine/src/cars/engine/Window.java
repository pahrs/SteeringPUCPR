package cars.engine;

import cars.student.Setup;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Path2D;
import java.util.List;

public final class Window extends JFrame implements Runnable {
    public static final int INITIAL_WIDTH = 1024;
    public static final int INITIAL_HEIGHT = 768;

    private final List<Car> cars;
    private final List<Obstacle> obstacles;
    private final Path path;
    private Vector2 clickPos = null;
    private Vector2 mousePos = null;
    private boolean debugMode = true;

    private Window() {
        super("Steering behaviors");
        setSize(INITIAL_WIDTH, INITIAL_HEIGHT);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setIgnoreRepaint(true);
        setResizable(true);
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowOpened(WindowEvent e) {
                start();
            }
        });

        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                // click position relative to the centered origin
                clickPos = new Vector2(
                    e.getX() - getWidth() / 2.0,
                    e.getY() - getHeight() / 2.0
                );
            }
        });

        addMouseMotionListener(new MouseAdapter() {
            @Override
            public void mouseMoved(MouseEvent e) {
                mousePos = new Vector2(
                    e.getX() - getWidth() / 2.0,
                    e.getY() - getHeight() / 2.0
                );
            }
        });

        addKeyListener(new KeyAdapter() {
            @Override
            public void keyTyped(KeyEvent e) {
                if ("D".equalsIgnoreCase("" + e.getKeyChar())) {
                    debugMode = !debugMode;
                }
            }
        });
        requestFocus();
        final var setup = new Setup();
        this.cars = setup.createCars();
        this.obstacles = setup.createObstacles();
        this.path = setup.createPath();
    }

    public static void main(String[] args) {
        EventQueue.invokeLater(() -> new Window().setVisible(true));
    }

    private void start() {
        createBufferStrategy(3);
        final var gameLoop = new Thread(this, "game-loop");
        gameLoop.setDaemon(true);
        gameLoop.start();
    }

    @Override
    public void run() {
        double prev = System.currentTimeMillis() - 1;
        try {
            final var strategy = getBufferStrategy();
            while (true) {
                final double now = System.currentTimeMillis();
                final var g2d = (Graphics2D) strategy.getDrawGraphics();

                draw(g2d);
                update((now - prev) / 1000.0);

                g2d.dispose();
                Toolkit.getDefaultToolkit().sync(); // helps on some systems
                prev = now;
                strategy.show();

                Thread.sleep(1);
            }
        } catch (InterruptedException e) {
            System.err.println("Interrupted");
        }
        System.exit(0);
    }

    private void update(final double secs) {
        cars.forEach(car -> car.update(
            new World(
                    secs, car, cars,
                    mousePos, clickPos,
                    getWidth(), getHeight(),
                    obstacles, path
                )
            )
        );
    }

    private void draw(Graphics2D g2d) {
        // Quality hints
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
        g2d.setRenderingHint(RenderingHints.KEY_ALPHA_INTERPOLATION, RenderingHints.VALUE_ALPHA_INTERPOLATION_QUALITY);
        g2d.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS, RenderingHints.VALUE_FRACTIONALMETRICS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);

        // Clear
        g2d.setBackground(new Color(220, 220, 220));
        g2d.clearRect(0, 0, getWidth(), getHeight());

        // Center the world origin
        g2d.translate(getWidth() / 2.0, getHeight() / 2.0);

        // Click marker (relative to center)
        if (clickPos != null) {
            g2d.setColor(Color.GRAY);
            g2d.fillOval((int) clickPos.x - 4, (int) clickPos.y - 4, 8, 8);
        }

        drawPath(g2d);
        drawObstacles(g2d);

        // Draw cars
        cars.forEach(car -> car.draw(g2d, debugMode));

        g2d.setFont(new Font("Arial", Font.PLAIN, 10));
        g2d.setColor(Color.DARK_GRAY);
        g2d.drawString(
            "Click: red car arrives at the point | Mouse: green cars flee from the cursor",
            20.0f - getWidth() / 2.0f, getHeight() / 2.0f - 36.0f
        );
        g2d.drawString(
            "Press D to turn debug arrows %s".formatted(debugMode ? "off" : "on"),
            20.0f - getWidth() / 2.0f, getHeight() / 2.0f - 20.0f
        );
    }

    private void drawPath(Graphics2D g2d) {
        final var points = path.getPoints();
        final var shape = new Path2D.Double();
        shape.moveTo(points.get(0).x, points.get(0).y);
        for (var i = 1; i < points.size(); i++) {
            shape.lineTo(points.get(i).x, points.get(i).y);
        }
        shape.closePath();

        final var oldStroke = g2d.getStroke();
        g2d.setColor(new Color(170, 170, 200));
        g2d.setStroke(new BasicStroke(3f));
        g2d.draw(shape);
        g2d.setStroke(oldStroke);
        for (var p : points) {
            g2d.fill(new Ellipse2D.Double(p.x - 4, p.y - 4, 8, 8));
        }
    }

    private void drawObstacles(Graphics2D g2d) {
        for (var obstacle : obstacles) {
            final var p = obstacle.getPosition();
            final var r = obstacle.getRadius();
            final var circle = new Ellipse2D.Double(p.x - r, p.y - r, r * 2, r * 2);
            g2d.setColor(new Color(110, 110, 110));
            g2d.fill(circle);
            g2d.setColor(Color.DARK_GRAY);
            g2d.draw(circle);
        }
    }
}
