/*
 * JRobotics v2 - World-Class General Purpose Robotics API
 * 
 * Copyright (c) 2025 Silvère Martin-Michiellot <silvere.martin@gmail.com>
 * Co-authored by Gemini AI Assistant
 * 
 * Licensed under the MIT License.
 * See LICENSE file in the project root for full license information.
 */
package org.jrobotics.sensor.onewire;

import org.jrobotics.sensor.AbstractSensor;
import org.jrobotics.sensor.environmental.Thermometer;

/**
 * Implementation of the DS18B20 digital thermometer.
 * 
 * <p>
 * A very common 1-Wire temperature sensor.
 * </p>
 * 
 * @author Silvère Martin-Michiellot
 * @author Gemini AI Assistant
 * @version 2.0.0
 * @since 2.5.0
 */
public class DS18B20 extends AbstractSensor<Double> implements OneWireSensor, Thermometer {

    private final String address;
    private volatile double temperature = 0.0;

    public DS18B20(String id, String name, String address) {
        super(id, name, "Celsius");
        this.address = address;
    }

    @Override
    public String getAddress() {
        return address;
    }

    @Override
    public int getFamilyCode() {
        return 0x28;
    }

    @Override
    protected Double doRead(long timeoutMs) {
        // In real impl, would trigger conversion and read scratchpad
        return temperature;
    }

    public double getTemperature() {
        // getLastValue returns Optional, use orElse(0.0) or current generic caching
        // logic
        // But Generic Sensor caches value in 'lastValue'.
        return getLastValue().orElse(0.0);
    }

    // For simulation/mocking
    public void setSimulatedTemperature(double temp) {
        this.temperature = temp;
    }

    @Override
    public double getTemperatureCelsius() {
        return getTemperature();
    }

    @Override
    public double getTemperatureFahrenheit() {
        return getTemperature() * 9.0 / 5.0 + 32.0;
    }
}
