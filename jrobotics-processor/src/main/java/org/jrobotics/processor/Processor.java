/*
 * JRobotics v2 - World-Class General Purpose Robotics API
 * 
 * Copyright (c) 2025 Silvère Martin-Michiellot <silvere.martin@gmail.com>
 * Co-authored by Gemini AI Assistant
 * 
 * Licensed under the MIT License.
 * See LICENSE file in the project root for full license information.
 */
package org.jrobotics.processor;

import org.jrobotics.core.Component;

import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * Base interface for all processors (AI/reasoning components) in the robotics system.
 * 
 * <p>Processors are components that process sensor data, make decisions,
 * and generate commands for actuators. They form the "brain" of the robot.</p>
 * 
 * @param <I> the input data type
 * @param <O> the output/result type
 * @author Silvère Martin-Michiellot
 * @author Gemini AI Assistant
 * @version 2.0.0
 * @since 2.0.0
 */
public interface Processor<I, O> extends Component {
    
    /**
     * Processes input data and returns a result.
     * 
     * <p>This is a synchronous processing operation.</p>
     * 
     * @param input the input data to process
     * @return the processing result
     * @throws ProcessorException if processing fails
     */
    O process(I input) throws ProcessorException;
    
    /**
     * Processes input data asynchronously.
     * 
     * @param input the input data to process
     * @return a future containing the result
     */
    CompletableFuture<O> processAsync(I input);
    
    /**
     * Gets the processor's configuration parameters.
     * 
     * @return the configuration map
     */
    Map<String, Object> getConfiguration();
    
    /**
     * Sets a configuration parameter.
     * 
     * @param key the parameter key
     * @param value the parameter value
     */
    void setConfiguration(String key, Object value);
    
    /**
     * Resets the processor state.
     * 
     * <p>Clears any internal state, memories, or learned data.</p>
     */
    void reset();
    
    /**
     * Gets the processor's current confidence level.
     * 
     * <p>This represents how confident the processor is in its outputs.</p>
     * 
     * @return the confidence level from 0.0 to 1.0
     */
    double getConfidence();
    
    /**
     * Checks if the processor supports learning.
     * 
     * @return true if the processor can learn from data
     */
    boolean supportsLearning();
    
    /**
     * Trains the processor with labeled data.
     * 
     * @param input the training input
     * @param expectedOutput the expected output
     * @throws UnsupportedOperationException if learning is not supported
     */
    void train(I input, O expectedOutput);
}
