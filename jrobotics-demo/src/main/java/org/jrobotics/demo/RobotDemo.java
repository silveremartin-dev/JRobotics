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

import org.jrobotics.actuator.motor.Motor;
import org.jrobotics.actuator.motor.MotorCommand;
import org.jrobotics.actuator.servo.Servo;
import org.jrobotics.actuator.servo.ServoCommand;
import org.jrobotics.robot.wheeled.DifferentialDriveRobot;
import org.jrobotics.sensor.imu.ImuData;
import org.jrobotics.sensor.imu.ImuSensor;
import org.jrobotics.sensor.range.LidarScan;
import org.jrobotics.sensor.range.LidarSensor;
import org.jrobotics.sensor.range.UltrasonicSensor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Locale;
import java.util.Optional;

/**
 * Demo application showcasing JRobotics API features.
 * 
 * <p>
 * This demo creates a simulated differential drive robot with sensors
 * and actuators, then demonstrates basic robot operations.
 * </p>
 * 
 * @author Silvère Martin-Michiellot
 * @author Gemini AI Assistant
 * @version 2.0.0
 */
public class RobotDemo {

    private static final Logger logger = LoggerFactory.getLogger(RobotDemo.class);

    public static void main(String[] args) {
        System.out.println("╔════════════════════════════════════════════════════════════════╗");
        System.out.println("║        JRobotics v2 - World-Class Robotics API Demo           ║");
        System.out.println("║                                                                ║");
        System.out.println("║  Copyright (c) 2025 Silvère Martin-Michiellot                 ║");
        System.out.println("║  Co-authored by Gemini AI Assistant                           ║");
        System.out.println("╚════════════════════════════════════════════════════════════════╝");
        System.out.println();

        try {
            runDemo();
        } catch (Exception e) {
            logger.error("Demo failed: {}", e.getMessage(), e);
            System.exit(1);
        }
    }

    private static void runDemo() throws Exception {
        // ============================================================
        // 1. Create Robot
        // ============================================================
        System.out.println("▶ Creating differential drive robot...");
        DifferentialDriveRobot robot = new DifferentialDriveRobot("demo-robot-1", "DemoBot");

        System.out.println("  ├─ ID: " + robot.getId());
        System.out.println("  ├─ Name: " + robot.getName());
        System.out.println("  └─ Capabilities: " + robot.getCapabilities());
        System.out.println();

        // ============================================================
        // 2. Add Sensors
        // ============================================================
        System.out.println("▶ Adding sensors...");

        // Ultrasonic distance sensor
        UltrasonicSensor frontSonar = new UltrasonicSensor("sonar-front", "Front Ultrasonic");
        robot.addComponent(frontSonar);
        System.out.println("  ├─ Added: " + frontSonar.getName());

        // IMU sensor
        ImuSensor imu = new ImuSensor("imu-main", "Main IMU");
        robot.addComponent(imu);
        System.out.println("  ├─ Added: " + imu.getName());

        // LIDAR sensor
        LidarSensor lidar = new LidarSensor("lidar-main", "Main LIDAR");
        robot.addComponent(lidar);
        System.out.println("  └─ Added: " + lidar.getName());
        System.out.println();

        // ============================================================
        // 3. Add Actuators
        // ============================================================
        System.out.println("▶ Adding actuators...");

        // Left and right drive motors
        Motor leftMotor = new Motor("motor-left", "Left Drive Motor");
        robot.addComponent(leftMotor);
        System.out.println("  ├─ Added: " + leftMotor.getName());

        Motor rightMotor = new Motor("motor-right", "Right Drive Motor");
        robot.addComponent(rightMotor);
        System.out.println("  ├─ Added: " + rightMotor.getName());

        // Camera pan servo
        Servo panServo = new Servo("servo-pan", "Camera Pan Servo");
        robot.addComponent(panServo);
        System.out.println("  └─ Added: " + panServo.getName());
        System.out.println();

        // ============================================================
        // 4. Initialize and Start Robot
        // ============================================================
        System.out.println("▶ Initializing robot...");
        robot.initialize();
        System.out.println("  └─ State: " + robot.getState());

        System.out.println("▶ Starting robot...");
        robot.start();
        System.out.println("  └─ State: " + robot.getState());
        System.out.println();

        // ============================================================
        // 5. Read Sensor Data
        // ============================================================
        System.out.println("▶ Reading sensor data...");

        // Read ultrasonic distance
        Optional<Double> distance = frontSonar.read();
        distance.ifPresent(d -> System.out.println("  ├─ Front sonar: " + String.format(Locale.US, "%.3f", d) + " m"));

        // Read IMU data
        Optional<ImuData> imuData = imu.read();
        imuData.ifPresent(data -> {
            System.out.println("  ├─ IMU Acceleration: [" +
                    String.format(Locale.US, "%.3f", data.accelX()) + ", " +
                    String.format(Locale.US, "%.3f", data.accelY()) + ", " +
                    String.format(Locale.US, "%.3f", data.accelZ()) + "] m/s²");
            System.out.println("  ├─ IMU Roll: " +
                    String.format(Locale.US, "%.2f", Math.toDegrees(data.getRoll())) + "°");
            System.out.println("  ├─ IMU Pitch: " +
                    String.format(Locale.US, "%.2f", Math.toDegrees(data.getPitch())) + "°");
        });

        // Read LIDAR scan
        Optional<LidarScan> scan = lidar.read();
        scan.ifPresent(s -> {
            System.out.println("  ├─ LIDAR points: " + s.getPointCount());
            if (s.findNearest() != null) {
                System.out.println("  └─ Nearest obstacle: " +
                        String.format(Locale.US, "%.3f", s.findNearest().distance()) + " m");
            }
        });
        System.out.println();

        // ============================================================
        // 6. Control Actuators
        // ============================================================
        System.out.println("▶ Controlling actuators...");

        // Set motor speeds
        leftMotor.execute(MotorCommand.speed(0.5));
        System.out.println("  ├─ Left motor: 50% forward");

        rightMotor.execute(MotorCommand.speed(0.5));
        System.out.println("  ├─ Right motor: 50% forward");

        // Move pan servo
        panServo.execute(ServoCommand.position(45.0));
        System.out.println("  └─ Pan servo: 45°");
        System.out.println();

        // ============================================================
        // 7. Simulate Robot Movement
        // ============================================================
        System.out.println("▶ Simulating robot movement...");

        robot.setWheelSpeeds(0.5, 0.5, 0.5); // Both wheels at 50%
        System.out.println("  ├─ Initial position: (" +
                String.format(Locale.US, "%.3f", robot.getX()) + ", " +
                String.format(Locale.US, "%.3f", robot.getY()) + ")");

        // Simulate 2 seconds of movement
        for (int i = 0; i < 20; i++) {
            robot.updateOdometry(0.1); // 100ms time steps
        }

        System.out.println("  └─ After 2s: (" +
                String.format(Locale.US, "%.3f", robot.getX()) + ", " +
                String.format(Locale.US, "%.3f", robot.getY()) + ")");
        System.out.println();

        // ============================================================
        // 8. Event System Demo
        // ============================================================
        System.out.println("▶ Event system demo...");
        robot.getEventBus().subscribe(org.jrobotics.core.Event.class, event -> {
            logger.debug("[{}] Received event: {}", System.currentTimeMillis(), event);
        });
        System.out.println("  └─ Subscribed to all events");
        System.out.println();

        // ============================================================
        // 9. Shutdown
        // ============================================================
        System.out.println("▶ Shutting down robot...");
        robot.stop();
        System.out.println("  ├─ State: " + robot.getState());

        robot.shutdown();
        System.out.println("  └─ State: " + robot.getState());
        System.out.println();

        System.out.println("════════════════════════════════════════════════════════════════");
        System.out.println("  ✓ Demo completed successfully!");
        System.out.println("════════════════════════════════════════════════════════════════");
    }
}
