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
 * Behavior tree implementation for robot decision-making.
 * 
 * <p>Provides a modular, reactive control architecture:</p>
 * <ul>
 *   <li>{@link org.jrobotics.processor.behavior.BehaviorNode} - Base node</li>
 *   <li>{@link org.jrobotics.processor.behavior.Sequence} - Sequential execution</li>
 *   <li>{@link org.jrobotics.processor.behavior.Selector} - Fallback selection</li>
 *   <li>{@link org.jrobotics.processor.behavior.Condition} - Condition check</li>
 *   <li>{@link org.jrobotics.processor.behavior.Action} - Leaf action</li>
 *   <li>{@link org.jrobotics.processor.behavior.BehaviorTree} - Tree executor</li>
 * </ul>
 * 
 * <p><b>References:</b></p>
 * <ul>
 *   <li>Colledanchise, M., &amp; Ögren, P. (2018). <i>Behavior Trees in Robotics 
 *       and AI</i>. CRC Press.</li>
 *   <li>Marzinotto, A., et al. (2014). Towards a Unified Behavior Trees Framework
 *       for Robot Control. <i>IEEE ICRA</i>.</li>
 * </ul>
 * 
 * @author Silvère Martin-Michiellot
 * @author Gemini AI Assistant
 * @version 2.0.0
 * @since 2.0.0
 */
package org.jrobotics.processor.behavior;
