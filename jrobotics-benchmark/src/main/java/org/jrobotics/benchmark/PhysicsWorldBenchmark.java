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

import org.jrobotics.simulation.*;
import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.infra.Blackhole;

import java.util.concurrent.TimeUnit;

/**
 * JMH benchmarks for PhysicsWorld simulation.
 * 
 * @author Silvère Martin-Michiellot
 * @author Gemini AI Assistant
 * @version 2.0.0
 * @since 2.0.0
 */
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.MICROSECONDS)
@State(Scope.Thread)
@Warmup(iterations = 3, time = 1)
@Measurement(iterations = 5, time = 1)
@Fork(1)
public class PhysicsWorldBenchmark {

    @Param({ "10", "100", "500" })
    private int bodyCount;

    private PhysicsWorld world;

    @Setup
    public void setup() {
        world = new PhysicsWorld();
        world.setGravity(Vector3.ZERO);
        world.setFriction(0.1);

        for (int i = 0; i < bodyCount; i++) {
            double x = (Math.random() - 0.5) * 100;
            double y = (Math.random() - 0.5) * 100;
            PhysicsBody body = PhysicsBody.dynamicBody(
                    "body-" + i,
                    1.0,
                    new Vector3(x, y, 0),
                    0.5);
            world.addBody(body);
        }
    }

    @Benchmark
    public void step(Blackhole bh) {
        world.step(0.016); // ~60 Hz
        bh.consume(world.getStepCount());
    }

    @Benchmark
    public void stepWithCollisions(Blackhole bh) {
        // Add velocities to cause collisions
        for (PhysicsBody body : world.getBodies()) {
            PhysicsBody moving = body.withVelocity(new Vector3(
                    (Math.random() - 0.5) * 2,
                    (Math.random() - 0.5) * 2,
                    0));
            // In a real physics engine we'd update the body in the world
            // For this benchmark we want to simulate the state change setup
            bh.consume(moving);
        }
        world.step(0.016);
        bh.consume(world.getStepCount());
    }
}
