/*
 * JRobotics v2 - World-Class General Purpose Robotics API
 * 
 * Copyright (c) 2025 Silvère Martin-Michiellot <silvere.martin@gmail.com>
 * Co-authored by Gemini AI Assistant
 * 
 * Licensed under the MIT License.
 * See LICENSE file in the project root for full license information.
 */
package org.jrobotics.sensor.environmental;

import org.jrobotics.sensor.Sensor;

/**
 * Interface for temperature sensors.
 * 
 * <p>
 * Provides standardized methods for reading temperature in various units.
 * </p>
 * 
 * @author Silvère Martin-Michiellot
 * @author Gemini AI Assistant
 * @version 2.0.0
 * @since 2.5.0
 */
public interface Thermometer extends Sensor<Double> {

    /**
     * Gets the temperature in Celsius.
     * 
     * @return temperature in °C
     */
    double getTemperatureCelsius();

    /**
     * Gets the temperature in Fahrenheit.
     * 
     * @return temperature in °F
     */
    double getTemperatureFahrenheit();
}
