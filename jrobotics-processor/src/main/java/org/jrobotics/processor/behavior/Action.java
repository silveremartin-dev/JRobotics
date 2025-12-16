/*
 * JRobotics v2 - World-Class General Purpose Robotics API
 * 
 * Copyright (c) 2025 Silvère Martin-Michiellot <silvere.martin@gmail.com>
 * Co-authored by Gemini AI Assistant
 * 
 * Licensed under the MIT License.
 * See LICENSE file in the project root for full license information.
 */
package org.jrobotics.processor.behavior;

import java.util.function.Function;

/**
 * Action node - performs an action with side effects.
 * 
 * @author Silvère Martin-Michiellot
 * @author Gemini AI Assistant
 * @version 2.0.0
 * @since 2.0.0
 */
public class Action implements BehaviorNode {
    
    private final String name;
    private final Function<BehaviorContext, Status> action;
    
    public Action(String name, Function<BehaviorContext, Status> action) {
        this.name = name;
        this.action = action;
    }
    
    /**
     * Creates an action that always succeeds.
     * 
     * @param name the action name
     * @param action the action to run
     * @return the action node
     */
    public static Action success(String name, java.util.function.Consumer<BehaviorContext> action) {
        return new Action(name, ctx -> {
            action.accept(ctx);
            return Status.SUCCESS;
        });
    }
    
    @Override
    public String getName() {
        return name;
    }
    
    @Override
    public Status execute(BehaviorContext context) {
        return action.apply(context);
    }
    
    @Override
    public void reset() {
        // Stateless
    }
}
