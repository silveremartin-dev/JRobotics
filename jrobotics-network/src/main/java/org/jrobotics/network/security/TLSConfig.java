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
import java.io.FileInputStream;
import java.io.IOException;
import java.security.*;
import java.security.cert.CertificateException;

/**
 * TLS/SSL Configuration for secure network communications.
 *
 * <p>
 * Provides configuration and factory methods for creating SSL contexts
 * and socket factories for encrypted robot communications.
 * </p>
 *
 * <p>
 * <b>Usage:</b>
 * </p>
 * 
 * <pre>
 * TLSConfig config = TLSConfig.builder()
 *         .keystorePath("/path/to/keystore.jks")
 *         .keystorePassword("password")
 *         .truststorePath("/path/to/truststore.jks")
 *         .truststorePassword("password")
 *         .build();
 *
 * SSLContext sslContext = config.createSSLContext();
 * </pre>
 *
 * @author Silvère Martin-Michiellot
 * @author Gemini AI Assistant
 * @version 2.0.0
 * @since 2.6.0
 */
public class TLSConfig {

    private final String keystorePath;
    private final String keystorePassword;
    private final String keystoreType;
    private final String truststorePath;
    private final String truststorePassword;
    private final String truststoreType;
    private final String protocol;
    private final boolean clientAuth;

    private TLSConfig(Builder builder) {
        this.keystorePath = builder.keystorePath;
        this.keystorePassword = builder.keystorePassword;
        this.keystoreType = builder.keystoreType;
        this.truststorePath = builder.truststorePath;
        this.truststorePassword = builder.truststorePassword;
        this.truststoreType = builder.truststoreType;
        this.protocol = builder.protocol;
        this.clientAuth = builder.clientAuth;
    }

    /**
     * Creates an SSLContext configured with the keystore and truststore.
     *
     * @return configured SSLContext
     * @throws SecurityException if SSL context creation fails
     */
    public SSLContext createSSLContext() throws SecurityException {
        try {
            // Load keystore
            KeyStore keyStore = null;
            KeyManagerFactory keyManagerFactory = null;

            if (keystorePath != null) {
                keyStore = KeyStore.getInstance(keystoreType);
                try (FileInputStream fis = new FileInputStream(keystorePath)) {
                    keyStore.load(fis, keystorePassword.toCharArray());
                }
                keyManagerFactory = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
                keyManagerFactory.init(keyStore, keystorePassword.toCharArray());
            }

            // Load truststore
            KeyStore trustStore = null;
            TrustManagerFactory trustManagerFactory = null;

            if (truststorePath != null) {
                trustStore = KeyStore.getInstance(truststoreType);
                try (FileInputStream fis = new FileInputStream(truststorePath)) {
                    trustStore.load(fis, truststorePassword.toCharArray());
                }
                trustManagerFactory = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
                trustManagerFactory.init(trustStore);
            }

            // Create SSL context
            SSLContext sslContext = SSLContext.getInstance(protocol);
            sslContext.init(
                    keyManagerFactory != null ? keyManagerFactory.getKeyManagers() : null,
                    trustManagerFactory != null ? trustManagerFactory.getTrustManagers() : null,
                    new SecureRandom());

            return sslContext;

        } catch (KeyStoreException | IOException | NoSuchAlgorithmException | CertificateException
                | UnrecoverableKeyException | KeyManagementException e) {
            throw new SecurityException("Failed to create SSL context", e);
        }
    }

    /**
     * Creates an SSLServerSocketFactory for secure server sockets.
     *
     * @return SSLServerSocketFactory
     * @throws SecurityException if creation fails
     */
    public SSLServerSocketFactory createServerSocketFactory() throws SecurityException {
        SSLContext ctx = createSSLContext();
        return ctx.getServerSocketFactory();
    }

    /**
     * Creates an SSLSocketFactory for secure client sockets.
     *
     * @return SSLSocketFactory
     * @throws SecurityException if creation fails
     */
    public SSLSocketFactory createSocketFactory() throws SecurityException {
        SSLContext ctx = createSSLContext();
        return ctx.getSocketFactory();
    }

    /**
     * Creates a default configuration for development/testing (insecure).
     *
     * @return default TLSConfig with system defaults
     */
    public static TLSConfig insecureDefault() {
        return new Builder()
                .protocol("TLS")
                .keystoreType("JKS")
                .truststoreType("JKS")
                .build();
    }

    /**
     * Returns whether client authentication is required.
     */
    public boolean isClientAuthRequired() {
        return clientAuth;
    }

    /**
     * Creates a new builder.
     */
    public static Builder builder() {
        return new Builder();
    }

    /**
     * Builder for TLSConfig.
     */
    public static class Builder {
        private String keystorePath;
        private String keystorePassword;
        private String keystoreType = "PKCS12";
        private String truststorePath;
        private String truststorePassword;
        private String truststoreType = "PKCS12";
        private String protocol = "TLSv1.3";
        private boolean clientAuth = false;

        public Builder keystorePath(String path) {
            this.keystorePath = path;
            return this;
        }

        public Builder keystorePassword(String password) {
            this.keystorePassword = password;
            return this;
        }

        public Builder keystoreType(String type) {
            this.keystoreType = type;
            return this;
        }

        public Builder truststorePath(String path) {
            this.truststorePath = path;
            return this;
        }

        public Builder truststorePassword(String password) {
            this.truststorePassword = password;
            return this;
        }

        public Builder truststoreType(String type) {
            this.truststoreType = type;
            return this;
        }

        public Builder protocol(String protocol) {
            this.protocol = protocol;
            return this;
        }

        public Builder clientAuth(boolean required) {
            this.clientAuth = required;
            return this;
        }

        public TLSConfig build() {
            return new TLSConfig(this);
        }
    }
}
