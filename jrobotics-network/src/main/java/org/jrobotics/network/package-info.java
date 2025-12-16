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
 * Network communication and messaging for multi-robot systems.
 * 
 * <p>Provides networking capabilities:</p>
 * <ul>
 *   <li>{@link org.jrobotics.network.NetworkNode} - Base network node interface</li>
 *   <li>{@link org.jrobotics.network.UdpNode} - UDP peer-to-peer communication</li>
 *   <li>{@link org.jrobotics.network.server.TcpServer} - TCP server</li>
 *   <li>{@link org.jrobotics.network.server.TcpClient} - TCP client</li>
 *   <li>{@link org.jrobotics.network.control.RemoteMaster} - Remote control master</li>
 *   <li>{@link org.jrobotics.network.control.RemoteSlave} - Remote control slave</li>
 * </ul>
 * 
 * @author Silvère Martin-Michiellot
 * @author Gemini AI Assistant
 * @version 2.0.0
 * @since 2.0.0
 * @see <a href="file:../../../docs/NETWORKING.md">Networking Comparison</a>
 */
package org.jrobotics.network;
