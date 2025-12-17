package org.jrobotics.processor.multirobot;

import org.jrobotics.network.swarm.DistributedSharedMemory;
import org.junit.jupiter.api.Test;
import java.util.ArrayList;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class SwarmIntegrationTest {

    static class MockDSM extends DistributedSharedMemory {
        private final List<DistributedEntry> cannedEntries = new ArrayList<>();

        public MockDSM() {
            super("mock-node", 0, "0.0.0.0");
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
            // No-op for broadcast in this test
        }

        @Override
        public List<DistributedEntry> findEntries(String prefix) {
            return cannedEntries;
        }

        public void addEntry(String key, Object value, String owner) {
            cannedEntries.add(new DistributedEntry(key, value, owner, System.currentTimeMillis()));
        }
    }

    @Test
    void testSwarmInteraction() throws Exception {
        SwarmController localRobot = new SwarmController("robot-1");
        MockDSM mockDsm = new MockDSM();
        localRobot.setDistributedMemory(mockDsm);

        // Initialize and start the processor
        localRobot.initialize();
        localRobot.start();

        // 1. Initial State: Local robot is at (0,0) with velocity (0,0)
        localRobot.broadcastState(new double[] { 0.0, 0.0, 0.0 }, new double[] { 0.0, 0.0, 0.0 });

        // 2. Add a remote robot at (2,0) with velocity (0,0).
        // Distance is 2.0. Perception radius is default 2.0.
        // It should just be within range.
        // If it's within range, Cohesion should pull towards it.
        // SwarmState expects a Map if we simulate what Jackson does, OR we can pass
        // SwarmState object
        // if the mapper.convertValue works for POJOs too (it does).

        SwarmController.SwarmState remoteState = new SwarmController.SwarmState(
                "robot-2",
                new double[] { 1.5, 0.0, 0.0 }, // x=1.5, y=0. Closer than 2.0
                new double[] { 0.0, 0.0, 0.0 },
                System.currentTimeMillis());

        mockDsm.addEntry("swarm/robot-2", remoteState, "robot-2");

        // 3. Process with mutable neighbor list
        double[] output = localRobot.process(new ArrayList<>());

        // 4. Assertions
        // Expecting some force towards positive X (Cohesion).
        assertNotNull(output);
        assertTrue(output[0] > 0, "Should have cohesion force towards neighbor at x=1.5. Output: " + output[0]);
        // Y should be 0
        assertEquals(0.0, output[1], 0.001);
    }
}
