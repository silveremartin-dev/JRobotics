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

import org.jrobotics.processor.behavior.*;
import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.infra.Blackhole;

import java.util.concurrent.TimeUnit;

/**
 * JMH benchmarks for Behavior Tree execution.
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
public class BehaviorTreeBenchmark {
    
    private BehaviorTree simpleTree;
    private BehaviorTree complexTree;
    private BehaviorContext context;
    
    @Setup
    public void setup() {
        context = new BehaviorContext();
        
        // Simple tree: 3 actions in sequence
        simpleTree = new BehaviorTree("simple",
            new Sequence("main")
                .addChild(Action.success("action1", ctx -> ctx.set("a", 1)))
                .addChild(Action.success("action2", ctx -> ctx.set("b", 2)))
                .addChild(Action.success("action3", ctx -> ctx.set("c", 3)))
        );
        
        // Complex tree: nested selectors and sequences
        complexTree = new BehaviorTree("complex",
            new Selector("root")
                .addChild(new Sequence("branch1")
                    .addChild(new Condition("check1", ctx -> ctx.get("flag", false)))
                    .addChild(Action.success("act1", ctx -> {}))
                )
                .addChild(new Sequence("branch2")
                    .addChild(new Condition("check2", ctx -> ctx.get("count", 0) > 5))
                    .addChild(new Selector("inner")
                        .addChild(Action.success("act2a", ctx -> {}))
                        .addChild(Action.success("act2b", ctx -> {}))
                    )
                )
                .addChild(new Sequence("branch3")
                    .addChild(Action.success("fallback1", ctx -> ctx.set("count", ctx.get("count", 0) + 1)))
                    .addChild(Action.success("fallback2", ctx -> {}))
                )
        );
    }
    
    @Benchmark
    public void simpleTreeTick(Blackhole bh) {
        simpleTree.reset();
        bh.consume(simpleTree.tick());
    }
    
    @Benchmark
    public void complexTreeTick(Blackhole bh) {
        complexTree.reset();
        bh.consume(complexTree.tick());
    }
    
    @Benchmark
    public void contextGet(Blackhole bh) {
        context.set("test", 42);
        bh.consume(context.get("test"));
    }
    
    @Benchmark
    public void conditionCheck(Blackhole bh) {
        context.set("value", Math.random() * 100);
        Condition cond = new Condition("bench", ctx -> ctx.<Double>get("value") > 50);
        bh.consume(cond.execute(context));
    }
}
