/*
 * JRobotics v2 - World-Class General Purpose Robotics API
 * 
 * Copyright (c) 2025 Silvère Martin-Michiellot <silvere.martin@gmail.com>
 * Co-authored by Gemini AI Assistant
 * 
 * Licensed under the MIT License.
 * See LICENSE file in the project root for full license information.
 */
package org.jrobotics.demo;

import org.jrobotics.network.dashboard.DashboardServer;
import org.jrobotics.robot.wheeled.DifferentialDriveRobot;

/**
 * Dashboard demo showing a simulated robot with visual telemetry.
 */
public class DashboardDemo {
    
    public static void main(String[] args) throws InterruptedException {
        System.out.println("Starting Dashboard Demo...");
        
        // Create 3 simulated robots
        DifferentialDriveRobot robot1 = new DifferentialDriveRobot("bot-1", "AlphaBot");
        DifferentialDriveRobot robot2 = new DifferentialDriveRobot("bot-2", "BetaBot");
        DifferentialDriveRobot robot3 = new DifferentialDriveRobot("bot-3", "GammaBot");
        
        // Start dashboard
        // HTTP port 8080, WS port 8081
        DashboardServer dashboard = new DashboardServer(8080);
        
        dashboard.monitor(robot1);
        dashboard.monitor(robot2);
        dashboard.monitor(robot3);
        
        dashboard.start();
        
        System.out.println("Dashboard running at http://localhost:8080");
        System.out.println("Press Ctrl+C to stop");
        
        // Simulation loop
        long lastTime = System.nanoTime();
        double time = 0;
        
        while (true) {
            long now = System.nanoTime();
            double dt = (now - lastTime) / 1e9;
            lastTime = now;
            time += dt;
            
            // Move robots in patterns
            
            // Robot 1: Circle
            robot1.setVelocity(0.5, 0.5); // v=0.5 m/s, w=0.5 rad/s -> r=1m
            robot1.updateOdometry(dt);
            
            // Robot 2: Figure 8
            robot2.setVelocity(0.8, Math.sin(time * 0.5));
            robot2.updateOdometry(dt);
            
            // Robot 3: Square-ish (straight then turn)
            double tMod = time % 8.0;
            if (tMod < 2.0) robot3.setVelocity(0.5, 0);       // Move
            else if (tMod < 4.0) robot3.setVelocity(0, 1.57); // Turn
            else if (tMod < 6.0) robot3.setVelocity(0.5, 0);  // Move
            else robot3.setVelocity(0, 1.57);                 // Turn
            robot3.updateOdometry(dt);
            
            Thread.sleep(50); // 20Hz sim
        }
    }
}
