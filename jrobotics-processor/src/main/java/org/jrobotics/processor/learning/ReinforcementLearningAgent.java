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

/**
 * Interface for reinforcement learning agents.
 * 
 * @author Silvère Martin-Michiellot
 * @author Gemini AI Assistant
 * @version 2.0.0
 * @since 2.5.0
 */
public interface ReinforcementLearningAgent {

    /**
     * Gets the action for a given state.
     * 
     * @param state the current state vector
     * @return the action vector
     */
    double[] act(double[] state);

    /**
     * Updates the policy based on the reward.
     * 
     * @param state     the state before action
     * @param action    the action taken
     * @param reward    the reward received
     * @param nextState the state after action
     */
    void learn(double[] state, double[] action, double reward, double[] nextState);

    /**
     * Saves the model.
     * 
     * @param path the file path
     */
    void save(String path);

    /**
     * Loads the model.
     * 
     * @param path the file path
     */
    void load(String path);
}
