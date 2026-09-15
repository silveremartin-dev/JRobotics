/*
 * JRobotics v2 - World-Class General Purpose Robotics API
 * 
 * Copyright (c) 2025 Silvère Martin-Michiellot <silvere.martin@gmail.com>
 * Co-authored by Gemini AI Assistant
 * 
 * Licensed under the MIT License.
 * See LICENSE file in the project root for full license information.
 */
package org.jrobotics.network.security;

import javax.net.ssl.*;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Secure Transport Layer using TLS/SSL.
 *
 * <p>
 * Provides factory methods for creating SSL-secured sockets and server sockets
 * for encrypted robot-to-robot and robot-to-server communications.
 * </p>
 *
 * @author Silvère Martin-Michiellot
 * @author Gemini AI Assistant
 * @version 2.0.0
 * @since 2.6.0
 */
public class SecureTransport {

    private static final Logger logger = LoggerFactory.getLogger(SecureTransport.class);

    private final TLSConfig config;
    private SSLContext sslContext;

    /**
     * Creates a secure transport with the given TLS configuration.
     *
     * @param config TLS configuration
     */
    public SecureTransport(TLSConfig config) {
        this.config = config;
    }

    /**
     * Initializes the SSL context.
     *
     * @throws SecurityException if initialization fails
     */
    public void initialize() throws SecurityException {
        this.sslContext = config.createSSLContext();
        logger.info("Secure transport initialized");
    }

    /**
     * Creates a secure server socket.
     *
     * @param port the port to bind to
     * @return SSLServerSocket
     * @throws IOException if socket creation fails
     */
    public ServerSocket createServerSocket(int port) throws IOException {
        if (sslContext == null) {
            throw new IllegalStateException("Transport not initialized");
        }

        SSLServerSocketFactory factory = sslContext.getServerSocketFactory();
        SSLServerSocket serverSocket = (SSLServerSocket) factory.createServerSocket(port);

        // Configure protocols and cipher suites
        serverSocket.setEnabledProtocols(new String[] { "TLSv1.3", "TLSv1.2" });
        serverSocket.setNeedClientAuth(config.isClientAuthRequired());

        logger.info("Secure server socket created on port {}", port);
        return serverSocket;
    }

    /**
     * Creates a secure client socket.
     *
     * @param host the host to connect to
     * @param port the port to connect to
     * @return SSLSocket
     * @throws IOException if socket creation fails
     */
    public Socket createSocket(String host, int port) throws IOException {
        if (sslContext == null) {
            throw new IllegalStateException("Transport not initialized");
        }

        SSLSocketFactory factory = sslContext.getSocketFactory();
        SSLSocket socket = (SSLSocket) factory.createSocket(host, port);

        // Configure protocols
        socket.setEnabledProtocols(new String[] { "TLSv1.3", "TLSv1.2" });

        // Enable TLS hostname verification to prevent MitM attacks
        SSLParameters params = socket.getSSLParameters();
        params.setEndpointIdentificationAlgorithm("HTTPS");
        socket.setSSLParameters(params);

        // Start handshake
        socket.startHandshake();

        logger.info("Secure connection established to {}:{}", host, port);
        return socket;
    }

    /**
     * Wraps an existing socket with SSL.
     *
     * @param socket    the socket to wrap
     * @param host      the host (for verification)
     * @param port      the port
     * @param autoClose whether to close underlying socket
     * @return SSLSocket
     * @throws IOException if wrapping fails
     */
    public SSLSocket wrapSocket(Socket socket, String host, int port, boolean autoClose) throws IOException {
        if (sslContext == null) {
            throw new IllegalStateException("Transport not initialized");
        }

        SSLSocketFactory factory = sslContext.getSocketFactory();
        SSLSocket sslSocket = (SSLSocket) factory.createSocket(socket, host, port, autoClose);
        sslSocket.setEnabledProtocols(new String[] { "TLSv1.3", "TLSv1.2" });

        // Enable TLS hostname verification
        SSLParameters params = sslSocket.getSSLParameters();
        params.setEndpointIdentificationAlgorithm("HTTPS");
        sslSocket.setSSLParameters(params);

        sslSocket.startHandshake();

        return sslSocket;
    }

    /**
     * Gets the SSL context.
     *
     * @return SSLContext
     */
    public SSLContext getSSLContext() {
        return sslContext;
    }

    /**
     * Gets session information from an SSL socket.
     *
     * @param socket the SSL socket
     * @return session info string
     */
    public String getSessionInfo(SSLSocket socket) {
        SSLSession session = socket.getSession();
        return String.format("Protocol: %s, CipherSuite: %s, PeerHost: %s",
                session.getProtocol(),
                session.getCipherSuite(),
                session.getPeerHost());
    }
}
