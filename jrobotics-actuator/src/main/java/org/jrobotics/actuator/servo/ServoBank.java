/*
 * JRobotics v2 - World-Class General Purpose Robotics API
 * 
 * Copyright (c) 2025 Silvère Martin-Michiellot <silvere.martin@gmail.com>
 * Co-authored by Gemini AI Assistant
 * 
 * Licensed under the MIT License.
 * See LICENSE file in the project root for full license information.
 */
package org.jrobotics.actuator.servo;

import org.jrobotics.core.Component;

/**
 * Interface for a multi-servo controller bank.
 * 
 * <p>
 * Represents hardware that controls multiple servos, such as:
 * <ul>
 * <li>Adafruit PCA9685 (16-channel PWM driver)</li>
 * <li>Pololu Maestro (6, 12, 18, 24 channels)</li>
 * <li>Lynxmotion SSC-32</li>
 * </ul>
 * </p>
 * 
 * <p>
 * Ports advanced features from JRobotics V1 ServoController, including:
 * <ul>
 * <li>Speed control</li>
 * <li>Acceleration control</li>
 * <li>Synchronized movement</li>
 * <li>Servo configuration (min/max pulse)</li>
 * </ul>
 * </p>
 * 
 * @author Silvère Martin-Michiellot
 * @author Gemini AI Assistant
 * @version 2.0.0
 * @since 2.5.0
 */
public interface ServoBank extends Component {

    /**
     * Gets the number of servo channels available on this bank.
     * 
     * @return the channel count (e.g., 16)
     */
    int getChannelCount();

    /**
     * Gets a specific servo instance managed by this bank.
     * 
     * @param channel the channel index (0 to getChannelCount() - 1)
     * @return the Servo object
     */
    Servo getServo(int channel);

    /**
     * Sets the position of a servo.
     * 
     * @param channel the channel index
     * @param angle   degrees
     */
    void setPosition(int channel, double angle);

    /**
     * Sets the speed for a servo channel.
     * 
     * <p>
     * 0 = instant (max speed), >0 = units per second (controller dependent)
     * </p>
     * 
     * @param channel the channel index
     * @param speed   speed value
     */
    void setSpeed(int channel, double speed);

    /**
     * Sets the acceleration for a servo channel.
     * 
     * <p>
     * 0 = instant (max accel), >0 = units per second squared
     * </p>
     * 
     * @param channel      the channel index
     * @param acceleration acceleration value
     */
    void setAcceleration(int channel, double acceleration);

    /**
     * Enables or disables a servo channel (stops sending pulses).
     * 
     * @param channel the channel index
     * @param enabled true to enable, false to disable
     */
    void setEnabled(int channel, boolean enabled);

    /**
     * Sets multiple servo positions simultaneously.
     * 
     * <p>
     * Useful for synchronized multi-joint movement (hexapods, arms).
     * </p>
     * 
     * @param channels array of channel indices
     * @param angles   array of target angles
     */
    void setPositions(int[] channels, double[] angles);
}
