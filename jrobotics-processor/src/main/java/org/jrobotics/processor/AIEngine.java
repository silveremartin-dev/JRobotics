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

import java.util.Map;

/**
 * Interface for pluggable AI/ML engine implementations.
 * 
 * <p>Allows integration with external AI libraries like DL4J, DeepJavaLibrary, etc.</p>
 * 
 * @author Silvère Martin-Michiellot
 * @author Gemini AI Assistant
 * @version 2.0.0
 * @since 2.0.0
 */
public interface AIEngine {
    
    /**
     * Gets the engine name.
     * 
     * @return the name (e.g., "JRobotics", "DL4J", "DJL")
     */
    String getName();
    
    /**
     * Loads a pre-trained model.
     * 
     * @param modelPath path to the model file
     * @return true if loaded successfully
     */
    boolean loadModel(String modelPath);
    
    /**
     * Performs inference on input data.
     * 
     * @param input the input tensor/data
     * @return the output prediction
     */
    double[] predict(double[] input);
    
    /**
     * Trains on a batch of data.
     * 
     * @param inputs the input batch
     * @param targets the target outputs
     * @return the training loss
     */
    double train(double[][] inputs, double[][] targets);
    
    /**
     * Saves the current model.
     * 
     * @param modelPath path to save to
     * @return true if saved successfully
     */
    boolean saveModel(String modelPath);
    
    /**
     * Gets model metadata.
     * 
     * @return metadata map
     */
    Map<String, Object> getMetadata();
    
    /**
     * Checks if GPU acceleration is available.
     * 
     * @return true if GPU is available
     */
    boolean isGpuAvailable();
    
    /**
     * Releases resources.
     */
    void close();
}
