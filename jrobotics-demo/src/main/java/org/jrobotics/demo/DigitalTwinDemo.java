package org.jrobotics.demo;

import org.jrobotics.bridge.cloud.DigitalTwinAgent;
import org.jrobotics.core.LifecycleException;
import org.jrobotics.robot.wheeled.DifferentialDriveRobot;
import org.jrobotics.simulation.Vector3;
import org.jrobotics.sensor.vision.CameraSensor;
import org.jrobotics.sensor.vision.OpenCVCamera;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Demo application showcasing Digital Twin integration and Computer Vision.
 * 
 * <p>
 * This demo:
 * 1. Initializes a Robot running a Digital Twin connection (AWS IoT).
 * 2. Starts a Camera Sensor (Native OpenCV).
 * 3. Simulates movement and updates the cloud shadow.
 * </p>
 * 
 * @author Silvère Martin-Michiellot
 * @author Gemini AI Assistant
 * @version 2.5.0
 */
public class DigitalTwinDemo {

    private static final Logger logger = LoggerFactory.getLogger(DigitalTwinDemo.class);

    public static void main(String[] args) {
        logger.info("==========================================");
        logger.info("   JRobotics v2.5 - Digital Twin Demo     ");
        logger.info("==========================================");

        try {
            // 1. Setup Robot
            DifferentialDriveRobot robot = new DifferentialDriveRobot("robot-1", "MyRobot");
            logger.info("Robot created: {}", robot.getName());
            robot.initialize();
            robot.start();

            // 2. Setup Camera (OpenCV)
            // By default, device 0 is web cam. We catch exception if no camera is present.
            CameraSensor camera = null;
            try {
                logger.info("Initializing Computer Vision (OpenCV)...");
                camera = new OpenCVCamera("cam-1", 0);
                camera.initialize();
                camera.start();
                logger.info("Camera started successfully. Resolution: {}x{}", camera.getWidth(), camera.getHeight());
                
                // Add camera to robot (if supported, or just manage conceptually)
                robot.addComponent(camera);
            } catch (Exception e) {
                logger.warn("Camera initialization failed (is a webcam connected?). Continuing without vision. Error: {}", e.getMessage());
            }

            // 3. Setup Digital Twin Agent
            // Using placeholder files - the agent will detect this and run in Mock Mode.
            logger.info("Initializing AWS IoT Digital Twin Bridge...");
            DigitalTwinAgent cloudAgent = new DigitalTwinAgent(
                "ssl://a3xxx.iot.us-east-1.amazonaws.com:8883",
                "robot-001-client",
                "JRoboticsThing",
                "path/to/cert.pem",
                "path/to/private.key"
            );
            
            cloudAgent.initialize();
            cloudAgent.start();
            // cloudAgent is not a Component, so we manage it separately

            // 4. Simulation Loop
            logger.info("Starting Simulation Loop (Press Ctrl+C to stop)...");
            
            Vector3 position = new Vector3(0, 0, 0);
            Vector3 velocity = new Vector3(0.5, 0.2, 0);
            double battery = 100.0;

            for (int i = 0; i < 20; i++) { // Run for 20 frames
                // Update Physics
                position = position.add(velocity);
                battery -= 0.1;
                
                // Construct Shadow JSON
                String shadowState = String.format(
                    "{\"state\":{\"reported\":{\"battery\":%.1f, \"location\":{\"x\":%.2f, \"y\":%.2f}}}}",
                    battery, position.x(), position.y()
                );
                
                // Sync to Cloud
                cloudAgent.updateShadow(shadowState);
                
                // Grab Frame if camera available
                if (camera != null && camera.isRunning()) {
                    // Just read to keep buffer fresh
                    try {
                        camera.read(); 
                    } catch (Exception e) {
                        logger.warn("Failed to capture snapshot");
                    }
                }

                Thread.sleep(1000); // 1Hz update for demo
            }

            logger.info("Demo sequence completed.");
            
            // Cleanup
            robot.shutdown(); // Should shutdown components too
            cloudAgent.shutdown();

        } catch (Exception e) {
            logger.error("Critical error in demo", e);
        }
    }
}
