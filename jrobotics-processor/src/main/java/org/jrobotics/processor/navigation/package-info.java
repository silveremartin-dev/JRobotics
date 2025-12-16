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
 * Navigation and path planning algorithms.
 * 
 * <p>Provides algorithms for autonomous robot navigation:</p>
 * <ul>
 *   <li>{@link org.jrobotics.processor.navigation.AStarPathPlanner} - A* grid-based planning</li>
 *   <li>{@link org.jrobotics.processor.GoToGoalProcessor} - Reactive goal navigation</li>
 * </ul>
 * 
 * <p><b>References:</b></p>
 * <ul>
 *   <li>Hart, P. E., et al. (1968). A Formal Basis for the Heuristic 
 *       Determination of Minimum Cost Paths. <i>IEEE Trans</i>.</li>
 *   <li>LaValle, S. M. (2006). <i>Planning Algorithms</i>. Cambridge.</li>
 * </ul>
 * 
 * @author Silvère Martin-Michiellot
 * @author Gemini AI Assistant
 * @version 2.0.0
 * @since 2.0.0
 */
package org.jrobotics.processor.navigation;
