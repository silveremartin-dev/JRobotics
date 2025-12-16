/*
 * JRobotics v2 - World-Class General Purpose Robotics API
 * 
 * Copyright (c) 2025 Silvère Martin-Michiellot <silvere.martin@gmail.com>
 * Co-authored by Gemini AI Assistant
 * 
 * Licensed under the MIT License.
 * See LICENSE file in the project root for full license information.
 */
package org.jrobotics.processor.navigation;

import org.jrobotics.simulation.Vector3;

import java.util.List;

/**
 * Dynamic Window Approach for local obstacle avoidance.
 * 
 * <p>Computes velocity commands that navigate toward goal while
 * avoiding obstacles using a sampled velocity space search.</p>
 * 
 * <p><b>Algorithm:</b></p>
 * <ol>
 *   <li>Generate velocity samples in (v, ω) space</li>
 *   <li>Simulate trajectories for each sample</li>
 *   <li>Score trajectories (goal direction, clearance, velocity)</li>
 *   <li>Select best trajectory</li>
 * </ol>
 * 
 * <p><b>References:</b></p>
 * <ul>
 *   <li>Fox, D., Burgard, W., & Thrun, S. (1997). The dynamic window 
 *       approach to collision avoidance. <i>IEEE Robotics & Automation</i>.</li>
 * </ul>
 * 
 * @author Silvère Martin-Michiellot
 * @author Gemini AI Assistant
 * @version 2.0.0
 * @since 2.0.0
 */
public class DynamicWindowApproach {
    
    // Robot constraints
    private double maxV = 1.0;         // m/s
    private double maxW = 1.0;         // rad/s
    private double maxAccel = 0.5;     // m/s²
    private double maxAngAccel = 1.0;  // rad/s²
    
    // Sampling
    private int vSamples = 20;
    private int wSamples = 40;
    
    // Weights
    private double headingWeight = 0.8;
    private double clearanceWeight = 0.2;
    private double velocityWeight = 0.1;
    
    // Simulation
    private double simTime = 2.0;      // seconds
    private double simDt = 0.1;        // seconds
    private double robotRadius = 0.3;  // meters
    
    /**
     * Creates a DWA planner.
     */
    public DynamicWindowApproach() {}
    
    /**
     * Computes velocity command.
     * 
     * @param robotPos current position
     * @param robotHeading current heading (radians)
     * @param currentV current linear velocity
     * @param currentW current angular velocity
     * @param goalPos goal position
     * @param obstacles list of obstacle positions
     * @return velocity command [v, ω]
     */
    public double[] computeVelocity(Vector3 robotPos, double robotHeading,
                                     double currentV, double currentW,
                                     Vector3 goalPos, List<Vector3> obstacles) {
        
        // Dynamic window limits based on kinematic constraints
        double minV = Math.max(0, currentV - maxAccel * simDt);
        double maxVLimit = Math.min(maxV, currentV + maxAccel * simDt);
        double minW = Math.max(-maxW, currentW - maxAngAccel * simDt);
        double maxWLimit = Math.min(maxW, currentW + maxAngAccel * simDt);
        
        double bestV = 0, bestW = 0;
        double bestScore = Double.NEGATIVE_INFINITY;
        
        // Sample velocity space
        for (int vi = 0; vi < vSamples; vi++) {
            double v = minV + (maxVLimit - minV) * vi / (vSamples - 1);
            
            for (int wi = 0; wi < wSamples; wi++) {
                double w = minW + (maxWLimit - minW) * wi / (wSamples - 1);
                
                // Simulate trajectory
                double[] endState = simulateTrajectory(
                    robotPos.x(), robotPos.y(), robotHeading, v, w);
                
                // Check collision
                double clearance = computeClearance(
                    robotPos.x(), robotPos.y(), robotHeading, v, w, obstacles);
                
                if (clearance < robotRadius) {
                    continue; // Skip collision trajectory
                }
                
                // Score trajectory
                double heading = computeHeadingScore(
                    endState[0], endState[1], endState[2], goalPos);
                double velScore = v / maxV;
                
                double score = headingWeight * heading + 
                              clearanceWeight * clearance / 5.0 +
                              velocityWeight * velScore;
                
                if (score > bestScore) {
                    bestScore = score;
                    bestV = v;
                    bestW = w;
                }
            }
        }
        
        return new double[]{bestV, bestW};
    }
    
    private double[] simulateTrajectory(double x, double y, double theta, 
                                         double v, double w) {
        for (double t = 0; t < simTime; t += simDt) {
            x += v * Math.cos(theta) * simDt;
            y += v * Math.sin(theta) * simDt;
            theta += w * simDt;
        }
        return new double[]{x, y, theta};
    }
    
    private double computeClearance(double x, double y, double theta,
                                     double v, double w, List<Vector3> obstacles) {
        double minDist = Double.MAX_VALUE;
        
        double simX = x, simY = y, simTheta = theta;
        for (double t = 0; t < simTime; t += simDt) {
            simX += v * Math.cos(simTheta) * simDt;
            simY += v * Math.sin(simTheta) * simDt;
            simTheta += w * simDt;
            
            for (Vector3 obs : obstacles) {
                double dx = obs.x() - simX;
                double dy = obs.y() - simY;
                double dist = Math.sqrt(dx * dx + dy * dy);
                minDist = Math.min(minDist, dist);
            }
        }
        
        return minDist;
    }
    
    private double computeHeadingScore(double x, double y, double theta, Vector3 goal) {
        double goalAngle = Math.atan2(goal.y() - y, goal.x() - x);
        double diff = Math.abs(normalizeAngle(goalAngle - theta));
        return 1.0 - diff / Math.PI;
    }
    
    private double normalizeAngle(double angle) {
        while (angle > Math.PI) angle -= 2 * Math.PI;
        while (angle < -Math.PI) angle += 2 * Math.PI;
        return angle;
    }
    
    // Setters for configuration
    public void setMaxVelocity(double maxV) { this.maxV = maxV; }
    public void setMaxAngularVelocity(double maxW) { this.maxW = maxW; }
    public void setMaxAcceleration(double maxAccel) { this.maxAccel = maxAccel; }
    public void setRobotRadius(double radius) { this.robotRadius = radius; }
    public void setWeights(double heading, double clearance, double velocity) {
        this.headingWeight = heading;
        this.clearanceWeight = clearance;
        this.velocityWeight = velocity;
    }
}
