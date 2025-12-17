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
import org.jrobotics.network.swarm.DistributedSharedMemory;
import org.jrobotics.network.swarm.DistributedSharedMemory.DistributedEntry;
import com.fasterxml.jackson.databind.ObjectMapper;

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

    // private static final Logger logger =
    // LoggerFactory.getLogger(SwarmController.class);

    // Weights
    private double separationWeight = 1.5;
    private double alignmentWeight = 1.0;
    private double cohesionWeight = 1.0;
    // private double maxSpeed = 1.0;
    // private double maxForce = 0.1;
    private double perceptionRadius = 2.0;
    private double separationRadius = 1.0;

    private DistributedSharedMemory dsm;
    private String swarmId;
    private final ObjectMapper mapper = new ObjectMapper();

    // Last known state for calculating relative vectors to remote neighbors
    private double[] lastPosition;
    private double[] lastVelocity;

    public SwarmController(String id) {
        super(id, "Swarm-" + id);
        this.swarmId = id; // Assuming id is unique per robot
    }

    /**
     * Enriches local perception with shared swarm data.
     * 
     * @param dsm the distributed memory instance
     */
    public void setDistributedMemory(DistributedSharedMemory dsm) {
        this.dsm = dsm;
    }

    /**
     * Publishes current state to the swarm.
     * 
     * @param position current position [x, y, z]
     * @param velocity current velocity [vx, vy, vz]
     */
    public void broadcastState(double[] position, double[] velocity) {
        this.lastPosition = position;
        this.lastVelocity = velocity;
        if (dsm != null) {
            SwarmState state = new SwarmState(swarmId, position, velocity, System.currentTimeMillis());
            dsm.put("swarm/" + swarmId, state);
        }
    }

    /**
     * Processes neighbor states to calculate steering force.
     * Can merge local neighbors with network neighbors if DSM is available.
     * 
     * @param neighbors list of perceived neighbors (local sensors)
     * @return velocity command [vx, vy]
     */
    @Override
    protected double[] doProcess(java.util.List<Neighbor> neighbors) throws Exception {
        // Merge remote neighbors from DSM into the list if enabled
        if (dsm != null && lastPosition != null && lastVelocity != null) {
            List<DistributedEntry> entries = dsm.findEntries("swarm/");
            for (DistributedEntry entry : entries) {
                // Skip self
                if (entry.owner != null && entry.owner.equals(swarmId)) {
                    continue;
                }

                try {
                    SwarmState remoteState = mapper.convertValue(entry.value, SwarmState.class);

                    // Skip if data is invalid or missing
                    if (remoteState == null || remoteState.position == null || remoteState.velocity == null) {
                        continue;
                    }

                    // Calculate relative position (Remote - Local)
                    double dx = remoteState.position[0] - lastPosition[0];
                    double dy = remoteState.position[1] - lastPosition[1];

                    // Calculate relative velocity (Remote - Local)
                    double vx = remoteState.velocity[0] - lastVelocity[0];
                    double vy = remoteState.velocity[1] - lastVelocity[1];

                    // Add as neighbor (can check distance here or let separate/align/cohesion
                    // filter it)
                    neighbors.add(new Neighbor(dx, dy, vx, vy));

                } catch (IllegalArgumentException e) {
                    // Mapping error, ignore bad entry
                }
            }
        }

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

    // ... existing steering methods ...

    private double[] separate(java.util.List<Neighbor> neighbors) {
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
        }
        return new double[] { steerX, steerY };
    }

    private double[] align(java.util.List<Neighbor> neighbors) {
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
            return new double[] { sumVx, sumVy };
        }
        return new double[] { 0, 0 };
    }

    private double[] cohesion(java.util.List<Neighbor> neighbors) {
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
        public double vx, vy; // Relative velocity

        public Neighbor(double dx, double dy, double vx, double vy) {
            this.dx = dx;
            this.dy = dy;
            this.vx = vx;
            this.vy = vy;
        }
    }

    /**
     * Data object for sharing state over the network.
     */
    public static class SwarmState {
        public String id;
        public double[] position;
        public double[] velocity;
        public long timestamp;

        public SwarmState() {
        }

        public SwarmState(String id, double[] position, double[] velocity, long timestamp) {
            this.id = id;
            this.position = position;
            this.velocity = velocity;
            this.timestamp = timestamp;
        }
    }
}
