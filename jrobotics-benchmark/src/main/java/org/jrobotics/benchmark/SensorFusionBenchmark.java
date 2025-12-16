/*
 * JRobotics v2 - World-Class General Purpose Robotics API
 * 
 * Copyright (c) 2025 Silvère Martin-Michiellot <silvere.martin@gmail.com>
 * Co-authored by Gemini AI Assistant
 * 
 * Licensed under the MIT License.
 * See LICENSE file in the project root for full license information.
 */
package org.jrobotics.benchmark;

import org.jrobotics.sensor.fusion.*;
import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.infra.Blackhole;

import java.util.concurrent.TimeUnit;

/**
 * JMH benchmarks for sensor fusion algorithms.
 * 
 * @author Silvère Martin-Michiellot
 * @author Gemini AI Assistant
 * @version 2.0.0
 * @since 2.0.0
 */
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@State(Scope.Thread)
@Warmup(iterations = 3, time = 1)
@Measurement(iterations = 5, time = 1)
@Fork(1)
public class SensorFusionBenchmark {
    
    private KalmanFilter1D kalman;
    private ComplementaryFilter complementary;
    private double measurement;
    private long timestamp;
    
    @Setup
    public void setup() {
        kalman = KalmanFilter1D.standard();
        complementary = ComplementaryFilter.standard();
        measurement = Math.random() * 10;
        timestamp = System.currentTimeMillis();
    }
    
    @Benchmark
    public void kalmanFilter(Blackhole bh) {
        double filtered = kalman.filter(measurement + Math.random() * 0.1);
        bh.consume(filtered);
    }
    
    @Benchmark
    public void kalmanPredictUpdate(Blackhole bh) {
        kalman.predict(0.01);
        kalman.update(measurement + Math.random() * 0.1);
        bh.consume(kalman.getEstimate());
    }
    
    @Benchmark
    public void complementaryFilterUpdate(Blackhole bh) {
        complementary.update(
            0.1, 0.2, 9.8,   // accel
            0.01, 0.02, 0.1, // gyro
            timestamp++
        );
        bh.consume(complementary.getOrientation());
    }
}
