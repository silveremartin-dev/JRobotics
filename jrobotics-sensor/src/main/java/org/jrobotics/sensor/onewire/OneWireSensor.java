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

import org.jrobotics.core.Component;

/**
 * Interface for 1-Wire sensors.
 * 
 * <p>
 * Represents devices connected via the Dallas/Maxim 1-Wire bus.
 * Unique 64-bit addresses identify each device.
 * </p>
 * 
 * @author Silvère Martin-Michiellot
 * @author Gemini AI Assistant
 * @version 2.0.0
 * @since 2.5.0
 */
public interface OneWireSensor extends Component {

    /**
     * Gets the 64-bit 1-Wire address (ROM ID).
     * 
     * @return hex string of the address
     */
    String getAddress();

    /**
     * Gets the family code of the device.
     * 
     * @return byte representing the family (e.g., 0x28 for DS18B20)
     */
    int getFamilyCode();
}
