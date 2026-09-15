/*
 * JRobotics v2 - World-Class General Purpose Robotics API
 * 
 * Copyright (c) 2025 Silvère Martin-Michiellot <silvere.martin@gmail.com>
 * Co-authored by Gemini AI Assistant
 * 
 * Licensed under the MIT License.
 * See LICENSE file in the project root for full license information.
 */
package org.jrobotics.core.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.MessageFormat;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

/**
 * Internationalization utility for loading localized messages.
 * 
 * <p>
 * Supports English, French, Spanish, and German by default.
 * </p>
 * 
 * <p>
 * Example usage:
 * </p>
 * 
 * <pre>{@code
 * I18n.setLocale(Locale.FRENCH);
 * String message = I18n.get("lifecycle.started", "MyRobot");
 * // Returns: "MyRobot démarré avec succès"
 * }</pre>
 * 
 * @author Silvère Martin-Michiellot
 * @author Gemini AI Assistant
 * @version 2.0.0
 * @since 2.0.0
 */
public final class I18n {

    private static final Logger logger = LoggerFactory.getLogger(I18n.class);
    private static final String BUNDLE_NAME = "i18n.messages";

    private static volatile Locale currentLocale = Locale.ENGLISH;
    private static volatile ResourceBundle bundle;

    static {
        loadBundle();
    }

    private I18n() {
        // Utility class - prevent instantiation
    }

    /**
     * Sets the current locale and reloads the resource bundle.
     * 
     * @param locale the new locale
     */
    public static synchronized void setLocale(Locale locale) {
        if (locale != null && !locale.equals(currentLocale)) {
            currentLocale = locale;
            loadBundle();
            logger.info("[{}] Locale changed to: {}", System.currentTimeMillis(), locale);
        }
    }

    /**
     * Gets the current locale.
     * 
     * @return the current locale
     */
    public static Locale getLocale() {
        return currentLocale;
    }

    /**
     * Gets a localized message by key.
     * 
     * @param key the message key
     * @return the localized message, or the key if not found
     */
    public static String get(String key) {
        if (key == null) return "";
        try {
            return bundle != null ? bundle.getString(key) : key;
        } catch (MissingResourceException e) {
            logger.warn("[{}] Missing i18n key: {}", System.currentTimeMillis(), key);
            return key;
        }
    }

    /**
     * Gets a localized message by key with parameter substitution.
     * 
     * <p>
     * Uses {@link MessageFormat} for parameter substitution.
     * </p>
     * 
     * @param key  the message key
     * @param args the message arguments
     * @return the formatted localized message
     */
    public static String get(String key, Object... args) {
        if (key == null) return "";
        try {
            String pattern = bundle != null ? bundle.getString(key) : key;
            return MessageFormat.format(pattern, args);
        } catch (MissingResourceException e) {
            logger.warn("[{}] Missing i18n key: {}", System.currentTimeMillis(), key);
            return key;
        }
    }

    /**
     * Checks if a key exists in the resource bundle.
     * 
     * @param key the key to check
     * @return true if the key exists
     */
    public static boolean hasKey(String key) {
        return key != null && bundle != null && bundle.containsKey(key);
    }

    /**
     * Gets the available locales.
     * 
     * @return array of supported locales
     */
    public static Locale[] getAvailableLocales() {
        return new Locale[] {
                Locale.ENGLISH,
                Locale.FRENCH,
                Locale.of("es"),
                Locale.GERMAN
        };
    }

    private static void loadBundle() {
        try {
            bundle = ResourceBundle.getBundle(BUNDLE_NAME, currentLocale);
            logger.debug("[{}] Loaded resource bundle for locale: {}",
                    System.currentTimeMillis(), currentLocale);
        } catch (MissingResourceException e) {
            logger.error("[{}] Failed to load resource bundle: {}",
                    System.currentTimeMillis(), e.getMessage());
            // Fall back to English
            try {
                bundle = ResourceBundle.getBundle(BUNDLE_NAME, Locale.ENGLISH);
            } catch (MissingResourceException ex) {
                bundle = null;
            }
        }
    }
}
