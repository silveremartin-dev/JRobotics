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
 * Denavit-Hartenberg (DH) parameters for robotic arm kinematics.
 * 
 * <p>
 * Represents a single link in a kinematic chain using the standard DH
 * convention.
 * </p>
 * 
 * @author Silvère Martin-Michiellot
 * @author Gemini AI Assistant
 * @version 2.0.0
 * @since 2.4.0
 */
public class DenavitHartenberg {

    private final double a; // Link length
    private final double alpha; // Link twist
    private final double d; // Link offset
    private final double theta; // Joint angle

    /**
     * Creates a DH parameter set for a link.
     * 
     * @param theta Joint angle (rotation about Zn-1)
     * @param d     Link offset (distance along Zn-1)
     * @param a     Link length (distance along Xn)
     * @param alpha Link twist (rotation about Xn)
     */
    public DenavitHartenberg(double theta, double d, double a, double alpha) {
        this.theta = theta;
        this.d = d;
        this.a = a;
        this.alpha = alpha;
    }

    /**
     * Computes the transformation matrix for this link.
     * 
     * @return a 4x4 transformation matrix
     */
    public double[][] getTransformationMatrix() {
        double ct = Math.cos(theta);
        double st = Math.sin(theta);
        double ca = Math.cos(alpha);
        double sa = Math.sin(alpha);

        return new double[][] {
                { ct, -st * ca, st * sa, a * ct },
                { st, ct * ca, -ct * sa, a * st },
                { 0, sa, ca, d },
                { 0, 0, 0, 1 }
        };
    }

    public double getA() {
        return a;
    }

    public double getAlpha() {
        return alpha;
    }

    public double getD() {
        return d;
    }

    public double getTheta() {
        return theta;
    }

    @Override
    public String toString() {
        return String.format("DH[θ=%.2f, d=%.2f, a=%.2f, α=%.2f]", theta, d, a, alpha);
    }
}
