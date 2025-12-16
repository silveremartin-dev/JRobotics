/*
 * JRobotics v2 - World-Class General Purpose Robotics API
 * 
 * Copyright (c) 2025 Silvère Martin-Michiellot <silvere.martin@gmail.com>
 * Co-authored by Gemini AI Assistant
 * 
 * Licensed under the MIT License.
 * See LICENSE file in the project root for full license information.
 */
package org.jrobotics.processor.multirobot;

import org.jrobotics.processor.AbstractProcessor;
import org.jrobotics.processor.ProcessorException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * Swarm Controller implementing Reynolds' Boids flocking algorithm.
 * 
 * <p>
 * Calculates steering vectors based on Separation, Alignment, and Cohesion.
 * </p>
 * 
 * @author Silvère Martin-Michiellot
 * @author Gemini AI Assistant
 * @version 2.0.0
 * @since 2.5.0
 */
public class SwarmController extends AbstractProcessor<List<SwarmController.Neighbor>, double[]> {

    private static final Logger logger = LoggerFactory.getLogger(SwarmController.class);

    // Weights
    private double separationWeight = 1.5;
    private double alignmentWeight = 1.0;
    private double cohesionWeight = 1.0;
    private double maxSpeed = 1.0;
    private double maxForce = 0.1;
    private double perceptionRadius = 2.0;
    private double separationRadius = 1.0;

    public SwarmController(String id) {
        super(id, "Swarm-" + id);
    }

    /**
     * Processes neighbor states to calculate steering force.
     * 
     * @param neighbors list of perceived neighbors
     * @return velocity command [vx, vy]
     */
    @Override
    protected double[] doProcess(List<Neighbor> neighbors) throws Exception {
        if (neighbors == null || neighbors.isEmpty()) {
            return new double[] { 0, 0 }; // No neighbors, keep current or wander
        }

        double[] sep = separate(neighbors);
        double[] ali = align(neighbors);
        double[] coh = cohesion(neighbors);

        double ax = sep[0] * separationWeight + ali[0] * alignmentWeight + coh[0] * cohesionWeight;
        double ay = sep[1] * separationWeight + ali[1] * alignmentWeight + coh[1] * cohesionWeight;

        // Limit force/acceleration then integrate to velocity (simplified)
        return new double[] { ax, ay };
    }

    private double[] separate(List<Neighbor> neighbors) {
        double steerX = 0, steerY = 0;
        int count = 0;

        for (Neighbor n : neighbors) {
            double d = Math.sqrt(n.dx * n.dx + n.dy * n.dy);
            if (d > 0 && d < separationRadius) {
                // Vector away from neighbor
                double diffX = -n.dx / d; // Normalized
                double diffY = -n.dy / d;

                // Weight by distance
                diffX /= d;
                diffY /= d;

                steerX += diffX;
                steerY += diffY;
                count++;
            }
        }

        if (count > 0) {
            steerX /= count;
            steerY /= count;
            // Limit magnitude?
        }
        return new double[] { steerX, steerY };
    }

    private double[] align(List<Neighbor> neighbors) {
        double sumVx = 0, sumVy = 0;
        int count = 0;

        for (Neighbor n : neighbors) {
            double d = Math.sqrt(n.dx * n.dx + n.dy * n.dy);
            if (d > 0 && d < perceptionRadius) {
                sumVx += n.vx;
                sumVy += n.vy;
                count++;
            }
        }

        if (count > 0) {
            sumVx /= count;
            sumVy /= count;
            // Seek this target velocity
            return new double[] { sumVx, sumVy }; // Simplified: returning desired direction
        }
        return new double[] { 0, 0 };
    }

    private double[] cohesion(List<Neighbor> neighbors) {
        double sumX = 0, sumY = 0;
        int count = 0;

        for (Neighbor n : neighbors) {
            double d = Math.sqrt(n.dx * n.dx + n.dy * n.dy);
            if (d > 0 && d < perceptionRadius) {
                sumX += n.dx; // Relative position
                sumY += n.dy;
                count++;
            }
        }

        if (count > 0) {
            sumX /= count;
            sumY /= count;
            // Steer towards center assuming we are at (0,0) in relative frame
            return new double[] { sumX, sumY };
        }
        return new double[] { 0, 0 };
    }

    public void setWeights(double sep, double ali, double coh) {
        this.separationWeight = sep;
        this.alignmentWeight = ali;
        this.cohesionWeight = coh;
    }

    /**
     * Represents a perceived neighbor.
     */
    public static class Neighbor {
        public double dx, dy; // Relative position
        public double vx, vy; // Relative velocity or absolute velocity

        public Neighbor(double dx, double dy, double vx, double vy) {
            this.dx = dx;
            this.dy = dy;
            this.vx = vx;
            this.vy = vy;
        }
    }
}
