/*
 * JRobotics v2 - World-Class General Purpose Robotics API
 * 
 * Copyright (c) 2025 Silvère Martin-Michiellot <silvere.martin@gmail.com>
 * Co-authored by Gemini AI Assistant
 * 
 * Licensed under the MIT License.
 * See LICENSE file in the project root for full license information.
 */
package org.jrobotics.core.math;

/**
 * Represents a 3D vector for physics calculations.
 * 
 * @author Silvère Martin-Michiellot
 * @author Gemini AI Assistant
 * @version 2.0.0
 * @since 2.0.0
 */
public record Vector3(double x, double y, double z) {

    /** Zero vector */
    public static final Vector3 ZERO = new Vector3(0, 0, 0);
    /** Unit X vector */
    public static final Vector3 UNIT_X = new Vector3(1, 0, 0);
    /** Unit Y vector */
    public static final Vector3 UNIT_Y = new Vector3(0, 1, 0);
    /** Unit Z vector */
    public static final Vector3 UNIT_Z = new Vector3(0, 0, 1);

    /**
     * Creates a vector from X and Y (2D).
     */
    public static Vector3 xy(double x, double y) {
        return new Vector3(x, y, 0);
    }

    /**
     * Adds another vector.
     */
    public Vector3 add(Vector3 other) {
        return new Vector3(x + other.x, y + other.y, z + other.z);
    }

    /**
     * Subtracts another vector.
     */
    public Vector3 subtract(Vector3 other) {
        return new Vector3(x - other.x, y - other.y, z - other.z);
    }

    /**
     * Multiplies by a scalar.
     */
    public Vector3 multiply(double scalar) {
        return new Vector3(x * scalar, y * scalar, z * scalar);
    }

    /**
     * Gets the magnitude (length).
     */
    public double magnitude() {
        return Math.sqrt(x * x + y * y + z * z);
    }

    /**
     * Returns a normalized (unit) vector.
     */
    public Vector3 normalize() {
        double mag = magnitude();
        if (mag < 1e-10)
            return ZERO;
        return multiply(1.0 / mag);
    }

    /**
     * Dot product with another vector.
     */
    public double dot(Vector3 other) {
        return x * other.x + y * other.y + z * other.z;
    }

    /**
     * Cross product with another vector.
     */
    public Vector3 cross(Vector3 other) {
        return new Vector3(
                y * other.z - z * other.y,
                z * other.x - x * other.z,
                x * other.y - y * other.x);
    }

    /**
     * Distance to another vector.
     */
    public double distanceTo(Vector3 other) {
        return subtract(other).magnitude();
    }
}
