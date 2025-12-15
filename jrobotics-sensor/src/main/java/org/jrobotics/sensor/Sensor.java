/*
 * JRobotics v2 - World-Class General Purpose Robotics API
 * 
 * Copyright (c) 2025 Silvère Martin-Michiellot <silvere.martin@gmail.com>
 * Co-authored by Gemini AI Assistant
 * 
 * Licensed under the MIT License.
 * See LICENSE file in the project root for full license information.
 */
package org.jrobotics.sensor;

import org.jrobotics.core.Component;

import java.util.Optional;

/**
 * Base interface for all sensors in the robotics system.
 * 
 * <p>Sensors are components that acquire data from the environment.
 * Each sensor produces data of a specific type T.</p>
 * 
 * <p>Example usage:</p>
 * <pre>{@code
 * Sensor<Double> ultrasonic = new UltrasonicSensor("front", gpio);
 * ultrasonic.initialize();
 * ultrasonic.start();
 * 
 * Optional<Double> distance = ultrasonic.read();
 * distance.ifPresent(d -> System.out.println("Distance: " + d + "m"));
 * }</pre>
 * 
 * @param <T> the type of data this sensor produces
 * @author Silvère Martin-Michiellot
 * @author Gemini AI Assistant
 * @version 2.0.0
 * @since 2.0.0
 */
public interface Sensor<T> extends Component {
    
    /**
     * Reads the current sensor value.
     * 
     * <p>This is a synchronous read operation. For continuous data acquisition,
     * consider using {@link #startStreaming(SensorDataListener)}.</p>
     * 
     * @return the current sensor reading, or empty if unavailable
     * @throws SensorException if the read operation fails
     */
    Optional<T> read() throws SensorException;
    
    /**
     * Reads the current sensor value with a timeout.
     * 
     * @param timeoutMs the maximum time to wait in milliseconds
     * @return the sensor reading, or empty if timeout or unavailable
     * @throws SensorException if the read operation fails
     */
    Optional<T> read(long timeoutMs) throws SensorException;
    
    /**
     * Gets the last successfully read value.
     * 
     * <p>This returns the cached value from the last successful read
     * without performing a new read operation.</p>
     * 
     * @return the last sensor reading, or empty if never read
     */
    Optional<T> getLastValue();
    
    /**
     * Gets the timestamp of the last successful read.
     * 
     * @return the timestamp in nanoseconds, or -1 if never read
     */
    long getLastReadTimestamp();
    
    /**
     * Gets the sensor's sample rate in Hz.
     * 
     * @return the sample rate, or -1 if not applicable
     */
    double getSampleRate();
    
    /**
     * Sets the sensor's sample rate in Hz.
     * 
     * @param rateHz the desired sample rate
     * @throws IllegalArgumentException if the rate is not supported
     */
    void setSampleRate(double rateHz);
    
    /**
     * Starts continuous data streaming.
     * 
     * <p>When streaming is active, new sensor readings are pushed to the
     * registered listener as they become available.</p>
     * 
     * @param listener the listener to receive sensor data
     */
    void startStreaming(SensorDataListener<T> listener);
    
    /**
     * Stops continuous data streaming.
     */
    void stopStreaming();
    
    /**
     * Checks if the sensor is currently streaming data.
     * 
     * @return true if streaming
     */
    boolean isStreaming();
    
    /**
     * Calibrates the sensor.
     * 
     * <p>Calibration behavior is sensor-specific.</p>
     * 
     * @throws SensorException if calibration fails
     */
    void calibrate() throws SensorException;
    
    /**
     * Gets the sensor's measurement unit.
     * 
     * @return the unit string (e.g., "m", "°C", "rad/s")
     */
    String getUnit();
    
    /**
     * Gets the sensor's minimum measurable value.
     * 
     * @return the minimum value, or null if unbounded
     */
    T getMinValue();
    
    /**
     * Gets the sensor's maximum measurable value.
     * 
     * @return the maximum value, or null if unbounded
     */
    T getMaxValue();
    
    /**
     * Gets the sensor's resolution/precision.
     * 
     * @return the resolution value
     */
    T getResolution();
}
