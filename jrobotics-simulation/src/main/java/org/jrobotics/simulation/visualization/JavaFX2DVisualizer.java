/*
 * JRobotics v2 - World-Class General Purpose Robotics API
 * 
 * Copyright (c) 2025 Silvère Martin-Michiellot <silvere.martin@gmail.com>
 * Co-authored by Gemini AI Assistant
 * 
 * Licensed under the MIT License.
 * See LICENSE file in the project root for full license information.
 */
package org.jrobotics.simulation.visualization;

import javafx.animation.AnimationTimer;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.stage.Stage;
import org.jrobotics.simulation.PhysicsBody;
import org.jrobotics.simulation.PhysicsWorld;
import org.jrobotics.core.math.Vector3;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicReference;

/**
 * 2D visualizer using JavaFX Canvas.
 * 
 * <p>
 * Modern, smooth 2D visualization with trails and anti-aliasing.
 * </p>
 * 
 * @author Silvère Martin-Michiellot
 * @author Gemini AI Assistant
 * @version 2.0.0
 * @since 2.0.0
 */
public class JavaFX2DVisualizer implements Visualizer {

    private static final Logger logger = LoggerFactory.getLogger(JavaFX2DVisualizer.class);

    private final String title;
    private final int width;
    private final int height;
    private double scale = 30.0;
    private double offsetX = 0;
    private double offsetY = 0;

    private final AtomicReference<PhysicsWorld> worldRef = new AtomicReference<>();
    private volatile boolean active = false;
    // private volatile boolean initialized = false;

    private final List<double[]> trails = new CopyOnWriteArrayList<>();
    private boolean showTrails = true;
    private int maxTrailPoints = 500;

    private Stage stage;
    private Canvas canvas;
    private AnimationTimer timer;

    /**
     * Creates a JavaFX 2D visualizer.
     */
    public JavaFX2DVisualizer() {
        this("JRobotics Simulation", 1024, 768);
    }

    /**
     * Creates a JavaFX 2D visualizer.
     */
    public JavaFX2DVisualizer(String title, int width, int height) {
        this.title = title;
        this.width = width;
        this.height = height;
    }

    @Override
    public void initialize() {
        CountDownLatch latch = new CountDownLatch(1);

        // Start JavaFX if not already running
        Thread fxThread = new Thread(() -> {
            Application.launch(FXApp.class, title, String.valueOf(width), String.valueOf(height));
        }, "JavaFX-Visualizer");
        fxThread.setDaemon(true);
        fxThread.start();

        // Wait for JavaFX to initialize
        try {
            Thread.sleep(500);
        } catch (InterruptedException ignored) {
        }

        Platform.runLater(() -> {
            FXApp app = FXApp.getInstance();
            if (app != null) {
                this.stage = app.getStage();
                this.canvas = app.getCanvas();
                this.timer = createAnimationTimer();
                this.timer.start();
                active = true;
                // initialized = true;
            }
            latch.countDown();
        });

        try {
            latch.await();
        } catch (InterruptedException ignored) {
        }

        logger.info("[{}] JavaFX2DVisualizer initialized ({}x{})",
                System.currentTimeMillis(), width, height);
    }

    private AnimationTimer createAnimationTimer() {
        return new AnimationTimer() {
            @Override
            public void handle(long now) {
                render();
            }
        };
    }

    @Override
    public void update(PhysicsWorld world) {
        worldRef.set(world);

        // Record trails
        if (showTrails && world != null) {
            for (PhysicsBody body : world.getBodies()) {
                if (!body.isStatic()) {
                    Vector3 pos = body.position();
                    trails.add(new double[] { pos.x(), pos.y() });
                    while (trails.size() > maxTrailPoints) {
                        trails.remove(0);
                    }
                    break;
                }
            }
        }
    }

    @Override
    public void render() {
        if (canvas == null)
            return;

        GraphicsContext gc = canvas.getGraphicsContext2D();
        double w = canvas.getWidth();
        double h = canvas.getHeight();
        double cx = w / 2;
        double cy = h / 2;

        // Clear background
        gc.setFill(Color.rgb(30, 30, 40));
        gc.fillRect(0, 0, w, h);

        // Draw grid
        drawGrid(gc, cx, cy, w, h);

        // Draw trails
        if (showTrails && trails.size() > 1) {
            gc.setStroke(Color.rgb(80, 180, 255, 0.3));
            gc.setLineWidth(2);
            gc.beginPath();
            double[] first = trails.get(0);
            gc.moveTo(cx + (first[0] - offsetX) * scale, cy - (first[1] - offsetY) * scale);
            for (int i = 1; i < trails.size(); i++) {
                double[] p = trails.get(i);
                gc.lineTo(cx + (p[0] - offsetX) * scale, cy - (p[1] - offsetY) * scale);
            }
            gc.stroke();
        }

        // Draw bodies
        PhysicsWorld world = worldRef.get();
        if (world != null) {
            for (PhysicsBody body : world.getBodies()) {
                drawBody(gc, body, cx, cy);
            }

            // Draw info
            drawInfo(gc, world);
        }
    }

    private void drawGrid(GraphicsContext gc, double cx, double cy, double w, double h) {
        gc.setStroke(Color.rgb(60, 60, 80));
        gc.setLineWidth(1);

        double gridSize = scale;

        for (double x = cx % gridSize; x < w; x += gridSize) {
            gc.strokeLine(x, 0, x, h);
        }
        for (double y = cy % gridSize; y < h; y += gridSize) {
            gc.strokeLine(0, y, w, y);
        }

        // Axes
        gc.setStroke(Color.rgb(200, 200, 220));
        gc.setLineWidth(2);
        gc.strokeLine(0, cy, w, cy);
        gc.strokeLine(cx, 0, cx, h);
    }

    private void drawBody(GraphicsContext gc, PhysicsBody body, double cx, double cy) {
        Vector3 pos = body.position();
        double x = cx + (pos.x() - offsetX) * scale;
        double y = cy - (pos.y() - offsetY) * scale;
        double r = Math.max(5, body.boundingRadius() * scale);

        if (body.isStatic()) {
            gc.setFill(Color.rgb(100, 100, 120));
            gc.fillRect(x - r, y - r, r * 2, r * 2);
            gc.setStroke(Color.rgb(80, 80, 100));
            gc.strokeRect(x - r, y - r, r * 2, r * 2);
        } else {
            // Robot
            gc.setFill(Color.rgb(80, 180, 255));
            gc.fillOval(x - r, y - r, r * 2, r * 2);
            gc.setStroke(Color.rgb(120, 200, 255));
            gc.setLineWidth(2);
            gc.strokeOval(x - r, y - r, r * 2, r * 2);

            // Orientation arrow
            Vector3 orient = body.orientation();
            double angle = orient.z();
            double ax = x + Math.cos(angle) * (r + 8);
            double ay = y - Math.sin(angle) * (r + 8);
            gc.strokeLine(x, y, ax, ay);
        }
    }

    private void drawInfo(GraphicsContext gc, PhysicsWorld world) {
        gc.setFill(Color.rgb(200, 200, 220));
        gc.setFont(Font.font("Monospaced", 14));

        int y = 25;
        gc.fillText("Step: " + world.getStepCount(), 15, y);
        y += 20;
        gc.fillText("Bodies: " + world.getBodies().size(), 15, y);
        y += 20;
        gc.fillText(String.format("Scale: %.0f px/m", scale), 15, y);

        for (PhysicsBody body : world.getBodies()) {
            if (!body.isStatic()) {
                Vector3 pos = body.position();
                y += 20;
                gc.fillText(String.format("Pos: (%.2f, %.2f)", pos.x(), pos.y()), 15, y);
                break;
            }
        }
    }

    @Override
    public boolean isActive() {
        return active && stage != null && stage.isShowing();
    }

    @Override
    public void shutdown() {
        active = false;
        if (timer != null) {
            timer.stop();
        }
        Platform.runLater(() -> {
            if (stage != null) {
                stage.close();
            }
        });
        logger.info("[{}] JavaFX2DVisualizer shut down", System.currentTimeMillis());
    }

    @Override
    public void setCameraPosition(double x, double y, double z) {
        this.offsetX = x;
        this.offsetY = y;
    }

    @Override
    public void setCameraTarget(double x, double y, double z) {
        this.offsetX = x;
        this.offsetY = y;
    }

    public void setScale(double scale) {
        this.scale = scale;
    }

    public void setShowTrails(boolean show) {
        this.showTrails = show;
    }

    /**
     * JavaFX Application wrapper.
     */
    public static class FXApp extends Application {

        private static FXApp instance;
        private Stage stage;
        private Canvas canvas;

        @Override
        public void start(Stage primaryStage) {
            instance = this;
            this.stage = primaryStage;

            Parameters params = getParameters();
            List<String> args = params.getRaw();

            String title = args.size() > 0 ? args.get(0) : "JRobotics";
            int width = args.size() > 1 ? Integer.parseInt(args.get(1)) : 1024;
            int height = args.size() > 2 ? Integer.parseInt(args.get(2)) : 768;

            canvas = new Canvas(width, height);
            StackPane root = new StackPane(canvas);
            root.setStyle("-fx-background-color: #1e1e28;");

            // Bind canvas size to window
            canvas.widthProperty().bind(root.widthProperty());
            canvas.heightProperty().bind(root.heightProperty());

            Scene scene = new Scene(root, width, height);
            primaryStage.setTitle(title);
            primaryStage.setScene(scene);
            primaryStage.show();
        }

        public static FXApp getInstance() {
            return instance;
        }

        public Stage getStage() {
            return stage;
        }

        public Canvas getCanvas() {
            return canvas;
        }
    }
}
