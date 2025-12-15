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

import org.jrobotics.core.AbstractComponent;
import org.jrobotics.core.ComponentType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Abstract base implementation of the {@link Sensor} interface.
 * 
 * <p>Provides common functionality for all sensors including lifecycle
 * management, data caching, and streaming support.</p>
 * 
 * @param <T> the type of data this sensor produces
 * @author Silvère Martin-Michiellot
 * @author Gemini AI Assistant
 * @version 2.0.0
 * @since 2.0.0
 */
public abstract class AbstractSensor<T> extends AbstractComponent implements Sensor<T> {
    
    private static final Logger logger = LoggerFactory.getLogger(AbstractSensor.class);
    
    private final AtomicReference<T> lastValue = new AtomicReference<>();
    private volatile long lastReadTimestamp = -1;
    private volatile double sampleRate = -1;
    private final AtomicBoolean streaming = new AtomicBoolean(false);
    private volatile SensorDataListener<T> streamListener;
    
    private final String unit;
    private final T minValue;
    private final T maxValue;
    private final T resolution;
    
    /**
     * Constructs a new sensor with basic parameters.
     * 
     * @param id the unique identifier
     * @param name the human-readable name
     * @param unit the measurement unit
     */
    protected AbstractSensor(String id, String name, String unit) {
        this(id, name, unit, null, null, null);
    }
    
    /**
     * Constructs a new sensor with full parameters.
     * 
     * @param id the unique identifier
     * @param name the human-readable name
     * @param unit the measurement unit
     * @param minValue the minimum measurable value
     * @param maxValue the maximum measurable value
     * @param resolution the measurement resolution
     */
    protected AbstractSensor(String id, String name, String unit, T minValue, T maxValue, T resolution) {
        super(id, name, ComponentType.SENSOR);
        this.unit = unit != null ? unit : "";
        this.minValue = minValue;
        this.maxValue = maxValue;
        this.resolution = resolution;
    }
    
    @Override
    public Optional<T> read() throws SensorException {
        return read(0);
    }
    
    @Override
    public Optional<T> read(long timeoutMs) throws SensorException {
        if (!isRunning()) {
            throw new SensorException(getId(), "Sensor is not running");
        }
        if (!isEnabled()) {
            logger.debug("[{}] Sensor disabled, returning empty", System.currentTimeMillis());
            return Optional.empty();
        }
        
        try {
            T value = doRead(timeoutMs);
            if (value != null) {
                lastValue.set(value);
                lastReadTimestamp = System.nanoTime();
                
                // Notify stream listener if active
                if (streaming.get() && streamListener != null) {
                    try {
                        streamListener.onData(getId(), value, lastReadTimestamp);
                    } catch (Exception e) {
                        logger.warn("[{}] Stream listener error: {}", System.currentTimeMillis(), e.getMessage());
                    }
                }
            }
            return Optional.ofNullable(value);
        } catch (Exception e) {
            logger.error("[{}] Read failed for sensor {}: {}", 
                    System.currentTimeMillis(), getId(), e.getMessage());
            throw new SensorException(getId(), "Read failed: " + e.getMessage(), e);
        }
    }
    
    /**
     * Template method for reading sensor data.
     * 
     * <p>Subclasses must implement this to perform actual data acquisition.</p>
     * 
     * @param timeoutMs the maximum time to wait in milliseconds (0 = no timeout)
     * @return the sensor reading, or null if unavailable
     * @throws Exception if the read fails
     */
    protected abstract T doRead(long timeoutMs) throws Exception;
    
    @Override
    public Optional<T> getLastValue() {
        return Optional.ofNullable(lastValue.get());
    }
    
    @Override
    public long getLastReadTimestamp() {
        return lastReadTimestamp;
    }
    
    @Override
    public double getSampleRate() {
        return sampleRate;
    }
    
    @Override
    public void setSampleRate(double rateHz) {
        if (rateHz <= 0 && rateHz != -1) {
            throw new IllegalArgumentException("Sample rate must be positive or -1");
        }
        this.sampleRate = rateHz;
        logger.debug("[{}] Sample rate set to {} Hz for sensor {}", 
                System.currentTimeMillis(), rateHz, getId());
    }
    
    @Override
    public void startStreaming(SensorDataListener<T> listener) {
        if (listener == null) {
            throw new IllegalArgumentException("Listener must not be null");
        }
        this.streamListener = listener;
        streaming.set(true);
        logger.info("[{}] Started streaming for sensor {}", System.currentTimeMillis(), getId());
    }
    
    @Override
    public void stopStreaming() {
        streaming.set(false);
        this.streamListener = null;
        logger.info("[{}] Stopped streaming for sensor {}", System.currentTimeMillis(), getId());
    }
    
    @Override
    public boolean isStreaming() {
        return streaming.get();
    }
    
    @Override
    public void calibrate() throws SensorException {
        logger.info("[{}] Calibrating sensor {}", System.currentTimeMillis(), getId());
        doCalibrate();
        logger.info("[{}] Sensor {} calibrated successfully", System.currentTimeMillis(), getId());
    }
    
    /**
     * Template method for sensor calibration.
     * 
     * <p>Subclasses can override this to implement sensor-specific calibration.</p>
     * 
     * @throws SensorException if calibration fails
     */
    protected void doCalibrate() throws SensorException {
        // Default: no calibration needed
    }
    
    @Override
    public String getUnit() {
        return unit;
    }
    
    @Override
    public T getMinValue() {
        return minValue;
    }
    
    @Override
    public T getMaxValue() {
        return maxValue;
    }
    
    @Override
    public T getResolution() {
        return resolution;
    }
    
    @Override
    protected void doStop() throws Exception {
        stopStreaming();
        super.doStop();
    }
    
    @Override
    public String toString() {
        return String.format("%s[id=%s, name=%s, unit=%s, state=%s]",
                getClass().getSimpleName(), getId(), getName(), unit, getState());
    }
}
