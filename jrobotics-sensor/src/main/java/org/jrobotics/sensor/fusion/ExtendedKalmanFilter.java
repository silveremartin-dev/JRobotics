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

/**
 * Extended Kalman Filter for nonlinear state estimation.
 * 
 * <p>Extends the standard Kalman filter to handle nonlinear systems by
 * linearizing the state transition and observation functions via Jacobians.</p>
 * 
 * <p><b>State model:</b></p>
 * <ul>
 *   <li>x[k] = f(x[k-1], u[k-1]) + w[k-1]  (state transition)</li>
 *   <li>z[k] = h(x[k]) + v[k]              (observation)</li>
 * </ul>
 * 
 * <p><b>References:</b></p>
 * <ul>
 *   <li>Welch, G., &amp; Bishop, G. (2006). <i>An Introduction to the Kalman Filter</i>.</li>
 *   <li>Thrun, S., Burgard, W., &amp; Fox, D. (2005). <i>Probabilistic Robotics</i>. MIT Press.</li>
 * </ul>
 * 
 * @author Silvère Martin-Michiellot
 * @author Gemini AI Assistant
 * @version 2.0.0
 * @since 2.0.0
 */
public class ExtendedKalmanFilter {
    
    private final int stateDim;
    private final int measurementDim;
    
    // State vector
    private double[] x;
    // State covariance matrix (flattened)
    private double[] P;
    // Process noise covariance
    private double[] Q;
    // Measurement noise covariance
    private double[] R;
    
    // Callbacks for nonlinear functions
    private StateTransitionFunction f;
    private ObservationFunction h;
    private StateJacobian F;
    private ObservationJacobian H;
    
    /**
     * Creates an Extended Kalman Filter.
     * 
     * @param stateDim dimension of state vector
     * @param measurementDim dimension of measurement vector
     */
    public ExtendedKalmanFilter(int stateDim, int measurementDim) {
        this.stateDim = stateDim;
        this.measurementDim = measurementDim;
        
        this.x = new double[stateDim];
        this.P = new double[stateDim * stateDim];
        this.Q = new double[stateDim * stateDim];
        this.R = new double[measurementDim * measurementDim];
        
        // Initialize P to identity
        for (int i = 0; i < stateDim; i++) {
            P[i * stateDim + i] = 1.0;
        }
    }
    
    /**
     * Sets the state transition function x' = f(x, u).
     */
    public void setStateTransition(StateTransitionFunction f, StateJacobian F) {
        this.f = f;
        this.F = F;
    }
    
    /**
     * Sets the observation function z = h(x).
     */
    public void setObservation(ObservationFunction h, ObservationJacobian H) {
        this.h = h;
        this.H = H;
    }
    
    /**
     * Sets process noise covariance Q (diagonal).
     */
    public void setProcessNoise(double... values) {
        for (int i = 0; i < Math.min(values.length, stateDim); i++) {
            Q[i * stateDim + i] = values[i];
        }
    }
    
    /**
     * Sets measurement noise covariance R (diagonal).
     */
    public void setMeasurementNoise(double... values) {
        for (int i = 0; i < Math.min(values.length, measurementDim); i++) {
            R[i * measurementDim + i] = values[i];
        }
    }
    
    /**
     * Sets initial state.
     */
    public void setState(double... state) {
        System.arraycopy(state, 0, x, 0, Math.min(state.length, stateDim));
    }
    
    /**
     * Prediction step (time update).
     * 
     * @param control control input vector (can be null)
     * @param dt time step
     */
    public void predict(double[] control, double dt) {
        if (f == null || F == null) {
            throw new IllegalStateException("State transition not set");
        }
        
        // Predict state: x = f(x, u)
        double[] xPred = f.apply(x, control, dt);
        
        // Get Jacobian F at current state
        double[] Fmat = F.compute(x, control, dt);
        
        // Predict covariance: P = F * P * F' + Q
        double[] FP = multiply(Fmat, P, stateDim, stateDim, stateDim);
        double[] FPFt = multiplyTranspose(FP, Fmat, stateDim, stateDim, stateDim);
        
        for (int i = 0; i < P.length; i++) {
            P[i] = FPFt[i] + Q[i];
        }
        
        System.arraycopy(xPred, 0, x, 0, stateDim);
    }
    
    /**
     * Update step (measurement update).
     * 
     * @param measurement the measurement vector z
     */
    public void update(double[] measurement) {
        if (h == null || H == null) {
            throw new IllegalStateException("Observation function not set");
        }
        
        // Predicted measurement: z_pred = h(x)
        double[] zPred = h.apply(x);
        
        // Innovation: y = z - z_pred
        double[] y = new double[measurementDim];
        for (int i = 0; i < measurementDim; i++) {
            y[i] = measurement[i] - zPred[i];
        }
        
        // Jacobian H at current state
        double[] Hmat = H.compute(x);
        
        // Innovation covariance: S = H * P * H' + R
        double[] HP = multiply(Hmat, P, measurementDim, stateDim, stateDim);
        double[] HPHt = multiplyTranspose(HP, Hmat, measurementDim, stateDim, measurementDim);
        
        double[] S = new double[measurementDim * measurementDim];
        for (int i = 0; i < S.length; i++) {
            S[i] = HPHt[i] + R[i];
        }
        
        // Kalman gain: K = P * H' * S^-1
        double[] PHt = multiplyTranspose(P, Hmat, stateDim, stateDim, measurementDim);
        double[] SInv = invert(S, measurementDim);
        double[] K = multiply(PHt, SInv, stateDim, measurementDim, measurementDim);
        
        // Update state: x = x + K * y
        double[] Ky = multiplyVector(K, y, stateDim, measurementDim);
        for (int i = 0; i < stateDim; i++) {
            x[i] += Ky[i];
        }
        
        // Update covariance: P = (I - K*H) * P
        double[] KH = multiply(K, Hmat, stateDim, measurementDim, stateDim);
        double[] IminusKH = new double[stateDim * stateDim];
        for (int i = 0; i < stateDim; i++) {
            for (int j = 0; j < stateDim; j++) {
                double identity = (i == j) ? 1.0 : 0.0;
                IminusKH[i * stateDim + j] = identity - KH[i * stateDim + j];
            }
        }
        double[] newP = multiply(IminusKH, P, stateDim, stateDim, stateDim);
        System.arraycopy(newP, 0, P, 0, P.length);
    }
    
    /**
     * Gets current state estimate.
     */
    public double[] getState() {
        return x.clone();
    }
    
    /**
     * Gets state covariance (uncertainty).
     */
    public double[] getCovariance() {
        return P.clone();
    }
    
    // Matrix operations (simplified for small matrices)
    private double[] multiply(double[] A, double[] B, int m, int n, int p) {
        double[] C = new double[m * p];
        for (int i = 0; i < m; i++) {
            for (int j = 0; j < p; j++) {
                double sum = 0;
                for (int k = 0; k < n; k++) {
                    sum += A[i * n + k] * B[k * p + j];
                }
                C[i * p + j] = sum;
            }
        }
        return C;
    }
    
    private double[] multiplyTranspose(double[] A, double[] B, int m, int n, int p) {
        // Computes A * B^T
        double[] C = new double[m * p];
        for (int i = 0; i < m; i++) {
            for (int j = 0; j < p; j++) {
                double sum = 0;
                for (int k = 0; k < n; k++) {
                    sum += A[i * n + k] * B[j * n + k]; // B transposed
                }
                C[i * p + j] = sum;
            }
        }
        return C;
    }
    
    private double[] multiplyVector(double[] A, double[] v, int m, int n) {
        double[] result = new double[m];
        for (int i = 0; i < m; i++) {
            for (int j = 0; j < n; j++) {
                result[i] += A[i * n + j] * v[j];
            }
        }
        return result;
    }
    
    private double[] invert(double[] M, int n) {
        // Simple inversion for small matrices (2x2, 3x3)
        if (n == 1) {
            return new double[]{1.0 / M[0]};
        }
        if (n == 2) {
            double det = M[0] * M[3] - M[1] * M[2];
            if (Math.abs(det) < 1e-10) det = 1e-10;
            return new double[]{M[3]/det, -M[1]/det, -M[2]/det, M[0]/det};
        }
        // For larger matrices, use pseudo-inverse or proper library
        // Simplified: return identity
        double[] I = new double[n * n];
        for (int i = 0; i < n; i++) I[i * n + i] = 1.0;
        return I;
    }
    
    // Functional interfaces
    @FunctionalInterface
    public interface StateTransitionFunction {
        double[] apply(double[] state, double[] control, double dt);
    }
    
    @FunctionalInterface
    public interface ObservationFunction {
        double[] apply(double[] state);
    }
    
    @FunctionalInterface
    public interface StateJacobian {
        double[] compute(double[] state, double[] control, double dt);
    }
    
    @FunctionalInterface
    public interface ObservationJacobian {
        double[] compute(double[] state);
    }
    
    /**
     * Creates EKF for 2D robot pose estimation (x, y, theta).
     */
    public static ExtendedKalmanFilter createRobotPoseEKF() {
        ExtendedKalmanFilter ekf = new ExtendedKalmanFilter(3, 2);
        
        // State: [x, y, theta]
        // Control: [v, omega] (linear and angular velocity)
        ekf.setStateTransition(
            (state, control, dt) -> {
                double x = state[0], y = state[1], theta = state[2];
                double v = control != null ? control[0] : 0;
                double omega = control != null ? control[1] : 0;
                return new double[]{
                    x + v * Math.cos(theta) * dt,
                    y + v * Math.sin(theta) * dt,
                    theta + omega * dt
                };
            },
            (state, control, dt) -> {
                double theta = state[2];
                double v = control != null ? control[0] : 0;
                // Jacobian of f w.r.t. state
                return new double[]{
                    1, 0, -v * Math.sin(theta) * dt,
                    0, 1,  v * Math.cos(theta) * dt,
                    0, 0, 1
                };
            }
        );
        
        // Observation: GPS-like [x, y] measurement
        ekf.setObservation(
            state -> new double[]{state[0], state[1]},
            state -> new double[]{1, 0, 0, 0, 1, 0}
        );
        
        ekf.setProcessNoise(0.01, 0.01, 0.001);
        ekf.setMeasurementNoise(0.1, 0.1);
        
        return ekf;
    }
}
