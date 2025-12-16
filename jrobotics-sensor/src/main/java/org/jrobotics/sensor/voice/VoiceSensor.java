/*
 * JRobotics v2 - World-Class General Purpose Robotics API
 * 
 * Copyright (c) 2025 Silvère Martin-Michiellot <silvere.martin@gmail.com>
 * Co-authored by Gemini AI Assistant
 * 
 * Licensed under the MIT License.
 * See LICENSE file in the project root for full license information.
 */
package org.jrobotics.sensor.voice;

import org.jrobotics.sensor.Sensor;

/**
 * Interface for voice recognition sensors.
 * 
 * <p>
 * Produces recognized text strings from audio input.
 * </p>
 * 
 * @author Silvère Martin-Michiellot
 * @author Gemini AI Assistant
 * @version 2.0.0
 * @since 2.4.0
 */
public interface VoiceSensor extends Sensor<String> {

    /**
     * Gets the current model path.
     * 
     * @return absolute path to the language model
     */
    String getModelPath();

    /**
     * Checks if the recognizer is ready.
     * 
     * @return true if initialized
     */
    boolean isReady();
}
