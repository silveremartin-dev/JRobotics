/*
 * JRobotics v2 - World-Class General Purpose Robotics API
 * 
 * Copyright (c) 2025 Silvère Martin-Michiellot <silvere.martin@gmail.com>
 * Co-authored by Gemini AI Assistant
 * 
 * Licensed under the MIT License.
 * See LICENSE file in the project root for full license information.
 */
package org.jrobotics.processor.control;

/**
 * PID (Proportional-Integral-Derivative) controller.
 * 
 * <p>Classic feedback controller for motion control, temperature regulation,
 * and other closed-loop control applications.</p>
 * 
 * <p><b>Control equation:</b></p>
 * <pre>
 * u(t) = Kp * e(t) + Ki * ∫e(τ)dτ + Kd * de(t)/dt
 * </pre>
 * 
 * <p><b>Features:</b></p>
 * <ul>
 *   <li>Anti-windup protection</li>
 *   <li>Output clamping</li>
 *   <li>Derivative on measurement (avoids setpoint kick)</li>
 *   <li>Low-pass filter on derivative term</li>
 * </ul>
 * 
 * @author Silvère Martin-Michiellot
 * @author Gemini AI Assistant
 * @version 2.0.0
 * @since 2.0.0
 */
public class PIDController {
    
    // PID gains
    private double kp;
    private double ki;
    private double kd;
    
    // State
    private double integral = 0;
    private double lastError = 0;
    private double lastMeasurement = 0;
    private long lastTimeNs = 0;
    
    // Limits
    private double outputMin = Double.NEGATIVE_INFINITY;
    private double outputMax = Double.POSITIVE_INFINITY;
    private double integralMin = Double.NEGATIVE_INFINITY;
    private double integralMax = Double.POSITIVE_INFINITY;
    
    // Derivative filter coefficient (0-1, higher = more filtering)
    private double derivativeFilter = 0.0;
    private double filteredDerivative = 0;
    
    // Setpoint
    private double setpoint = 0;
    
    /**
     * Creates a PID controller.
     * 
     * @param kp proportional gain
     * @param ki integral gain
     * @param kd derivative gain
     */
    public PIDController(double kp, double ki, double kd) {
        this.kp = kp;
        this.ki = ki;
        this.kd = kd;
    }
    
    /**
     * Computes PID output.
     * 
     * @param measurement current measured value
     * @return control output
     */
    public double compute(double measurement) {
        long now = System.nanoTime();
        double dt = (lastTimeNs == 0) ? 0 : (now - lastTimeNs) / 1e9;
        lastTimeNs = now;
        
        return compute(measurement, dt);
    }
    
    /**
     * Computes PID output with explicit time step.
     * 
     * @param measurement current measured value
     * @param dt time step in seconds
     * @return control output
     */
    public double compute(double measurement, double dt) {
        double error = setpoint - measurement;
        
        // Proportional term
        double pTerm = kp * error;
        
        // Integral term with anti-windup
        if (dt > 0) {
            integral += error * dt;
            integral = clamp(integral, integralMin, integralMax);
        }
        double iTerm = ki * integral;
        
        // Derivative term (on measurement to avoid kick)
        double derivative = 0;
        if (dt > 0) {
            derivative = -(measurement - lastMeasurement) / dt;
            
            // Low-pass filter on derivative
            if (derivativeFilter > 0) {
                filteredDerivative = derivativeFilter * filteredDerivative + 
                                    (1 - derivativeFilter) * derivative;
                derivative = filteredDerivative;
            }
        }
        double dTerm = kd * derivative;
        
        lastError = error;
        lastMeasurement = measurement;
        
        // Compute and clamp output
        double output = pTerm + iTerm + dTerm;
        return clamp(output, outputMin, outputMax);
    }
    
    /**
     * Sets the setpoint.
     */
    public void setSetpoint(double setpoint) {
        this.setpoint = setpoint;
    }
    
    /**
     * Gets the setpoint.
     */
    public double getSetpoint() {
        return setpoint;
    }
    
    /**
     * Sets PID gains.
     */
    public void setGains(double kp, double ki, double kd) {
        this.kp = kp;
        this.ki = ki;
        this.kd = kd;
    }
    
    /**
     * Sets output limits.
     */
    public void setOutputLimits(double min, double max) {
        this.outputMin = min;
        this.outputMax = max;
    }
    
    /**
     * Sets integral limits (anti-windup).
     */
    public void setIntegralLimits(double min, double max) {
        this.integralMin = min;
        this.integralMax = max;
    }
    
    /**
     * Sets derivative low-pass filter coefficient.
     * 
     * @param alpha filter coefficient (0 = no filter, 0.9 = heavy filtering)
     */
    public void setDerivativeFilter(double alpha) {
        this.derivativeFilter = clamp(alpha, 0, 0.99);
    }
    
    /**
     * Resets controller state.
     */
    public void reset() {
        integral = 0;
        lastError = 0;
        lastMeasurement = 0;
        lastTimeNs = 0;
        filteredDerivative = 0;
    }
    
    /**
     * Gets current error.
     */
    public double getError() {
        return lastError;
    }
    
    /**
     * Gets current integral term.
     */
    public double getIntegral() {
        return integral;
    }
    
    private double clamp(double value, double min, double max) {
        return Math.max(min, Math.min(max, value));
    }
    
    /**
     * Creates a velocity PID controller (common for motors).
     */
    public static PIDController velocityController() {
        PIDController pid = new PIDController(0.5, 0.1, 0.01);
        pid.setOutputLimits(-1.0, 1.0);
        pid.setIntegralLimits(-10, 10);
        return pid;
    }
    
    /**
     * Creates a position PID controller.
     */
    public static PIDController positionController() {
        PIDController pid = new PIDController(1.0, 0.01, 0.1);
        pid.setOutputLimits(-10, 10);
        pid.setIntegralLimits(-100, 100);
        pid.setDerivativeFilter(0.8);
        return pid;
    }
}
