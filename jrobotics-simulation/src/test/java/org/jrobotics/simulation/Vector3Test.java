/*
 * JRobotics v2 - World-Class General Purpose Robotics API
 * 
 * Copyright (c) 2025 Silvère Martin-Michiellot <silvere.martin@gmail.com>
 * Co-authored by Gemini AI Assistant
 * 
 * Licensed under the MIT License.
 * See LICENSE file in the project root for full license information.
 */
package org.jrobotics.simulation;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for {@link Vector3}.
 * 
 * @author Silvère Martin-Michiellot
 * @author Gemini AI Assistant
 */
class Vector3Test {

    @Test
    void testAddition() {
        Vector3 a = new Vector3(1, 2, 3);
        Vector3 b = new Vector3(4, 5, 6);
        Vector3 result = a.add(b);

        assertEquals(5, result.x(), 1e-10);
        assertEquals(7, result.y(), 1e-10);
        assertEquals(9, result.z(), 1e-10);
    }

    @Test
    void testSubtraction() {
        Vector3 a = new Vector3(4, 5, 6);
        Vector3 b = new Vector3(1, 2, 3);
        Vector3 result = a.subtract(b);

        assertEquals(3, result.x(), 1e-10);
        assertEquals(3, result.y(), 1e-10);
        assertEquals(3, result.z(), 1e-10);
    }

    @Test
    void testMultiply() {
        Vector3 v = new Vector3(2, 3, 4);
        Vector3 result = v.multiply(2);

        assertEquals(4, result.x(), 1e-10);
        assertEquals(6, result.y(), 1e-10);
        assertEquals(8, result.z(), 1e-10);
    }

    @Test
    void testMagnitude() {
        Vector3 v = new Vector3(3, 4, 0);
        assertEquals(5, v.magnitude(), 1e-10);
    }

    @Test
    void testNormalize() {
        Vector3 v = new Vector3(3, 4, 0);
        Vector3 normalized = v.normalize();

        assertEquals(1, normalized.magnitude(), 1e-10);
        assertEquals(0.6, normalized.x(), 1e-10);
        assertEquals(0.8, normalized.y(), 1e-10);
    }

    @Test
    void testNormalizeZeroVector() {
        Vector3 result = Vector3.ZERO.normalize();
        assertEquals(Vector3.ZERO, result);
    }

    @Test
    void testDotProduct() {
        Vector3 a = new Vector3(1, 2, 3);
        Vector3 b = new Vector3(4, 5, 6);
        double dot = a.dot(b);

        assertEquals(32, dot, 1e-10); // 1*4 + 2*5 + 3*6 = 4 + 10 + 18 = 32
    }

    @Test
    void testCrossProduct() {
        Vector3 x = Vector3.UNIT_X;
        Vector3 y = Vector3.UNIT_Y;
        Vector3 z = x.cross(y);

        assertEquals(0, z.x(), 1e-10);
        assertEquals(0, z.y(), 1e-10);
        assertEquals(1, z.z(), 1e-10);
    }

    @Test
    void testDistance() {
        Vector3 a = new Vector3(0, 0, 0);
        Vector3 b = new Vector3(3, 4, 0);

        assertEquals(5, a.distanceTo(b), 1e-10);
    }

    @Test
    void testXY() {
        Vector3 v = Vector3.xy(5, 10);
        assertEquals(5, v.x(), 1e-10);
        assertEquals(10, v.y(), 1e-10);
        assertEquals(0, v.z(), 1e-10);
    }
}
