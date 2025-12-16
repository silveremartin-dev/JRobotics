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

/**
 * Interface for simulation visualization.
 * 
 * <p>Implementations can render the simulation state to various outputs
 * (console, 2D, 3D graphics).</p>
 * 
 * @author Silvère Martin-Michiellot
 * @author Gemini AI Assistant
 * @version 2.0.0
 * @since 2.0.0
 */
public interface Visualizer {
    
    /**
     * Initializes the visualizer.
     */
    void initialize();
    
    /**
     * Updates the visualization with current simulation state.
     * 
     * @param world the simulation world
     */
    void update(org.jrobotics.simulation.PhysicsWorld world);
    
    /**
     * Renders the current frame.
     */
    void render();
    
    /**
     * Checks if the visualizer is still active.
     * 
     * @return true if active
     */
    boolean isActive();
    
    /**
     * Shuts down the visualizer.
     */
    void shutdown();
    
    /**
     * Sets the camera position.
     * 
     * @param x the x coordinate
     * @param y the y coordinate
     * @param z the z coordinate
     */
    void setCameraPosition(double x, double y, double z);
    
    /**
     * Sets the camera target (look-at point).
     * 
     * @param x the x coordinate
     * @param y the y coordinate
     * @param z the z coordinate
     */
    void setCameraTarget(double x, double y, double z);
}
