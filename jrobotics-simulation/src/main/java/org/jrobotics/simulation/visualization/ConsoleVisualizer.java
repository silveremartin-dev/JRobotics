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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Console-based ASCII visualization for debugging.
 * 
 * <p>Renders a top-down 2D view of the simulation to the console.</p>
 * 
 * @author Silvère Martin-Michiellot
 * @author Gemini AI Assistant
 * @version 2.0.0
 * @since 2.0.0
 */
public class ConsoleVisualizer implements Visualizer {
    
    private static final Logger logger = LoggerFactory.getLogger(ConsoleVisualizer.class);
    
    private final int width;
    private final int height;
    private final double scale; // meters per character
    private boolean active = false;
    private char[][] buffer;
    private PhysicsWorld currentWorld;
    
    /**
     * Creates a console visualizer with default size.
     */
    public ConsoleVisualizer() {
        this(80, 24, 0.5);
    }
    
    /**
     * Creates a console visualizer.
     * 
     * @param width the width in characters
     * @param height the height in characters
     * @param scale meters per character
     */
    public ConsoleVisualizer(int width, int height, double scale) {
        this.width = width;
        this.height = height;
        this.scale = scale;
        this.buffer = new char[height][width];
    }
    
    @Override
    public void initialize() {
        active = true;
        logger.info("ConsoleVisualizer initialized ({}x{}, scale={})", width, height, scale);
    }
    
    @Override
    public void update(PhysicsWorld world) {
        this.currentWorld = world;
        
        // Clear buffer
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                buffer[y][x] = '.';
            }
        }
        
        // Draw border
        for (int x = 0; x < width; x++) {
            buffer[0][x] = '-';
            buffer[height - 1][x] = '-';
        }
        for (int y = 0; y < height; y++) {
            buffer[y][0] = '|';
            buffer[y][width - 1] = '|';
        }
        buffer[0][0] = '+';
        buffer[0][width - 1] = '+';
        buffer[height - 1][0] = '+';
        buffer[height - 1][width - 1] = '+';
        
        // Draw bodies
        for (PhysicsBody body : world.getBodies()) {
            int x = (int) (body.position().x() / scale) + width / 2;
            int y = height / 2 - (int) (body.position().y() / scale);
            
            if (x > 0 && x < width - 1 && y > 0 && y < height - 1) {
                char symbol = body.isStatic() ? '#' : 'O';
                buffer[y][x] = symbol;
            }
        }
    }
    
    @Override
    public void render() {
        StringBuilder sb = new StringBuilder();
        sb.append("\033[H\033[2J"); // ANSI clear screen (optional)
        sb.append("\n");
        
        for (int y = 0; y < height; y++) {
            sb.append(new String(buffer[y])).append("\n");
        }
        
        if (currentWorld != null) {
            sb.append(String.format("Step: %d | Bodies: %d%n", 
                    currentWorld.getStepCount(), currentWorld.getBodies().size()));
        }
        
        System.out.print(sb);
    }
    
    @Override
    public boolean isActive() {
        return active;
    }
    
    @Override
    public void shutdown() {
        active = false;
        logger.info("ConsoleVisualizer shut down");
    }
    
    @Override
    public void setCameraPosition(double x, double y, double z) {
        // Console visualizer is fixed top-down view
    }
    
    @Override
    public void setCameraTarget(double x, double y, double z) {
        // Console visualizer is fixed top-down view
    }
    
    /**
     * Gets the buffer for testing.
     */
    public char[][] getBuffer() {
        return buffer;
    }
}
