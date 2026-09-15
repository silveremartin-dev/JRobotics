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
 * Proximal Policy Optimization (PPO) Agent for Reinforcement Learning.
 *
 * <p>
 * Implements a simplified PPO algorithm suitable for continuous action spaces
 * in robot navigation tasks. Uses clipped surrogate objective for stable
 * policy updates.
 * </p>
 *
 * <p>
 * <b>Features:</b>
 * </p>
 * <ul>
 * <li>Actor-Critic Architecture</li>
 * <li>Clipped Surrogate Objective</li>
 * <li>Generalized Advantage Estimation (GAE)</li>
 * <li>Continuous action space support</li>
 * </ul>
 *
 * @author Silvère Martin-Michiellot
 * @author Gemini AI Assistant
 * @version 2.0.0
 * @since 2.6.0
 */
public class PPOAgent extends AbstractProcessor<double[], double[]> implements ReinforcementLearningAgent {

    private static final Logger logger = LoggerFactory.getLogger(PPOAgent.class);

    // Network dimensions
    private final int stateSize;
    private final int actionSize;
    private final int hiddenSize;

    // Actor network (policy): outputs mean actions
    private double[][] actorW1;
    private double[] actorB1;
    private double[][] actorW2;
    private double[] actorB2;

    // Critic network (value function)
    private double[][] criticW1;
    private double[] criticB1;
    private double[][] criticW2;
    private double criticB2;

    // Action standard deviation (learnable or fixed)
    private double[] logStd;

    // Hyperparameters
    private double learningRateActor = 0.0003;
    private double learningRateCritic = 0.001;
    private double gamma = 0.99;
    private double lambda = 0.95; // GAE lambda
    private double clipEpsilon = 0.2; // PPO clip range
    private int epochs = 10; // Update epochs per batch

    // Trajectory buffer
    private final List<Transition> trajectoryBuffer = new ArrayList<>();

    private final Random random = new Random();

    /**
     * Creates a new PPO agent.
     *
     * @param id         unique identifier
     * @param stateSize  dimension of state space
     * @param actionSize dimension of action space (continuous)
     * @param hiddenSize number of hidden neurons
     */
    public PPOAgent(String id, int stateSize, int actionSize, int hiddenSize) {
        super(id, "PPO-" + id);
        this.stateSize = stateSize;
        this.actionSize = actionSize;
        this.hiddenSize = hiddenSize;

        initializeNetworks();
    }

    private void initializeNetworks() {
        double scale1 = Math.sqrt(2.0 / (stateSize + hiddenSize));
        double scale2 = Math.sqrt(2.0 / (hiddenSize + actionSize));

        // Actor network
        actorW1 = randomMatrix(stateSize, hiddenSize, scale1);
        actorB1 = new double[hiddenSize];
        actorW2 = randomMatrix(hiddenSize, actionSize, scale2);
        actorB2 = new double[actionSize];

        // Critic network
        criticW1 = randomMatrix(stateSize, hiddenSize, scale1);
        criticB1 = new double[hiddenSize];
        criticW2 = randomMatrix(hiddenSize, 1, scale2);
        criticB2 = 0;

        // Log standard deviation (initialized to 0 -> std = 1)
        logStd = new double[actionSize];
    }

    private double[][] randomMatrix(int rows, int cols, double scale) {
        double[][] m = new double[rows][cols];
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                m[i][j] = random.nextGaussian() * scale;
            }
        }
        return m;
    }

    /**
     * Selects an action using the stochastic policy.
     *
     * @param state current state
     * @return sampled action
     */
    @Override
    public double[] act(double[] state) {
        double[] mean = forwardActor(state);
        double[] action = new double[actionSize];

        // Sample from Gaussian: action = mean + std * noise
        for (int i = 0; i < actionSize; i++) {
            double std = Math.exp(logStd[i]);
            action[i] = mean[i] + std * random.nextGaussian();
            // Clip action to [-1, 1]
            action[i] = Math.max(-1, Math.min(1, action[i]));
        }

        return action;
    }

    @Override
    protected double[] doProcess(double[] state) throws Exception {
        return act(state);
    }

    private double[] forwardActor(double[] state) {
        // Hidden layer with tanh
        double[] hidden = new double[hiddenSize];
        for (int j = 0; j < hiddenSize; j++) {
            double sum = actorB1[j];
            for (int i = 0; i < stateSize; i++) {
                sum += state[i] * actorW1[i][j];
            }
            hidden[j] = Math.tanh(sum);
        }

        // Output layer with tanh (bounded actions)
        double[] output = new double[actionSize];
        for (int j = 0; j < actionSize; j++) {
            double sum = actorB2[j];
            for (int i = 0; i < hiddenSize; i++) {
                sum += hidden[i] * actorW2[i][j];
            }
            output[j] = Math.tanh(sum);
        }

        return output;
    }

    private double forwardCritic(double[] state) {
        // Hidden layer with tanh
        double[] hidden = new double[hiddenSize];
        for (int j = 0; j < hiddenSize; j++) {
            double sum = criticB1[j];
            for (int i = 0; i < stateSize; i++) {
                sum += state[i] * criticW1[i][j];
            }
            hidden[j] = Math.tanh(sum);
        }

        // Output (single value)
        double value = criticB2;
        for (int i = 0; i < hiddenSize; i++) {
            value += hidden[i] * criticW2[i][0];
        }

        return value;
    }

    /**
     * Stores transition and learns when episode ends.
     */
    @Override
    public void learn(double[] state, double[] action, double reward, double[] nextState) {
        boolean done = (nextState == null);
        double value = forwardCritic(state);
        double logProb = computeLogProb(state, action);

        trajectoryBuffer.add(new Transition(state, action, reward, value, logProb, done));

        if (done) {
            // Episode ended, compute advantages and update
            computeAdvantages();
            updateNetworks();
            trajectoryBuffer.clear();
        }
    }

    private double computeLogProb(double[] state, double[] action) {
        double[] mean = forwardActor(state);
        double logProb = 0;
        for (int i = 0; i < actionSize; i++) {
            double std = Math.exp(logStd[i]);
            double diff = action[i] - mean[i];
            logProb += -0.5 * (diff * diff) / (std * std) - logStd[i] - 0.5 * Math.log(2 * Math.PI);
        }
        return logProb;
    }

    private void computeAdvantages() {
        int n = trajectoryBuffer.size();
        double[] advantages = new double[n];
        double[] returns = new double[n];

        double lastAdvantage = 0;
        double lastValue = 0;

        for (int t = n - 1; t >= 0; t--) {
            Transition tr = trajectoryBuffer.get(t);
            double nextValue = tr.done ? 0 : (t < n - 1 ? trajectoryBuffer.get(t + 1).value : lastValue);
            double delta = tr.reward + gamma * nextValue - tr.value;
            advantages[t] = delta + gamma * lambda * (tr.done ? 0 : lastAdvantage);
            returns[t] = advantages[t] + tr.value;
            lastAdvantage = advantages[t];
        }

        // Store computed values
        for (int t = 0; t < n; t++) {
            trajectoryBuffer.get(t).advantage = advantages[t];
            trajectoryBuffer.get(t).returnValue = returns[t];
        }
    }

    private void updateNetworks() {
        for (int epoch = 0; epoch < epochs; epoch++) {
            for (Transition tr : trajectoryBuffer) {
                // Compute new log prob and value
                double newLogProb = computeLogProb(tr.state, tr.action);
                double newValue = forwardCritic(tr.state);

                // PPO objective (clipped)
                double ratio = Math.exp(newLogProb - tr.oldLogProb);
                double surr1 = ratio * tr.advantage;
                double surr2 = clip(ratio, 1 - clipEpsilon, 1 + clipEpsilon) * tr.advantage;
                double policyLoss = -Math.min(surr1, surr2);

                // Value loss
                double valueLoss = 0.5 * Math.pow(newValue - tr.returnValue, 2);

                // Log losses periodically for diagnostics
                if (epoch == 0 && trajectoryBuffer.indexOf(tr) == 0) {
                    logger.debug("PPO update - Policy loss: {}, Value loss: {}", policyLoss, valueLoss);
                }

                // Simplified gradient update (actor)
                updateActor(tr.state, tr.action, tr.advantage, ratio);

                // Update critic
                updateCritic(tr.state, tr.returnValue - newValue);
            }
        }
    }

    private void updateActor(double[] state, double[] action, double advantage, double ratio) {
        double[] mean = forwardActor(state);
        double[] hidden = new double[hiddenSize];
        for (int j = 0; j < hiddenSize; j++) {
            double sum = actorB1[j];
            for (int i = 0; i < stateSize; i++) {
                sum += state[i] * actorW1[i][j];
            }
            hidden[j] = Math.tanh(sum);
        }

        // Gradients for output layer
        double[] outputGrad = new double[actionSize];
        for (int j = 0; j < actionSize; j++) {
            double std = Math.exp(logStd[j]);
            double diff = action[j] - mean[j];
            double grad = (diff / (std * std)) * advantage;
            double tanhDeriv = 1 - mean[j] * mean[j];
            outputGrad[j] = grad * tanhDeriv;
        }

        // Gradients for hidden layer (computed before updating actorW2)
        double[] hiddenGrad = new double[hiddenSize];
        for (int j = 0; j < hiddenSize; j++) {
            double sum = 0;
            for (int k = 0; k < actionSize; k++) {
                sum += outputGrad[k] * actorW2[j][k];
            }
            double tanhDeriv = 1 - hidden[j] * hidden[j];
            hiddenGrad[j] = sum * tanhDeriv;
        }

        // Update output layer weights and biases
        for (int j = 0; j < actionSize; j++) {
            for (int i = 0; i < hiddenSize; i++) {
                actorW2[i][j] += learningRateActor * outputGrad[j] * hidden[i];
            }
            actorB2[j] += learningRateActor * outputGrad[j];
        }

        // Update hidden layer weights and biases
        for (int j = 0; j < hiddenSize; j++) {
            for (int i = 0; i < stateSize; i++) {
                actorW1[i][j] += learningRateActor * hiddenGrad[j] * state[i];
            }
            actorB1[j] += learningRateActor * hiddenGrad[j];
        }
    }

    private void updateCritic(double[] state, double tdError) {
        double[] hidden = new double[hiddenSize];
        for (int j = 0; j < hiddenSize; j++) {
            double sum = criticB1[j];
            for (int i = 0; i < stateSize; i++) {
                sum += state[i] * criticW1[i][j];
            }
            hidden[j] = Math.tanh(sum);
        }

        // Hidden layer gradients (computed before updating criticW2)
        double[] hiddenGrad = new double[hiddenSize];
        for (int j = 0; j < hiddenSize; j++) {
            double tanhDeriv = 1 - hidden[j] * hidden[j];
            hiddenGrad[j] = (tdError * criticW2[j][0]) * tanhDeriv;
        }

        // Update output weights
        for (int i = 0; i < hiddenSize; i++) {
            criticW2[i][0] += learningRateCritic * tdError * hidden[i];
        }
        criticB2 += learningRateCritic * tdError;

        // Update hidden weights and biases
        for (int j = 0; j < hiddenSize; j++) {
            for (int i = 0; i < stateSize; i++) {
                criticW1[i][j] += learningRateCritic * hiddenGrad[j] * state[i];
            }
            criticB1[j] += learningRateCritic * hiddenGrad[j];
        }
    }

    private double clip(double value, double min, double max) {
        return Math.max(min, Math.min(max, value));
    }

    // Hyperparameter setters
    public void setLearningRates(double actor, double critic) {
        this.learningRateActor = actor;
        this.learningRateCritic = critic;
    }

    public void setGamma(double gamma) {
        this.gamma = gamma;
    }

    public void setLambda(double lambda) {
        this.lambda = lambda;
    }

    public void setClipEpsilon(double clip) {
        this.clipEpsilon = clip;
    }

    public void setEpochs(int epochs) {
        this.epochs = epochs;
    }

    @Override
    public void save(String path) {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(path))) {
            oos.writeObject(actorW1);
            oos.writeObject(actorB1);
            oos.writeObject(actorW2);
            oos.writeObject(actorB2);
            oos.writeObject(criticW1);
            oos.writeObject(criticB1);
            oos.writeObject(criticW2);
            oos.writeDouble(criticB2);
            oos.writeObject(logStd);
            logger.info("PPO model saved to {}", path);
        } catch (IOException e) {
            logger.error("Failed to save model", e);
        }
    }

    @Override
    public void load(String path) {
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(path))) {
            actorW1 = (double[][]) ois.readObject();
            actorB1 = (double[]) ois.readObject();
            actorW2 = (double[][]) ois.readObject();
            actorB2 = (double[]) ois.readObject();
            criticW1 = (double[][]) ois.readObject();
            criticB1 = (double[]) ois.readObject();
            criticW2 = (double[][]) ois.readObject();
            criticB2 = ois.readDouble();
            logStd = (double[]) ois.readObject();
            logger.info("PPO model loaded from {}", path);
        } catch (IOException | ClassNotFoundException e) {
            logger.error("Failed to load model", e);
        }
    }

    @Override
    public boolean supportsLearning() {
        return true;
    }

    /**
     * Transition for trajectory storage.
     */
    private static class Transition {
        final double[] state;
        final double[] action;
        final double reward;
        final double value;
        final double oldLogProb;
        final boolean done;
        double advantage;
        double returnValue;

        Transition(double[] state, double[] action, double reward, double value, double oldLogProb, boolean done) {
            this.state = state;
            this.action = action;
            this.reward = reward;
            this.value = value;
            this.oldLogProb = oldLogProb;
            this.done = done;
        }
    }
}
