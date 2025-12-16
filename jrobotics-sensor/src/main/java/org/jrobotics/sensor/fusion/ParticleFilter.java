/*
 * JRobotics v2 - World-Class General Purpose Robotics API
 * 
 * Copyright (c) 2025 Silvère Martin-Michiellot <silvere.martin@gmail.com>
 * Co-authored by Gemini AI Assistant
 * 
 * Licensed under the MIT License.
 * See LICENSE file in the project root for full license information.
 */
package org.jrobotics.sensor.fusion;

import java.util.Random;

/**
 * Particle Filter (Sequential Monte Carlo) for robot localization.
 * 
 * <p>Uses a set of weighted samples (particles) to represent the belief 
 * distribution over robot pose. Each particle represents a hypothesis 
 * about the robot's position and orientation.</p>
 * 
 * <p><b>Algorithm:</b></p>
 * <ol>
 *   <li><b>Prediction:</b> Move particles according to motion model</li>
 *   <li><b>Update:</b> Weight particles by measurement likelihood</li>
 *   <li><b>Resample:</b> Duplicate high-weight particles, remove low-weight</li>
 * </ol>
 * 
 * <p><b>References:</b></p>
 * <ul>
 *   <li>Thrun, S., Burgard, W., &amp; Fox, D. (2005). <i>Probabilistic Robotics</i>. MIT Press.</li>
 *   <li>Dellaert, F., et al. (1999). Monte Carlo localization for mobile robots.</li>
 * </ul>
 * 
 * @author Silvère Martin-Michiellot  
 * @author Gemini AI Assistant
 * @version 2.0.0
 * @since 2.0.0
 */
public class ParticleFilter {
    
    private final int numParticles;
    private final double[][] particles; // [numParticles][3] = [x, y, theta]
    private final double[] weights;
    private final Random random;
    
    // Motion noise parameters
    private double alphaV = 0.1;  // Velocity noise
    private double alphaW = 0.1;  // Angular velocity noise
    
    // Measurement noise
    private double measurementNoise = 0.5;
    
    /**
     * Creates a particle filter.
     * 
     * @param numParticles number of particles
     */
    public ParticleFilter(int numParticles) {
        this.numParticles = numParticles;
        this.particles = new double[numParticles][3];
        this.weights = new double[numParticles];
        this.random = new Random();
        
        // Initialize uniform weights
        for (int i = 0; i < numParticles; i++) {
            weights[i] = 1.0 / numParticles;
        }
    }
    
    /**
     * Initializes particles uniformly in given bounds.
     */
    public void initializeUniform(double minX, double maxX, double minY, double maxY) {
        for (int i = 0; i < numParticles; i++) {
            particles[i][0] = minX + random.nextDouble() * (maxX - minX);
            particles[i][1] = minY + random.nextDouble() * (maxY - minY);
            particles[i][2] = random.nextDouble() * 2 * Math.PI - Math.PI;
            weights[i] = 1.0 / numParticles;
        }
    }
    
    /**
     * Initializes particles around a known pose.
     */
    public void initializeGaussian(double x, double y, double theta, 
                                    double stdX, double stdY, double stdTheta) {
        for (int i = 0; i < numParticles; i++) {
            particles[i][0] = x + random.nextGaussian() * stdX;
            particles[i][1] = y + random.nextGaussian() * stdY;
            particles[i][2] = normalizeAngle(theta + random.nextGaussian() * stdTheta);
            weights[i] = 1.0 / numParticles;
        }
    }
    
    /**
     * Prediction step: propagate particles using motion model.
     * 
     * @param velocity linear velocity
     * @param angularVelocity angular velocity  
     * @param dt time step
     */
    public void predict(double velocity, double angularVelocity, double dt) {
        for (int i = 0; i < numParticles; i++) {
            // Add noise to control
            double noisyV = velocity + random.nextGaussian() * alphaV * Math.abs(velocity);
            double noisyW = angularVelocity + random.nextGaussian() * alphaW * Math.abs(angularVelocity);
            
            double theta = particles[i][2];
            
            // Motion model
            if (Math.abs(noisyW) < 1e-6) {
                // Straight line motion
                particles[i][0] += noisyV * Math.cos(theta) * dt;
                particles[i][1] += noisyV * Math.sin(theta) * dt;
            } else {
                // Arc motion
                double r = noisyV / noisyW;
                particles[i][0] += r * (-Math.sin(theta) + Math.sin(theta + noisyW * dt));
                particles[i][1] += r * (Math.cos(theta) - Math.cos(theta + noisyW * dt));
                particles[i][2] = normalizeAngle(theta + noisyW * dt);
            }
        }
    }
    
    /**
     * Update step: weight particles by landmark measurement.
     * 
     * @param measuredRange measured distance to landmark
     * @param measuredBearing measured bearing to landmark
     * @param landmarkX landmark X position
     * @param landmarkY landmark Y position
     */
    public void updateLandmark(double measuredRange, double measuredBearing,
                                double landmarkX, double landmarkY) {
        double sumWeights = 0;
        
        for (int i = 0; i < numParticles; i++) {
            double dx = landmarkX - particles[i][0];
            double dy = landmarkY - particles[i][1];
            
            double expectedRange = Math.sqrt(dx * dx + dy * dy);
            double expectedBearing = normalizeAngle(Math.atan2(dy, dx) - particles[i][2]);
            
            double rangeError = measuredRange - expectedRange;
            double bearingError = normalizeAngle(measuredBearing - expectedBearing);
            
            // Gaussian likelihood
            double likelihood = gaussian(rangeError, measurementNoise) * 
                               gaussian(bearingError, measurementNoise * 0.5);
            
            weights[i] *= likelihood;
            sumWeights += weights[i];
        }
        
        // Normalize weights
        if (sumWeights > 0) {
            for (int i = 0; i < numParticles; i++) {
                weights[i] /= sumWeights;
            }
        } else {
            // All weights zero - reinitialize uniformly
            for (int i = 0; i < numParticles; i++) {
                weights[i] = 1.0 / numParticles;
            }
        }
    }
    
    /**
     * Update step: weight particles by GPS-like position measurement.
     */
    public void updatePosition(double measuredX, double measuredY, double stdDev) {
        double sumWeights = 0;
        
        for (int i = 0; i < numParticles; i++) {
            double dx = measuredX - particles[i][0];
            double dy = measuredY - particles[i][1];
            double dist = Math.sqrt(dx * dx + dy * dy);
            
            weights[i] *= gaussian(dist, stdDev);
            sumWeights += weights[i];
        }
        
        if (sumWeights > 0) {
            for (int i = 0; i < numParticles; i++) {
                weights[i] /= sumWeights;
            }
        }
    }
    
    /**
     * Resample particles using low-variance resampling.
     */
    public void resample() {
        double[][] newParticles = new double[numParticles][3];
        
        // Low-variance resampling
        double r = random.nextDouble() / numParticles;
        double c = weights[0];
        int j = 0;
        
        for (int i = 0; i < numParticles; i++) {
            double u = r + i * (1.0 / numParticles);
            while (u > c && j < numParticles - 1) {
                j++;
                c += weights[j];
            }
            newParticles[i][0] = particles[j][0];
            newParticles[i][1] = particles[j][1];
            newParticles[i][2] = particles[j][2];
        }
        
        // Copy back and reset weights
        for (int i = 0; i < numParticles; i++) {
            particles[i][0] = newParticles[i][0];
            particles[i][1] = newParticles[i][1];
            particles[i][2] = newParticles[i][2];
            weights[i] = 1.0 / numParticles;
        }
    }
    
    /**
     * Gets estimated pose (weighted mean).
     */
    public double[] getEstimatedPose() {
        double x = 0, y = 0;
        double sinSum = 0, cosSum = 0;
        
        for (int i = 0; i < numParticles; i++) {
            x += weights[i] * particles[i][0];
            y += weights[i] * particles[i][1];
            sinSum += weights[i] * Math.sin(particles[i][2]);
            cosSum += weights[i] * Math.cos(particles[i][2]);
        }
        
        return new double[]{x, y, Math.atan2(sinSum, cosSum)};
    }
    
    /**
     * Gets all particles for visualization.
     */
    public double[][] getParticles() {
        return particles.clone();
    }
    
    /**
     * Gets particle weights.
     */
    public double[] getWeights() {
        return weights.clone();
    }
    
    /**
     * Gets effective sample size (ESS).
     */
    public double getEffectiveSampleSize() {
        double sumSquaredWeights = 0;
        for (double w : weights) {
            sumSquaredWeights += w * w;
        }
        return 1.0 / sumSquaredWeights;
    }
    
    /**
     * Sets motion noise parameters.
     */
    public void setMotionNoise(double alphaV, double alphaW) {
        this.alphaV = alphaV;
        this.alphaW = alphaW;
    }
    
    /**
     * Sets measurement noise.
     */
    public void setMeasurementNoise(double noise) {
        this.measurementNoise = noise;
    }
    
    private double gaussian(double x, double sigma) {
        return Math.exp(-0.5 * x * x / (sigma * sigma)) / (sigma * Math.sqrt(2 * Math.PI));
    }
    
    private double normalizeAngle(double angle) {
        while (angle > Math.PI) angle -= 2 * Math.PI;
        while (angle < -Math.PI) angle += 2 * Math.PI;
        return angle;
    }
}
