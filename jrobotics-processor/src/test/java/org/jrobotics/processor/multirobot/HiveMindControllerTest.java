package org.jrobotics.processor.multirobot;

import org.jrobotics.network.swarm.DistributedSharedMemory;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class HiveMindControllerTest {

    /**
     * Mock DSM for testing without network.
     */
    static class MockDSM extends DistributedSharedMemory {
        private final List<DistributedEntry> cannedEntries = new ArrayList<>();
        private final java.util.Map<String, Object> publishedData = new java.util.HashMap<>();

        public MockDSM() {
            super("mock-hive", 0, "0.0.0.0");
        }

        @Override
        public void initialize() {
        }

        @Override
        public void start() {
        }

        @Override
        public void stop() {
        }

        @Override
        public void put(String key, Object value) {
            publishedData.put(key, value);
        }

        @Override
        public List<DistributedEntry> findEntries(String prefix) {
            return cannedEntries;
        }

        public void addEntry(String key, Object value, String owner) {
            cannedEntries.add(new DistributedEntry(key, value, owner, System.currentTimeMillis()));
        }

        public java.util.Map<String, Object> getPublishedData() {
            return publishedData;
        }
    }

    @Test
    void testHiveMindProcessesSwarmAndIssuesCommands() throws Exception {
        HiveMindController hiveMind = new HiveMindController("hive-1");
        MockDSM mockDsm = new MockDSM();
        hiveMind.setDistributedMemory(mockDsm);

        hiveMind.initialize();
        hiveMind.start();

        // Set a global goal
        hiveMind.setGlobalGoal(new double[] { 10.0, 10.0, 0.0 });

        // Simulate two robots
        SwarmController.SwarmState robot1 = new SwarmController.SwarmState(
                "robot-1",
                new double[] { 0.0, 0.0, 0.0 },
                new double[] { 0.0, 0.0, 0.0 },
                System.currentTimeMillis());
        SwarmController.SwarmState robot2 = new SwarmController.SwarmState(
                "robot-2",
                new double[] { 2.0, 0.0, 0.0 },
                new double[] { 0.0, 0.0, 0.0 },
                System.currentTimeMillis());

        mockDsm.addEntry("swarm/robot-1", robot1, "robot-1");
        mockDsm.addEntry("swarm/robot-2", robot2, "robot-2");

        // Process
        Map<String, HiveMindController.RobotCommand> commands = hiveMind.process(null);

        // Assertions
        assertNotNull(commands);
        assertEquals(2, commands.size());
        assertTrue(commands.containsKey("robot-1"));
        assertTrue(commands.containsKey("robot-2"));

        // Check commands were published to DSM
        assertTrue(mockDsm.getPublishedData().containsKey("command/robot-1"));
        assertTrue(mockDsm.getPublishedData().containsKey("command/robot-2"));

        // Check velocity commands point towards the goal (positive X and Y)
        HiveMindController.RobotCommand cmd1 = commands.get("robot-1");
        assertNotNull(cmd1.velocityCommand);
        assertTrue(cmd1.velocityCommand[0] > 0, "Robot 1 should move towards positive X");
        assertTrue(cmd1.velocityCommand[1] > 0, "Robot 1 should move towards positive Y");

        HiveMindController.RobotCommand cmd2 = commands.get("robot-2");
        assertNotNull(cmd2.velocityCommand);
        assertTrue(cmd2.velocityCommand[0] > 0, "Robot 2 should move towards positive X");
        assertTrue(cmd2.velocityCommand[1] > 0, "Robot 2 should move towards positive Y");
    }

    @Test
    void testHiveMindWithNoRobots() throws Exception {
        HiveMindController hiveMind = new HiveMindController("hive-empty");
        MockDSM mockDsm = new MockDSM();
        hiveMind.setDistributedMemory(mockDsm);

        hiveMind.initialize();
        hiveMind.start();

        Map<String, HiveMindController.RobotCommand> commands = hiveMind.process(null);

        assertNotNull(commands);
        assertTrue(commands.isEmpty());
    }
}
