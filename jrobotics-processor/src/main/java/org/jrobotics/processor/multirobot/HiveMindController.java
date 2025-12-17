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

import com.fasterxml.jackson.databind.ObjectMapper;
import org.jrobotics.network.swarm.DistributedSharedMemory;
import org.jrobotics.network.swarm.DistributedSharedMemory.DistributedEntry;
import org.jrobotics.processor.AbstractProcessor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Hive Mind - Centralized processor for multi-robot teams.
 *
 * <p>
 * This processor acts as a centralized brain for a swarm of robots.
 * It aggregates state from all registered robots (via DSM), computes
 * global objectives (e.g., task allocation, formation commands), and
 * publishes individual commands back to each robot.
 * </p>
 *
 * <p>
 * <b>Typical Usage:</b>
 * </p>
 * <ol>
 * <li>Instantiate HiveMindController on a central server or leader robot.</li>
 * <li>Connect it to a {@link DistributedSharedMemory} instance.</li>
 * <li>Periodically call {@link #process(Void)} to gather state and issue
 * commands.</li>
 * <li>Individual robots read their commands from DSM.</li>
 * </ol>
 *
 * @author Silvère Martin-Michiellot
 * @author Gemini AI Assistant
 * @version 2.0.0
 * @since 2.6.0
 */
public class HiveMindController extends AbstractProcessor<Void, Map<String, HiveMindController.RobotCommand>> {

    private static final Logger logger = LoggerFactory.getLogger(HiveMindController.class);

    private DistributedSharedMemory dsm;
    private final ObjectMapper mapper = new ObjectMapper();

    // Registered robots and their latest known state
    private final Map<String, SwarmController.SwarmState> robotStates = new HashMap<>();

    // Global goal for the swarm (e.g., a target position for the centroid)
    private double[] globalGoal = null;

    // Task assignments: robotId -> taskType
    private final Map<String, String> taskAssignments = new HashMap<>();

    /**
     * Creates a new HiveMindController.
     *
     * @param id unique identifier for this processor
     */
    public HiveMindController(String id) {
        super(id, "HiveMind-" + id);
    }

    /**
     * Sets the distributed shared memory instance.
     *
     * @param dsm the DSM instance
     */
    public void setDistributedMemory(DistributedSharedMemory dsm) {
        this.dsm = dsm;
    }

    /**
     * Sets a global goal for the swarm.
     *
     * @param goal target position [x, y, z] for the swarm centroid
     */
    public void setGlobalGoal(double[] goal) {
        this.globalGoal = goal;
    }

    /**
     * Assigns a task to a specific robot.
     *
     * @param robotId  the robot's identifier
     * @param taskType a task descriptor (e.g., "EXPLORE", "GUARD", "TRANSPORT")
     */
    public void assignTask(String robotId, String taskType) {
        taskAssignments.put(robotId, taskType);
    }

    /**
     * Processes the swarm state and generates commands for all robots.
     *
     * <p>
     * This method:
     * <ol>
     * <li>Reads all robot states from DSM.</li>
     * <li>Computes the swarm centroid.</li>
     * <li>Generates velocity commands for each robot to move towards the global
     * goal
     * while maintaining relative formation.</li>
     * <li>Publishes commands back to DSM.</li>
     * </ol>
     * </p>
     *
     * @param input not used (pass null)
     * @return a map of robotId to RobotCommand
     * @throws Exception if processing fails
     */
    @Override
    protected Map<String, RobotCommand> doProcess(Void input) throws Exception {
        if (dsm == null) {
            logger.warn("DSM not configured for HiveMind");
            return Map.of();
        }

        // 1. Gather all robot states
        gatherRobotStates();

        if (robotStates.isEmpty()) {
            logger.debug("No robot states available");
            return Map.of();
        }

        // 2. Compute centroid
        double[] centroid = computeCentroid();

        // 3. Compute commands for each robot
        Map<String, RobotCommand> commands = new HashMap<>();

        for (Map.Entry<String, SwarmController.SwarmState> entry : robotStates.entrySet()) {
            String robotId = entry.getKey();
            SwarmController.SwarmState state = entry.getValue();

            RobotCommand command = computeCommand(robotId, state, centroid);
            commands.put(robotId, command);

            // 4. Publish command to DSM
            dsm.put("command/" + robotId, command);
        }

        logger.debug("HiveMind issued commands to {} robots", commands.size());
        return commands;
    }

    private void gatherRobotStates() {
        List<DistributedEntry> entries = dsm.findEntries("swarm/");
        robotStates.clear();

        for (DistributedEntry entry : entries) {
            try {
                SwarmController.SwarmState state = mapper.convertValue(entry.value, SwarmController.SwarmState.class);
                if (state != null && state.id != null) {
                    robotStates.put(state.id, state);
                }
            } catch (IllegalArgumentException e) {
                logger.warn("Failed to parse robot state for key: {}", entry.key);
            }
        }
    }

    private double[] computeCentroid() {
        double sumX = 0, sumY = 0, sumZ = 0;
        int count = 0;

        for (SwarmController.SwarmState state : robotStates.values()) {
            if (state.position != null && state.position.length >= 2) {
                sumX += state.position[0];
                sumY += state.position[1];
                if (state.position.length >= 3) {
                    sumZ += state.position[2];
                }
                count++;
            }
        }

        if (count == 0) {
            return new double[] { 0, 0, 0 };
        }

        return new double[] { sumX / count, sumY / count, sumZ / count };
    }

    private RobotCommand computeCommand(String robotId, SwarmController.SwarmState state, double[] centroid) {
        RobotCommand command = new RobotCommand();
        command.robotId = robotId;
        command.timestamp = System.currentTimeMillis();
        command.taskType = taskAssignments.getOrDefault(robotId, "FOLLOW");

        if (globalGoal != null && state.position != null) {
            // Compute desired velocity towards global goal while maintaining formation
            // offset from centroid
            double[] offset = new double[3];
            for (int i = 0; i < Math.min(state.position.length, 3); i++) {
                offset[i] = state.position[i] - centroid[i];
            }

            // Target position: global goal + offset
            double[] targetPosition = new double[3];
            for (int i = 0; i < 3; i++) {
                targetPosition[i] = globalGoal[i] + offset[i];
            }

            // Proportional control towards target
            double kp = 0.5;
            command.velocityCommand = new double[3];
            for (int i = 0; i < Math.min(state.position.length, 3); i++) {
                command.velocityCommand[i] = kp * (targetPosition[i] - state.position[i]);
            }

            // Limit velocity
            double maxV = 2.0;
            double v = Math.sqrt(command.velocityCommand[0] * command.velocityCommand[0]
                    + command.velocityCommand[1] * command.velocityCommand[1]);
            if (v > maxV) {
                command.velocityCommand[0] = command.velocityCommand[0] / v * maxV;
                command.velocityCommand[1] = command.velocityCommand[1] / v * maxV;
            }

        } else {
            // No global goal, stay in place
            command.velocityCommand = new double[] { 0, 0, 0 };
        }

        return command;
    }

    /**
     * Gets a list of all known robot IDs.
     *
     * @return list of robot identifiers
     */
    public List<String> getKnownRobots() {
        return new ArrayList<>(robotStates.keySet());
    }

    /**
     * Gets the current state of a specific robot.
     *
     * @param robotId the robot identifier
     * @return the robot's state, or null if unknown
     */
    public SwarmController.SwarmState getRobotState(String robotId) {
        return robotStates.get(robotId);
    }

    /**
     * Command data object issued by the HiveMind.
     */
    public static class RobotCommand {
        public String robotId;
        public String taskType;
        public double[] velocityCommand;
        public double[] targetPosition;
        public long timestamp;

        public RobotCommand() {
        }
    }
}
