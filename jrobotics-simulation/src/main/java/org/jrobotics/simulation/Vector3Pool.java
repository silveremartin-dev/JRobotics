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

import java.util.concurrent.ArrayBlockingQueue;

/**
 * Object pool for Vector3 to reduce garbage collection pressure.
 * 
 * <p>Provides thread-safe pooling for hot paths in physics simulation.</p>
 * 
 * <p><b>Usage:</b></p>
 * <pre>{@code
 * Vector3 v = Vector3Pool.acquire();
 * try {
 *     // use v
 * } finally {
 *     Vector3Pool.release(v);
 * }
 * }</pre>
 * 
 * @author Silvère Martin-Michiellot
 * @author Gemini AI Assistant
 * @version 2.0.0
 * @since 2.0.0
 */
public final class Vector3Pool {
    
    private static final int DEFAULT_POOL_SIZE = 256;
    private static final ArrayBlockingQueue<MutableVector3> pool = 
        new ArrayBlockingQueue<>(DEFAULT_POOL_SIZE);
    
    // Pre-populate pool
    static {
        for (int i = 0; i < DEFAULT_POOL_SIZE / 2; i++) {
            pool.offer(new MutableVector3());
        }
    }
    
    private Vector3Pool() {}
    
    /**
     * Acquires a vector from the pool.
     * 
     * @return a pooled or new vector (zeroed)
     */
    public static MutableVector3 acquire() {
        MutableVector3 v = pool.poll();
        if (v == null) {
            v = new MutableVector3();
        } else {
            v.set(0, 0, 0);
        }
        return v;
    }
    
    /**
     * Acquires and sets a vector from the pool.
     */
    public static MutableVector3 acquire(double x, double y, double z) {
        MutableVector3 v = pool.poll();
        if (v == null) {
            v = new MutableVector3();
        }
        v.set(x, y, z);
        return v;
    }
    
    /**
     * Releases a vector back to the pool.
     */
    public static void release(MutableVector3 v) {
        if (v != null) {
            pool.offer(v);
        }
    }
    
    /**
     * Gets current pool size.
     */
    public static int poolSize() {
        return pool.size();
    }
    
    /**
     * Mutable vector for pooling.
     */
    public static final class MutableVector3 {
        private double x, y, z;
        
        public MutableVector3() {}
        
        public MutableVector3 set(double x, double y, double z) {
            this.x = x;
            this.y = y;
            this.z = z;
            return this;
        }
        
        public double x() { return x; }
        public double y() { return y; }
        public double z() { return z; }
        
        public MutableVector3 add(MutableVector3 o) {
            this.x += o.x;
            this.y += o.y;
            this.z += o.z;
            return this;
        }
        
        public MutableVector3 add(double ox, double oy, double oz) {
            this.x += ox;
            this.y += oy;
            this.z += oz;
            return this;
        }
        
        public MutableVector3 subtract(MutableVector3 o) {
            this.x -= o.x;
            this.y -= o.y;
            this.z -= o.z;
            return this;
        }
        
        public MutableVector3 multiply(double s) {
            this.x *= s;
            this.y *= s;
            this.z *= s;
            return this;
        }
        
        public double magnitude() {
            return Math.sqrt(x*x + y*y + z*z);
        }
        
        public MutableVector3 normalize() {
            double mag = magnitude();
            if (mag > 1e-10) {
                multiply(1.0 / mag);
            }
            return this;
        }
        
        public double dot(MutableVector3 o) {
            return x*o.x + y*o.y + z*o.z;
        }
        
        public double distanceTo(MutableVector3 o) {
            double dx = o.x - x;
            double dy = o.y - y;
            double dz = o.z - z;
            return Math.sqrt(dx*dx + dy*dy + dz*dz);
        }
        
        /**
         * Converts to immutable Vector3.
         */
        public Vector3 toVector3() {
            return new Vector3(x, y, z);
        }
        
        /**
         * Copies from immutable Vector3.
         */
        public MutableVector3 from(Vector3 v) {
            this.x = v.x();
            this.y = v.y();
            this.z = v.z();
            return this;
        }
    }
}
