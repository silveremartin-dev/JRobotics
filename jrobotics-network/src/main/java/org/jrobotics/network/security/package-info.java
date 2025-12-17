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
 * Security Package for Robot Network Communications.
 *
 * <p>
 * Provides TLS/SSL encryption, authentication, and role-based access control:
 * </p>
 * <ul>
 * <li>{@link org.jrobotics.network.security.TLSConfig} - SSL configuration</li>
 * <li>{@link org.jrobotics.network.security.SecureTransport} - Encrypted
 * sockets</li>
 * <li>{@link org.jrobotics.network.security.RBACManager} - Role-based access
 * control</li>
 * <li>{@link org.jrobotics.network.security.AuthenticationService} - User
 * authentication</li>
 * </ul>
 *
 * @since 2.6.0
 */
package org.jrobotics.network.security;
