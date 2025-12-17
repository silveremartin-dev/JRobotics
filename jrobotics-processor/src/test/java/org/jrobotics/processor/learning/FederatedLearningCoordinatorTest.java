package org.jrobotics.processor.learning;

import org.jrobotics.network.swarm.DistributedSharedMemory;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class FederatedLearningCoordinatorTest {

    /**
     * Mock DSM for testing.
     */
    static class MockDSM extends DistributedSharedMemory {
        private final List<DistributedEntry> cannedEntries = new ArrayList<>();
        private final Map<String, Object> publishedData = new HashMap<>();

        public MockDSM() {
            super("mock-fl", 0, "0.0.0.0");
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
            // Also add to entries for findEntries to work
            cannedEntries.removeIf(e -> e.key.equals(key));
            cannedEntries.add(new DistributedEntry(key, value, "mock", System.currentTimeMillis()));
        }

        @Override
        public Object get(String key) {
            return publishedData.get(key);
        }

        @Override
        public List<DistributedEntry> findEntries(String prefix) {
            List<DistributedEntry> result = new ArrayList<>();
            for (DistributedEntry e : cannedEntries) {
                if (e.key.startsWith(prefix)) {
                    result.add(e);
                }
            }
            return result;
        }

        public void addEntry(String key, Object value, String owner) {
            cannedEntries.add(new DistributedEntry(key, value, owner, System.currentTimeMillis()));
        }

        public Map<String, Object> getPublishedData() {
            return publishedData;
        }
    }

    @Test
    void testGradientAggregation() {
        int modelSize = 4;
        FederatedLearningCoordinator coordinator = new FederatedLearningCoordinator("coord-1", modelSize);
        MockDSM mockDsm = new MockDSM();
        coordinator.setDistributedMemory(mockDsm);
        coordinator.setLearningRate(1.0); // For easier testing
        coordinator.setMinParticipants(2);

        // Set initial model to zeros
        coordinator.setGlobalModel(new double[] { 0, 0, 0, 0 });

        // Simulate two robots publishing gradients
        // Robot 1: gradients = [1, 2, 3, 4]
        FederatedLearningCoordinator.GradientUpdate update1 = new FederatedLearningCoordinator.GradientUpdate();
        update1.robotId = "robot-1";
        update1.gradients = new double[] { 1.0, 2.0, 3.0, 4.0 };
        update1.timestamp = System.currentTimeMillis();
        mockDsm.addEntry("gradient/robot-1", update1, "robot-1");

        // Robot 2: gradients = [3, 4, 5, 6]
        FederatedLearningCoordinator.GradientUpdate update2 = new FederatedLearningCoordinator.GradientUpdate();
        update2.robotId = "robot-2";
        update2.gradients = new double[] { 3.0, 4.0, 5.0, 6.0 };
        update2.timestamp = System.currentTimeMillis();
        mockDsm.addEntry("gradient/robot-2", update2, "robot-2");

        // Aggregate
        boolean success = coordinator.aggregateAndUpdate();

        assertTrue(success, "Aggregation should succeed with 2 participants");

        // Check global model
        // Average gradient = [(1+3)/2, (2+4)/2, (3+5)/2, (4+6)/2] = [2, 3, 4, 5]
        // New model = [0, 0, 0, 0] - 1.0 * [2, 3, 4, 5] = [-2, -3, -4, -5]
        double[] model = coordinator.getGlobalModel();
        assertArrayEquals(new double[] { -2.0, -3.0, -4.0, -5.0 }, model, 0.001);

        // Verify model was published
        assertTrue(mockDsm.getPublishedData().containsKey("model/global"));
    }

    @Test
    void testNotEnoughParticipants() {
        FederatedLearningCoordinator coordinator = new FederatedLearningCoordinator("coord-2", 4);
        MockDSM mockDsm = new MockDSM();
        coordinator.setDistributedMemory(mockDsm);
        coordinator.setMinParticipants(3);

        // Only one robot
        FederatedLearningCoordinator.GradientUpdate update = new FederatedLearningCoordinator.GradientUpdate();
        update.robotId = "robot-1";
        update.gradients = new double[] { 1.0, 2.0, 3.0, 4.0 };
        mockDsm.addEntry("gradient/robot-1", update, "robot-1");

        boolean success = coordinator.aggregateAndUpdate();

        assertFalse(success, "Aggregation should fail with insufficient participants");
    }

    @Test
    void testPublishAndFetchModel() {
        FederatedLearningCoordinator coordinator = new FederatedLearningCoordinator("coord-3", 3);
        MockDSM mockDsm = new MockDSM();
        coordinator.setDistributedMemory(mockDsm);

        double[] model = new double[] { 0.5, 1.5, 2.5 };
        coordinator.setGlobalModel(model);
        coordinator.publishGlobalModel();

        // Fetch it back
        double[] fetched = coordinator.fetchGlobalModel();
        assertNotNull(fetched);
        assertArrayEquals(model, fetched, 0.001);
    }
}
