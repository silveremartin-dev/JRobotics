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

import org.jrobotics.simulation.Vector3;
import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.infra.Blackhole;

import java.util.concurrent.TimeUnit;

/**
 * JMH benchmarks for Vector3 operations.
 * 
 * <p>Run with: mvn package -pl jrobotics-benchmark
 *    java -jar jrobotics-benchmark/target/benchmarks.jar</p>
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
public class Vector3Benchmark {
    
    private Vector3 v1;
    private Vector3 v2;
    
    @Setup
    public void setup() {
        v1 = new Vector3(1.0, 2.0, 3.0);
        v2 = new Vector3(4.0, 5.0, 6.0);
    }
    
    @Benchmark
    public void add(Blackhole bh) {
        bh.consume(v1.add(v2));
    }
    
    @Benchmark
    public void subtract(Blackhole bh) {
        bh.consume(v1.subtract(v2));
    }
    
    @Benchmark
    public void multiply(Blackhole bh) {
        bh.consume(v1.multiply(2.5));
    }
    
    @Benchmark
    public void dot(Blackhole bh) {
        bh.consume(v1.dot(v2));
    }
    
    @Benchmark
    public void cross(Blackhole bh) {
        bh.consume(v1.cross(v2));
    }
    
    @Benchmark
    public void magnitude(Blackhole bh) {
        bh.consume(v1.magnitude());
    }
    
    @Benchmark
    public void normalize(Blackhole bh) {
        bh.consume(v1.normalize());
    }
    
    @Benchmark
    public void distanceTo(Blackhole bh) {
        bh.consume(v1.distanceTo(v2));
    }
}
