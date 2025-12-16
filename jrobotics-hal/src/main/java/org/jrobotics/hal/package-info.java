/*
 * JRobotics v2 - World-Class General Purpose Robotics API
 * 
 * Copyright (c) 2025 Silvère Martin-Michiellot <silvere.martin@gmail.com>
 * Co-authored by Gemini AI Assistant
 * 
 * Licensed under the MIT License.
 * See LICENSE file in the project root for full license information.
 */

/**
 * Hardware Abstraction Layer interfaces for common peripherals.
 * 
 * <p>Provides platform-independent access to:</p>
 * <ul>
 *   <li>{@link org.jrobotics.hal.GpioPin} - Digital and analog I/O</li>
 *   <li>{@link org.jrobotics.hal.I2CBus} / {@link org.jrobotics.hal.I2CDevice} - I2C communication</li>
 *   <li>{@link org.jrobotics.hal.SpiBus} / {@link org.jrobotics.hal.SpiDevice} - SPI communication</li>
 *   <li>{@link org.jrobotics.hal.SerialPort} - UART serial communication</li>
 *   <li>{@link org.jrobotics.hal.HalProvider} - Platform-specific provider</li>
 * </ul>
 * 
 * @author Silvère Martin-Michiellot
 * @author Gemini AI Assistant
 * @version 2.0.0
 * @since 2.0.0
 */
package org.jrobotics.hal;
