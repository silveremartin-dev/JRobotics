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
 * Sensor fusion algorithms for combining multiple sensor inputs.
 * 
 * <p>Implements state estimation and filtering techniques:</p>
 * <ul>
 *   <li>{@link org.jrobotics.sensor.fusion.KalmanFilter1D} - 1D Kalman filter</li>
 *   <li>{@link org.jrobotics.sensor.fusion.ComplementaryFilter} - IMU fusion</li>
 *   <li>{@link org.jrobotics.sensor.fusion.FusedPose} - Fused pose estimate</li>
 * </ul>
 * 
 * <p><b>References:</b></p>
 * <ul>
 *   <li>Welch, G., &amp; Bishop, G. (2006). <i>An Introduction to the Kalman Filter</i>. 
 *       University of North Carolina at Chapel Hill.</li>
 *   <li>Mahony, R., Hamel, T., &amp; Pflimlin, J. M. (2008). Nonlinear complementary 
 *       filters on the special orthogonal group. <i>IEEE TAC</i>, 53(5).</li>
 * </ul>
 * 
 * @author Silvère Martin-Michiellot
 * @author Gemini AI Assistant
 * @version 2.0.0
 * @since 2.0.0
 */
package org.jrobotics.sensor.fusion;
