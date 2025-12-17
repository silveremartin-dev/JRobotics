/*
 * JRobotics v2 - World-Class General Purpose Robotics API
 * 
 * Copyright (c) 2025 Silvère Martin-Michiellot <silvere.martin@gmail.com>
 * Co-authored by Gemini AI Assistant
 * 
 * Licensed under the MIT License.
 * See LICENSE file in the project root for full license information.
 */
package org.jrobotics.processor.ai;

import org.jrobotics.processor.AbstractProcessor;
import org.jrobotics.processor.learning.ReinforcementLearningAgent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Deep Q-Network (DQN) Agent for Reinforcement Learning.
 *
 * <p>
 * Implements a simplified DQN algorithm suitable for discrete action spaces
 * in robot navigation tasks. Uses experience replay and a target network
 * for stable learning.
 * </p>
 *
 * <p>
 * <b>Features:</b>
 * </p>
 * <ul>
 * <li>Experience Replay Buffer</li>
 * <li>Epsilon-Greedy Exploration</li>
 * <li>Target Network (soft updates)</li>
 * <li>Configurable neural network architecture</li>
 * </ul>
 *
 * @author Silvère Martin-Michiellot
 * @author Gemini AI Assistant
 * @version 2.0.0
 * @since 2.6.0
 */
public class DQNAgent extends AbstractProcessor<double[], Integer> implements ReinforcementLearningAgent {

    private static final Logger logger = LoggerFactory.getLogger(DQNAgent.class);

    // Network dimensions
    private final int stateSize;
    private final int actionSize;
    private final int hiddenSize;

    // Q-network weights (simplified: input -> hidden -> output)
    private double[][] weightsInputHidden;
    private double[] biasHidden;
    private double[][] weightsHiddenOutput;
    private double[] biasOutput;

    // Target network weights
    private double[][] targetWeightsInputHidden;
    private double[] targetBiasHidden;
    private double[][] targetWeightsHiddenOutput;
    private double[] targetBiasOutput;

    // Hyperparameters
    private double learningRate = 0.001;
    private double gamma = 0.99; // Discount factor
    private double epsilon = 1.0; // Exploration rate
    private double epsilonMin = 0.01;
    private double epsilonDecay = 0.995;
    private double tau = 0.01; // Soft update parameter
    private int batchSize = 32;

    // Experience replay
    private final List<Experience> replayBuffer = new ArrayList<>();
    private int bufferCapacity = 10000;

    private final Random random = new Random();

    /**
     * Creates a new DQN agent.
     *
     * @param id         unique identifier
     * @param stateSize  dimension of state space
     * @param actionSize number of discrete actions
     * @param hiddenSize number of hidden neurons
     */
    public DQNAgent(String id, int stateSize, int actionSize, int hiddenSize) {
        super(id, "DQN-" + id);
        this.stateSize = stateSize;
        this.actionSize = actionSize;
        this.hiddenSize = hiddenSize;

        initializeWeights();
    }

    private void initializeWeights() {
        // Xavier initialization
        double scale1 = Math.sqrt(2.0 / (stateSize + hiddenSize));
        double scale2 = Math.sqrt(2.0 / (hiddenSize + actionSize));

        weightsInputHidden = new double[stateSize][hiddenSize];
        biasHidden = new double[hiddenSize];
        weightsHiddenOutput = new double[hiddenSize][actionSize];
        biasOutput = new double[actionSize];

        for (int i = 0; i < stateSize; i++) {
            for (int j = 0; j < hiddenSize; j++) {
                weightsInputHidden[i][j] = (random.nextGaussian()) * scale1;
            }
        }
        for (int i = 0; i < hiddenSize; i++) {
            for (int j = 0; j < actionSize; j++) {
                weightsHiddenOutput[i][j] = (random.nextGaussian()) * scale2;
            }
        }

        // Copy to target network
        copyToTargetNetwork();
    }

    private void copyToTargetNetwork() {
        targetWeightsInputHidden = deepCopy(weightsInputHidden);
        targetBiasHidden = biasHidden.clone();
        targetWeightsHiddenOutput = deepCopy(weightsHiddenOutput);
        targetBiasOutput = biasOutput.clone();
    }

    private double[][] deepCopy(double[][] arr) {
        double[][] copy = new double[arr.length][];
        for (int i = 0; i < arr.length; i++) {
            copy[i] = arr[i].clone();
        }
        return copy;
    }

    /**
     * Selects an action using epsilon-greedy policy.
     *
     * @param state current state
     * @return action index
     */
    @Override
    public double[] act(double[] state) {
        int action;
        if (random.nextDouble() < epsilon) {
            // Explore
            action = random.nextInt(actionSize);
        } else {
            // Exploit
            double[] qValues = forward(state, false);
            action = argmax(qValues);
        }
        return new double[] { action };
    }

    /**
     * Process wrapper for compatibility with AbstractProcessor.
     */
    @Override
    protected Integer doProcess(double[] state) throws Exception {
        return (int) act(state)[0];
    }

    /**
     * Forward pass through the Q-network.
     */
    private double[] forward(double[] state, boolean useTarget) {
        double[][] w1 = useTarget ? targetWeightsInputHidden : weightsInputHidden;
        double[] b1 = useTarget ? targetBiasHidden : biasHidden;
        double[][] w2 = useTarget ? targetWeightsHiddenOutput : weightsHiddenOutput;
        double[] b2 = useTarget ? targetBiasOutput : biasOutput;

        // Hidden layer with ReLU
        double[] hidden = new double[hiddenSize];
        for (int j = 0; j < hiddenSize; j++) {
            double sum = b1[j];
            for (int i = 0; i < stateSize; i++) {
                sum += state[i] * w1[i][j];
            }
            hidden[j] = Math.max(0, sum); // ReLU
        }

        // Output layer (linear for Q-values)
        double[] output = new double[actionSize];
        for (int j = 0; j < actionSize; j++) {
            double sum = b2[j];
            for (int i = 0; i < hiddenSize; i++) {
                sum += hidden[i] * w2[i][j];
            }
            output[j] = sum;
        }

        return output;
    }

    /**
     * Stores experience and learns from replay buffer.
     */
    @Override
    public void learn(double[] state, double[] action, double reward, double[] nextState) {
        int actionIndex = (int) action[0];
        boolean done = (nextState == null);

        // Store experience
        replayBuffer.add(new Experience(state, actionIndex, reward, nextState, done));
        if (replayBuffer.size() > bufferCapacity) {
            replayBuffer.remove(0);
        }

        // Learn from batch
        if (replayBuffer.size() >= batchSize) {
            trainBatch();
        }

        // Decay epsilon
        if (epsilon > epsilonMin) {
            epsilon *= epsilonDecay;
        }
    }

    private void trainBatch() {
        // Sample random batch
        List<Experience> batch = new ArrayList<>();
        for (int i = 0; i < batchSize; i++) {
            batch.add(replayBuffer.get(random.nextInt(replayBuffer.size())));
        }

        for (Experience exp : batch) {
            double[] qValues = forward(exp.state, false);
            double target;

            if (exp.done || exp.nextState == null) {
                target = exp.reward;
            } else {
                double[] nextQValues = forward(exp.nextState, true);
                target = exp.reward + gamma * max(nextQValues);
            }

            // Compute TD error
            double tdError = target - qValues[exp.action];

            // Backpropagation (simplified gradient descent)
            updateWeights(exp.state, exp.action, tdError);
        }

        // Soft update target network
        softUpdateTarget();
    }

    private void updateWeights(double[] state, int action, double tdError) {
        // Forward pass to get hidden activations
        double[] hidden = new double[hiddenSize];
        for (int j = 0; j < hiddenSize; j++) {
            double sum = biasHidden[j];
            for (int i = 0; i < stateSize; i++) {
                sum += state[i] * weightsInputHidden[i][j];
            }
            hidden[j] = Math.max(0, sum);
        }

        // Update output layer weights
        for (int i = 0; i < hiddenSize; i++) {
            weightsHiddenOutput[i][action] += learningRate * tdError * hidden[i];
        }
        biasOutput[action] += learningRate * tdError;

        // Update hidden layer weights (simplified, only for the chosen action path)
        for (int j = 0; j < hiddenSize; j++) {
            if (hidden[j] > 0) { // ReLU derivative
                double grad = tdError * weightsHiddenOutput[j][action];
                for (int i = 0; i < stateSize; i++) {
                    weightsInputHidden[i][j] += learningRate * grad * state[i];
                }
                biasHidden[j] += learningRate * grad;
            }
        }
    }

    private void softUpdateTarget() {
        for (int i = 0; i < stateSize; i++) {
            for (int j = 0; j < hiddenSize; j++) {
                targetWeightsInputHidden[i][j] = tau * weightsInputHidden[i][j]
                        + (1 - tau) * targetWeightsInputHidden[i][j];
            }
        }
        for (int i = 0; i < hiddenSize; i++) {
            targetBiasHidden[i] = tau * biasHidden[i] + (1 - tau) * targetBiasHidden[i];
            for (int j = 0; j < actionSize; j++) {
                targetWeightsHiddenOutput[i][j] = tau * weightsHiddenOutput[i][j]
                        + (1 - tau) * targetWeightsHiddenOutput[i][j];
            }
        }
        for (int j = 0; j < actionSize; j++) {
            targetBiasOutput[j] = tau * biasOutput[j] + (1 - tau) * targetBiasOutput[j];
        }
    }

    private int argmax(double[] arr) {
        int maxIdx = 0;
        for (int i = 1; i < arr.length; i++) {
            if (arr[i] > arr[maxIdx])
                maxIdx = i;
        }
        return maxIdx;
    }

    private double max(double[] arr) {
        double maxVal = arr[0];
        for (int i = 1; i < arr.length; i++) {
            if (arr[i] > maxVal)
                maxVal = arr[i];
        }
        return maxVal;
    }

    // Hyperparameter setters
    public void setLearningRate(double lr) {
        this.learningRate = lr;
    }

    public void setGamma(double gamma) {
        this.gamma = gamma;
    }

    public void setEpsilon(double epsilon) {
        this.epsilon = epsilon;
    }

    public void setEpsilonDecay(double decay) {
        this.epsilonDecay = decay;
    }

    public void setBufferCapacity(int capacity) {
        this.bufferCapacity = capacity;
    }

    public double getEpsilon() {
        return epsilon;
    }

    public int getBufferSize() {
        return replayBuffer.size();
    }

    @Override
    public void save(String path) {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(path))) {
            oos.writeObject(weightsInputHidden);
            oos.writeObject(biasHidden);
            oos.writeObject(weightsHiddenOutput);
            oos.writeObject(biasOutput);
            oos.writeDouble(epsilon);
            logger.info("DQN model saved to {}", path);
        } catch (IOException e) {
            logger.error("Failed to save model", e);
        }
    }

    @Override
    public void load(String path) {
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(path))) {
            weightsInputHidden = (double[][]) ois.readObject();
            biasHidden = (double[]) ois.readObject();
            weightsHiddenOutput = (double[][]) ois.readObject();
            biasOutput = (double[]) ois.readObject();
            epsilon = ois.readDouble();
            copyToTargetNetwork();
            logger.info("DQN model loaded from {}", path);
        } catch (IOException | ClassNotFoundException e) {
            logger.error("Failed to load model", e);
        }
    }

    @Override
    public boolean supportsLearning() {
        return true;
    }

    /**
     * Experience tuple for replay buffer.
     */
    private static class Experience {
        final double[] state;
        final int action;
        final double reward;
        final double[] nextState;
        final boolean done;

        Experience(double[] state, int action, double reward, double[] nextState, boolean done) {
            this.state = state;
            this.action = action;
            this.reward = reward;
            this.nextState = nextState;
            this.done = done;
        }
    }
}
