/*
 * JRobotics v2 - World-Class General Purpose Robotics API
 * 
 * Copyright (c) 2025 Silvère Martin-Michiellot <silvere.martin@gmail.com>
 * Co-authored by Gemini AI Assistant
 * 
 * Licensed under the MIT License.
 * See LICENSE file in the project root for full license information.
 */
package org.jrobotics.processor.learning;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.jrobotics.network.swarm.DistributedSharedMemory;
import org.jrobotics.network.swarm.DistributedSharedMemory.DistributedEntry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * Federated Learning Coordinator for Swarm Robotics.
 *
 * <p>
 * Implements a simple FedAvg (Federated Averaging) algorithm where:
 * <ol>
 * <li>Each robot trains a local model and publishes its gradients/weights to
 * DSM.</li>
 * <li>The coordinator aggregates gradients from all participating robots.</li>
 * <li>The averaged model is published back to DSM for all robots to
 * download.</li>
 * </ol>
 * </p>
 *
 * <p>
 * This is a simplified implementation suitable for demonstration. In
 * production,
 * you would integrate with a proper ML framework (DJL, DL4J, etc.).
 * </p>
 *
 * @author Silvère Martin-Michiellot
 * @author Gemini AI Assistant
 * @version 2.0.0
 * @since 2.6.0
 */
public class FederatedLearningCoordinator {

    private static final Logger logger = LoggerFactory.getLogger(FederatedLearningCoordinator.class);

    private static final String GRADIENT_PREFIX = "gradient/";
    private static final String MODEL_KEY = "model/global";

    private final String coordinatorId;
    private DistributedSharedMemory dsm;
    private final ObjectMapper mapper = new ObjectMapper();

    // Current global model (represented as a simple weight vector)
    private double[] globalModel;
    private int modelSize;

    // Learning configuration
    private double learningRate = 0.01;
    private int minParticipants = 2;

    /**
     * Creates a new FederatedLearningCoordinator.
     *
     * @param coordinatorId unique identifier
     * @param modelSize     size of the model weight vector
     */
    public FederatedLearningCoordinator(String coordinatorId, int modelSize) {
        this.coordinatorId = coordinatorId;
        this.modelSize = modelSize;
        this.globalModel = new double[modelSize];
        // Initialize with small random weights
        for (int i = 0; i < modelSize; i++) {
            globalModel[i] = (Math.random() - 0.5) * 0.1;
        }
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
     * Sets the learning rate for gradient aggregation.
     *
     * @param learningRate the learning rate
     */
    public void setLearningRate(double learningRate) {
        this.learningRate = learningRate;
    }

    /**
     * Sets the minimum number of participants required to aggregate.
     *
     * @param minParticipants minimum participants
     */
    public void setMinParticipants(int minParticipants) {
        this.minParticipants = minParticipants;
    }

    /**
     * Gets the current global model weights.
     *
     * @return copy of the global model
     */
    public double[] getGlobalModel() {
        return globalModel.clone();
    }

    /**
     * Sets the global model weights.
     *
     * @param model the model weights
     */
    public void setGlobalModel(double[] model) {
        if (model == null || model.length != modelSize) {
            throw new IllegalArgumentException("Model size mismatch");
        }
        this.globalModel = model.clone();
    }

    /**
     * Aggregates gradients from all participants and updates the global model.
     *
     * <p>
     * This implements the FedAvg algorithm:
     * 
     * <pre>
     * global_model += learning_rate * mean(all_gradients)
     * </pre>
     * </p>
     *
     * @return true if aggregation was performed, false if not enough participants
     */
    public boolean aggregateAndUpdate() {
        if (dsm == null) {
            logger.warn("DSM not configured");
            return false;
        }

        // Gather all gradient updates
        List<GradientUpdate> updates = gatherGradients();

        if (updates.size() < minParticipants) {
            logger.debug("Not enough participants: {} < {}", updates.size(), minParticipants);
            return false;
        }

        // Compute average gradient
        double[] avgGradient = new double[modelSize];
        for (GradientUpdate update : updates) {
            for (int i = 0; i < modelSize && i < update.gradients.length; i++) {
                avgGradient[i] += update.gradients[i];
            }
        }
        for (int i = 0; i < modelSize; i++) {
            avgGradient[i] /= updates.size();
        }

        // Update global model
        for (int i = 0; i < modelSize; i++) {
            globalModel[i] -= learningRate * avgGradient[i]; // Gradient descent
        }

        // Publish updated model
        publishGlobalModel();

        logger.info("Aggregated gradients from {} participants", updates.size());
        return true;
    }

    /**
     * Publishes a gradient update from a local robot.
     *
     * <p>
     * Call this from each robot after local training.
     * </p>
     *
     * @param robotId   the robot's identifier
     * @param gradients the computed gradients
     */
    public void publishGradient(String robotId, double[] gradients) {
        if (dsm == null) {
            logger.warn("DSM not configured");
            return;
        }

        GradientUpdate update = new GradientUpdate();
        update.robotId = robotId;
        update.gradients = gradients;
        update.timestamp = System.currentTimeMillis();

        dsm.put(GRADIENT_PREFIX + robotId, update);
        logger.debug("Published gradient from {}", robotId);
    }

    /**
     * Publishes the global model to DSM.
     */
    public void publishGlobalModel() {
        if (dsm == null)
            return;

        ModelUpdate model = new ModelUpdate();
        model.coordinatorId = coordinatorId;
        model.weights = globalModel.clone();
        model.version = System.currentTimeMillis();

        dsm.put(MODEL_KEY, model);
        logger.debug("Published global model v{}", model.version);
    }

    /**
     * Fetches the latest global model from DSM.
     *
     * @return the model weights, or null if not available
     */
    public double[] fetchGlobalModel() {
        if (dsm == null)
            return null;

        Object value = dsm.get(MODEL_KEY);
        if (value == null)
            return null;

        try {
            ModelUpdate model = mapper.convertValue(value, ModelUpdate.class);
            return model != null && model.weights != null ? model.weights.clone() : null;
        } catch (IllegalArgumentException e) {
            logger.warn("Failed to parse global model");
            return null;
        }
    }

    private List<GradientUpdate> gatherGradients() {
        List<GradientUpdate> updates = new ArrayList<>();

        List<DistributedEntry> entries = dsm.findEntries(GRADIENT_PREFIX);
        for (DistributedEntry entry : entries) {
            try {
                GradientUpdate update = mapper.convertValue(entry.value, GradientUpdate.class);
                if (update != null && update.gradients != null) {
                    updates.add(update);
                }
            } catch (IllegalArgumentException e) {
                logger.warn("Failed to parse gradient from {}", entry.key);
            }
        }

        return updates;
    }

    /**
     * Clears all gradient entries from DSM (for a fresh round).
     */
    public void clearGradients() {
        // Note: DSM doesn't have a delete operation in the current implementation.
        // In a real system, you'd add a delete method or use TTL on entries.
        logger.debug("Gradient clearing not implemented - entries will be overwritten");
    }

    /**
     * Data object for gradient updates.
     */
    public static class GradientUpdate {
        public String robotId;
        public double[] gradients;
        public long timestamp;

        public GradientUpdate() {
        }
    }

    /**
     * Data object for model updates.
     */
    public static class ModelUpdate {
        public String coordinatorId;
        public double[] weights;
        public long version;

        public ModelUpdate() {
        }
    }
}
