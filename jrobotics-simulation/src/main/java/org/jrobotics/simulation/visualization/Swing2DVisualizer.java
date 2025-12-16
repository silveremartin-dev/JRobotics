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

import org.jrobotics.simulation.PhysicsBody;
import org.jrobotics.simulation.PhysicsWorld;
import org.jrobotics.simulation.Vector3;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.*;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * 2D graphical visualizer using Java Swing.
 * 
 * <p>Provides a real-time graphical view of the simulation.</p>
 * 
 * @author Silvère Martin-Michiellot
 * @author Gemini AI Assistant
 * @version 2.0.0
 * @since 2.0.0
 */
public class Swing2DVisualizer implements Visualizer {
    
    private static final Logger logger = LoggerFactory.getLogger(Swing2DVisualizer.class);
    
    private final String title;
    private final int width;
    private final int height;
    private double scale = 30.0; // pixels per meter
    private double offsetX = 0;
    private double offsetY = 0;
    
    private JFrame frame;
    private SimulationPanel panel;
    private PhysicsWorld currentWorld;
    private boolean active = false;
    
    // Trail history
    private final List<Point2D.Double> trails = new CopyOnWriteArrayList<>();
    private boolean showTrails = true;
    private int maxTrailPoints = 500;
    
    /**
     * Creates a 2D visualizer with default size.
     */
    public Swing2DVisualizer() {
        this("JRobotics Simulation", 800, 600);
    }
    
    /**
     * Creates a 2D visualizer.
     * 
     * @param title the window title
     * @param width the window width
     * @param height the window height
     */
    public Swing2DVisualizer(String title, int width, int height) {
        this.title = title;
        this.width = width;
        this.height = height;
    }
    
    @Override
    public void initialize() {
        SwingUtilities.invokeLater(() -> {
            frame = new JFrame(title);
            frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
            frame.setSize(width, height);
            frame.setLocationRelativeTo(null);
            
            panel = new SimulationPanel();
            frame.add(panel);
            
            frame.setVisible(true);
            active = true;
            
            logger.info("[{}] Swing2DVisualizer initialized ({}x{})", 
                    System.currentTimeMillis(), width, height);
        });
        
        // Wait for window to be created
        try { Thread.sleep(100); } catch (InterruptedException ignored) {}
    }
    
    @Override
    public void update(PhysicsWorld world) {
        this.currentWorld = world;
        
        // Record trails for first dynamic body
        if (showTrails && world != null) {
            for (PhysicsBody body : world.getBodies()) {
                if (!body.isStatic()) {
                    Vector3 pos = body.position();
                    trails.add(new Point2D.Double(pos.x(), pos.y()));
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
        if (panel != null) {
            panel.repaint();
        }
    }
    
    @Override
    public boolean isActive() {
        return active && frame != null && frame.isVisible();
    }
    
    @Override
    public void shutdown() {
        active = false;
        if (frame != null) {
            SwingUtilities.invokeLater(() -> frame.dispose());
        }
        logger.info("[{}] Swing2DVisualizer shut down", System.currentTimeMillis());
    }
    
    @Override
    public void setCameraPosition(double x, double y, double z) {
        this.offsetX = x;
        this.offsetY = y;
    }
    
    @Override
    public void setCameraTarget(double x, double y, double z) {
        // In 2D, we just center on target
        this.offsetX = x;
        this.offsetY = y;
    }
    
    /**
     * Sets the scale (pixels per meter).
     */
    public void setScale(double scale) {
        this.scale = scale;
    }
    
    /**
     * Enables/disables trail rendering.
     */
    public void setShowTrails(boolean show) {
        this.showTrails = show;
    }
    
    /**
     * Clears trails.
     */
    public void clearTrails() {
        trails.clear();
    }
    
    /**
     * The simulation rendering panel.
     */
    private class SimulationPanel extends JPanel {
        
        private final Color BACKGROUND = new Color(30, 30, 40);
        private final Color GRID_COLOR = new Color(60, 60, 80);
        private final Color STATIC_COLOR = new Color(100, 100, 120);
        private final Color DYNAMIC_COLOR = new Color(80, 180, 255);
        private final Color TRAIL_COLOR = new Color(80, 180, 255, 80);
        private final Color TEXT_COLOR = new Color(200, 200, 220);
        
        public SimulationPanel() {
            setBackground(BACKGROUND);
            setDoubleBuffered(true);
        }
        
        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2d = (Graphics2D) g;
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            
            int centerX = getWidth() / 2;
            int centerY = getHeight() / 2;
            
            // Draw grid
            drawGrid(g2d, centerX, centerY);
            
            // Draw trails
            if (showTrails && !trails.isEmpty()) {
                g2d.setColor(TRAIL_COLOR);
                for (int i = 1; i < trails.size(); i++) {
                    Point2D.Double p1 = trails.get(i - 1);
                    Point2D.Double p2 = trails.get(i);
                    int x1 = centerX + (int) ((p1.x - offsetX) * scale);
                    int y1 = centerY - (int) ((p1.y - offsetY) * scale);
                    int x2 = centerX + (int) ((p2.x - offsetX) * scale);
                    int y2 = centerY - (int) ((p2.y - offsetY) * scale);
                    g2d.drawLine(x1, y1, x2, y2);
                }
            }
            
            // Draw bodies
            if (currentWorld != null) {
                for (PhysicsBody body : currentWorld.getBodies()) {
                    drawBody(g2d, body, centerX, centerY);
                }
                
                // Draw info
                drawInfo(g2d);
            }
        }
        
        private void drawGrid(Graphics2D g, int cx, int cy) {
            g.setColor(GRID_COLOR);
            g.setStroke(new BasicStroke(1));
            
            int gridSize = (int) scale; // 1 meter grid
            
            // Vertical lines
            for (int x = cx % gridSize; x < getWidth(); x += gridSize) {
                g.drawLine(x, 0, x, getHeight());
            }
            // Horizontal lines
            for (int y = cy % gridSize; y < getHeight(); y += gridSize) {
                g.drawLine(0, y, getWidth(), y);
            }
            
            // Axes
            g.setColor(TEXT_COLOR);
            g.setStroke(new BasicStroke(2));
            g.drawLine(0, cy, getWidth(), cy);
            g.drawLine(cx, 0, cx, getHeight());
        }
        
        private void drawBody(Graphics2D g, PhysicsBody body, int cx, int cy) {
            Vector3 pos = body.position();
            int x = cx + (int) ((pos.x() - offsetX) * scale);
            int y = cy - (int) ((pos.y() - offsetY) * scale);
            int r = Math.max(3, (int) (body.boundingRadius() * scale));
            
            if (body.isStatic()) {
                g.setColor(STATIC_COLOR);
                g.fillRect(x - r, y - r, r * 2, r * 2);
                g.setColor(STATIC_COLOR.darker());
                g.drawRect(x - r, y - r, r * 2, r * 2);
            } else {
                // Draw robot body
                g.setColor(DYNAMIC_COLOR);
                g.fillOval(x - r, y - r, r * 2, r * 2);
                g.setColor(DYNAMIC_COLOR.brighter());
                g.drawOval(x - r, y - r, r * 2, r * 2);
                
                // Draw orientation arrow
                Vector3 orient = body.orientation();
                double angle = orient.z(); // yaw
                int arrowLen = r + 5;
                int ax = x + (int) (Math.cos(angle) * arrowLen);
                int ay = y - (int) (Math.sin(angle) * arrowLen);
                g.setStroke(new BasicStroke(2));
                g.drawLine(x, y, ax, ay);
            }
        }
        
        private void drawInfo(Graphics2D g) {
            g.setColor(TEXT_COLOR);
            g.setFont(new Font("Monospaced", Font.PLAIN, 12));
            
            int y = 20;
            g.drawString("Step: " + currentWorld.getStepCount(), 10, y);
            y += 15;
            g.drawString("Bodies: " + currentWorld.getBodies().size(), 10, y);
            y += 15;
            g.drawString("Scale: " + String.format("%.0f px/m", scale), 10, y);
            
            // Show first dynamic body position
            for (PhysicsBody body : currentWorld.getBodies()) {
                if (!body.isStatic()) {
                    Vector3 pos = body.position();
                    y += 15;
                    g.drawString(String.format("Pos: (%.2f, %.2f)", pos.x(), pos.y()), 10, y);
                    break;
                }
            }
        }
    }
}
