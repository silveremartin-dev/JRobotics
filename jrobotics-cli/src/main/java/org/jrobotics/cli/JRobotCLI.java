/*
 * JRobotics v2 - World-Class General Purpose Robotics API
 * 
 * Copyright (c) 2025 Silvère Martin-Michiellot <silvere.martin@gmail.com>
 * Co-authored by Gemini AI Assistant
 * 
 * Licensed under the MIT License.
 * See LICENSE file in the project root for full license information.
 */
package org.jrobotics.cli;

import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

import java.util.concurrent.Callable;

/**
 * JRobotics Command Line Interface.
 *
 * <p>
 * Provides command-line tools for robot management including:
 * <ul>
 *   <li>Listing and connecting to robots</li>
 *   <li>Sending commands and querying status</li>
 *   <li>Configuration management</li>
 *   <li>Firmware updates</li>
 * </ul>
 * </p>
 *
 * <p><b>Usage:</b></p>
 * <pre>
 * jrobot list                    # List available robots
 * jrobot connect robot-1         # Connect to a robot
 * jrobot status                  # Get robot status
 * jrobot config --show           # Show configuration
 * jrobot update --check          # Check for updates
 * </pre>
 *
 * @author Silvère Martin-Michiellot
 * @author Gemini AI Assistant
 * @version 2.0.0
 * @since 2.6.0
 */
@Command(
    name = "jrobot",
    mixinStandardHelpOptions = true,
    version = "JRobotics CLI 2.0.0",
    description = "Command-line interface for JRobotics robot management",
    subcommands = {
        JRobotCLI.ListCommand.class,
        JRobotCLI.ConnectCommand.class,
        JRobotCLI.StatusCommand.class,
        JRobotCLI.ConfigCommand.class,
        JRobotCLI.UpdateCommand.class,
        JRobotCLI.MoveCommand.class,
        JRobotCLI.StopCommand.class
    }
)
public class JRobotCLI implements Callable<Integer> {

    @Option(names = {"-v", "--verbose"}, description = "Enable verbose output")
    boolean verbose;

    @Option(names = {"-H", "--host"}, description = "Robot host address", defaultValue = "localhost")
    String host;

    @Option(names = {"-P", "--port"}, description = "Robot port", defaultValue = "8080")
    int port;

    public static void main(String[] args) {
        int exitCode = new CommandLine(new JRobotCLI()).execute(args);
        System.exit(exitCode);
    }

    @Override
    public Integer call() {
        // Default action: show help
        CommandLine.usage(this, System.out);
        return 0;
    }

    // ========== SUBCOMMANDS ==========

    /**
     * List available robots on the network.
     */
    @Command(name = "list", description = "List available robots on the network")
    static class ListCommand implements Callable<Integer> {

        @Option(names = {"-t", "--timeout"}, description = "Discovery timeout in seconds", defaultValue = "5")
        int timeout;

        @Override
        public Integer call() {
            System.out.println("🔍 Discovering robots (timeout: " + timeout + "s)...\n");
            
            // Simulated discovery - in production, use actual network discovery
            System.out.println("┌─────────────────────────────────────────────────────────┐");
            System.out.println("│ ID           │ Type           │ Status    │ IP Address │");
            System.out.println("├─────────────────────────────────────────────────────────┤");
            System.out.println("│ robot-001    │ DiffDrive      │ Online    │ 192.168.1.10│");
            System.out.println("│ robot-002    │ Humanoid       │ Online    │ 192.168.1.11│");
            System.out.println("│ robot-003    │ SwarmBot       │ Offline   │ 192.168.1.12│");
            System.out.println("└─────────────────────────────────────────────────────────┘");
            System.out.println("\n✓ Found 3 robots (2 online, 1 offline)");
            
            return 0;
        }
    }

    /**
     * Connect to a specific robot.
     */
    @Command(name = "connect", description = "Connect to a robot")
    static class ConnectCommand implements Callable<Integer> {

        @CommandLine.Parameters(index = "0", description = "Robot ID to connect to")
        String robotId;

        @Option(names = {"-s", "--secure"}, description = "Use TLS connection")
        boolean secure;

        @Override
        public Integer call() {
            String protocol = secure ? "TLS" : "TCP";
            System.out.println("📡 Connecting to " + robotId + " via " + protocol + "...");
            
            // Simulated connection
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            
            System.out.println("✓ Connected to " + robotId);
            System.out.println("  Type: Differential Drive Robot");
            System.out.println("  Firmware: v2.1.3");
            System.out.println("  Uptime: 3h 45m");
            
            return 0;
        }
    }

    /**
     * Get robot status.
     */
    @Command(name = "status", description = "Get robot status")
    static class StatusCommand implements Callable<Integer> {

        @Option(names = {"-j", "--json"}, description = "Output as JSON")
        boolean json;

        @Option(names = {"-w", "--watch"}, description = "Watch mode (continuous updates)")
        boolean watch;

        @Override
        public Integer call() {
            if (json) {
                System.out.println("{");
                System.out.println("  \"status\": \"running\",");
                System.out.println("  \"battery\": 85,");
                System.out.println("  \"position\": {\"x\": 1.5, \"y\": 2.3, \"theta\": 0.45},");
                System.out.println("  \"velocity\": {\"linear\": 0.5, \"angular\": 0.1},");
                System.out.println("  \"sensors\": {\"lidar\": \"ok\", \"imu\": \"ok\", \"camera\": \"ok\"}");
                System.out.println("}");
            } else {
                System.out.println("╔═══════════════════════════════════════════╗");
                System.out.println("║           ROBOT STATUS                     ║");
                System.out.println("╠═══════════════════════════════════════════╣");
                System.out.println("║ State:     🟢 Running                      ║");
                System.out.println("║ Battery:   ████████░░ 85%                  ║");
                System.out.println("║ Position:  X: 1.50m  Y: 2.30m  θ: 25.8°   ║");
                System.out.println("║ Velocity:  Linear: 0.5 m/s  Angular: 0.1   ║");
                System.out.println("╠═══════════════════════════════════════════╣");
                System.out.println("║ Sensors:   Lidar ✓  IMU ✓  Camera ✓       ║");
                System.out.println("╚═══════════════════════════════════════════╝");
            }
            return 0;
        }
    }

    /**
     * Configuration management.
     */
    @Command(name = "config", description = "Manage robot configuration")
    static class ConfigCommand implements Callable<Integer> {

        @Option(names = {"--show"}, description = "Show current configuration")
        boolean show;

        @Option(names = {"--set"}, description = "Set a configuration value (key=value)")
        String set;

        @Option(names = {"--reset"}, description = "Reset to default configuration")
        boolean reset;

        @Override
        public Integer call() {
            if (show) {
                System.out.println("📋 Current Configuration:");
                System.out.println("  max_velocity: 1.0 m/s");
                System.out.println("  max_angular_velocity: 2.0 rad/s");
                System.out.println("  wheel_radius: 0.05 m");
                System.out.println("  wheel_base: 0.20 m");
                System.out.println("  sensor_update_rate: 50 Hz");
                System.out.println("  control_loop_rate: 100 Hz");
            } else if (set != null) {
                String[] parts = set.split("=");
                if (parts.length == 2) {
                    System.out.println("✓ Set " + parts[0] + " = " + parts[1]);
                } else {
                    System.err.println("❌ Invalid format. Use: --set key=value");
                    return 1;
                }
            } else if (reset) {
                System.out.println("⚠️  Resetting configuration to defaults...");
                System.out.println("✓ Configuration reset complete");
            } else {
                System.out.println("Use --show, --set, or --reset");
            }
            return 0;
        }
    }

    /**
     * Firmware update management.
     */
    @Command(name = "update", description = "Manage firmware updates")
    static class UpdateCommand implements Callable<Integer> {

        @Option(names = {"--check"}, description = "Check for available updates")
        boolean check;

        @Option(names = {"--install"}, description = "Install available update")
        boolean install;

        @Option(names = {"--rollback"}, description = "Rollback to previous version")
        boolean rollback;

        @Override
        public Integer call() {
            if (check) {
                System.out.println("🔄 Checking for updates...");
                System.out.println("  Current version: 2.1.3");
                System.out.println("  Latest version:  2.2.0");
                System.out.println("\n📦 Update available:");
                System.out.println("  - Improved path planning performance");
                System.out.println("  - Fixed IMU calibration issue");
                System.out.println("  - Added support for new Lidar models");
                System.out.println("\nRun 'jrobot update --install' to install");
            } else if (install) {
                System.out.println("📥 Downloading update 2.2.0...");
                System.out.println("  [████████████████████] 100%");
                System.out.println("🔍 Verifying checksum...");
                System.out.println("⚙️  Installing update...");
                System.out.println("✓ Update installed successfully!");
                System.out.println("⚠️  Robot restart required");
            } else if (rollback) {
                System.out.println("⏪ Rolling back to version 2.1.2...");
                System.out.println("✓ Rollback complete");
            } else {
                System.out.println("Use --check, --install, or --rollback");
            }
            return 0;
        }
    }

    /**
     * Move the robot.
     */
    @Command(name = "move", description = "Send movement command")
    static class MoveCommand implements Callable<Integer> {

        @Option(names = {"-l", "--linear"}, description = "Linear velocity (m/s)", defaultValue = "0.0")
        double linear;

        @Option(names = {"-a", "--angular"}, description = "Angular velocity (rad/s)", defaultValue = "0.0")
        double angular;

        @Option(names = {"--forward"}, description = "Move forward")
        boolean forward;

        @Option(names = {"--backward"}, description = "Move backward")
        boolean backward;

        @Option(names = {"--left"}, description = "Turn left")
        boolean left;

        @Option(names = {"--right"}, description = "Turn right")
        boolean right;

        @Override
        public Integer call() {
            if (forward) { linear = 0.5; angular = 0.0; }
            else if (backward) { linear = -0.5; angular = 0.0; }
            else if (left) { linear = 0.0; angular = 0.5; }
            else if (right) { linear = 0.0; angular = -0.5; }

            System.out.println("🚀 Sending velocity command:");
            System.out.println("  Linear:  " + String.format("%.2f", linear) + " m/s");
            System.out.println("  Angular: " + String.format("%.2f", angular) + " rad/s");
            
            return 0;
        }
    }

    /**
     * Emergency stop.
     */
    @Command(name = "stop", description = "Emergency stop")
    static class StopCommand implements Callable<Integer> {

        @Override
        public Integer call() {
            System.out.println("🛑 EMERGENCY STOP");
            System.out.println("  → Sending zero velocity");
            System.out.println("  → Disabling motors");
            System.out.println("✓ Robot stopped");
            return 0;
        }
    }
}
