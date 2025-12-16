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

import org.jrobotics.simulation.Vector3;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Multi-robot formation controller.
 * 
 * <p>Coordinates multiple robots to maintain geometric formations
 * while following a leader or navigating to goals.</p>
 * 
 * <p><b>Supported formation types:</b></p>
 * <ul>
 *   <li>Line - robots in a line</li>
 *   <li>Column - robots in a column</li>
 *   <li>V-Formation - wedge pattern</li>
 *   <li>Circle - robots around center</li>
 *   <li>Custom - user-defined offsets</li>
 * </ul>
 * 
 * @author Silvère Martin-Michiellot
 * @author Gemini AI Assistant
 * @version 2.0.0
 * @since 2.0.0
 */
public class FormationController {
    
    public enum FormationType { LINE, COLUMN, V_FORMATION, CIRCLE, CUSTOM }
    
    private final List<String> robotIds;
    private final Map<String, Vector3> formationOffsets;
    private FormationType formationType = FormationType.LINE;
    private double spacing = 1.5; // meters
    private String leaderId;
    
    // Control gains
    private double positionGain = 1.0;
    private double velocityGain = 0.5;
    
    /**
     * Creates a formation controller.
     */
    public FormationController() {
        this.robotIds = new ArrayList<>();
        this.formationOffsets = new HashMap<>();
    }
    
    /**
     * Adds a robot to the formation.
     * 
     * @param robotId robot identifier
     */
    public void addRobot(String robotId) {
        if (!robotIds.contains(robotId)) {
            robotIds.add(robotId);
            if (leaderId == null) {
                leaderId = robotId;
            }
            updateFormationOffsets();
        }
    }
    
    /**
     * Removes a robot from the formation.
     */
    public void removeRobot(String robotId) {
        robotIds.remove(robotId);
        formationOffsets.remove(robotId);
        if (robotId.equals(leaderId) && !robotIds.isEmpty()) {
            leaderId = robotIds.get(0);
        }
        updateFormationOffsets();
    }
    
    /**
     * Sets the formation type.
     */
    public void setFormationType(FormationType type) {
        this.formationType = type;
        updateFormationOffsets();
    }
    
    /**
     * Sets robot spacing.
     */
    public void setSpacing(double spacing) {
        this.spacing = spacing;
        updateFormationOffsets();
    }
    
    /**
     * Sets the leader robot.
     */
    public void setLeader(String robotId) {
        if (robotIds.contains(robotId)) {
            this.leaderId = robotId;
            updateFormationOffsets();
        }
    }
    
    /**
     * Computes velocity command for a follower robot.
     * 
     * @param robotId robot identifier
     * @param robotPos current robot position
     * @param leaderPos leader position
     * @param leaderHeading leader heading in radians
     * @return velocity command [vx, vy]
     */
    public double[] computeVelocity(String robotId, Vector3 robotPos, 
                                     Vector3 leaderPos, double leaderHeading) {
        Vector3 offset = formationOffsets.get(robotId);
        if (offset == null) {
            return new double[]{0, 0};
        }
        
        // Rotate offset by leader heading
        double cos = Math.cos(leaderHeading);
        double sin = Math.sin(leaderHeading);
        double targetX = leaderPos.x() + offset.x() * cos - offset.y() * sin;
        double targetY = leaderPos.y() + offset.x() * sin + offset.y() * cos;
        
        // Position error
        double errX = targetX - robotPos.x();
        double errY = targetY - robotPos.y();
        
        // Velocity command
        double vx = positionGain * errX;
        double vy = positionGain * errY;
        
        // Limit velocity
        double maxV = 2.0;
        double v = Math.sqrt(vx * vx + vy * vy);
        if (v > maxV) {
            vx = vx / v * maxV;
            vy = vy / v * maxV;
        }
        
        return new double[]{vx, vy};
    }
    
    /**
     * Gets desired position for a robot in formation.
     */
    public Vector3 getDesiredPosition(String robotId, Vector3 leaderPos, double leaderHeading) {
        Vector3 offset = formationOffsets.get(robotId);
        if (offset == null) {
            return leaderPos;
        }
        
        double cos = Math.cos(leaderHeading);
        double sin = Math.sin(leaderHeading);
        return new Vector3(
            leaderPos.x() + offset.x() * cos - offset.y() * sin,
            leaderPos.y() + offset.x() * sin + offset.y() * cos,
            0
        );
    }
    
    private void updateFormationOffsets() {
        formationOffsets.clear();
        int n = robotIds.size();
        
        for (int i = 0; i < n; i++) {
            String robotId = robotIds.get(i);
            Vector3 offset;
            
            if (robotId.equals(leaderId)) {
                offset = Vector3.ZERO;
            } else {
                int followerIndex = robotIds.indexOf(robotId);
                if (followerIndex > robotIds.indexOf(leaderId)) {
                    followerIndex--;
                }
                offset = computeOffset(followerIndex, n - 1);
            }
            
            formationOffsets.put(robotId, offset);
        }
    }
    
    private Vector3 computeOffset(int index, int totalFollowers) {
        return switch (formationType) {
            case LINE -> new Vector3(0, -(index + 1) * spacing, 0);
            case COLUMN -> new Vector3(-(index + 1) * spacing, 0, 0);
            case V_FORMATION -> {
                int side = (index % 2 == 0) ? 1 : -1;
                int row = (index / 2) + 1;
                yield new Vector3(-row * spacing, side * row * spacing * 0.6, 0);
            }
            case CIRCLE -> {
                double angle = 2 * Math.PI * index / totalFollowers;
                yield new Vector3(
                    -Math.cos(angle) * spacing,
                    Math.sin(angle) * spacing,
                    0
                );
            }
            case CUSTOM -> formationOffsets.getOrDefault(
                robotIds.get(index + 1), 
                new Vector3(-spacing, 0, 0)
            );
        };
    }
    
    /**
     * Sets custom offset for a robot.
     */
    public void setCustomOffset(String robotId, double x, double y) {
        formationOffsets.put(robotId, new Vector3(x, y, 0));
    }
    
    /**
     * Gets number of robots.
     */
    public int getRobotCount() {
        return robotIds.size();
    }
    
    /**
     * Gets leader ID.
     */
    public String getLeaderId() {
        return leaderId;
    }
}
