# JRobotics v2

[![Build Status](https://github.com/silvere-martin/jrobotics/workflows/CI/badge.svg)](https://github.com/silvere-martin/jrobotics/actions)
[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](https://opensource.org/licenses/MIT)
[![Java Version](https://img.shields.io/badge/Java-21%2B-blue)](https://openjdk.org/)

**World-Class General Purpose Robotics API for Java**

JRobotics is a comprehensive, modular robotics framework designed for hobbyists and professionals alike. It provides a complete toolkit for building, simulating, and controlling robots with an AI-friendly API.

## Features

### 🤖 Core Capabilities

- **Modular Architecture** - Use only what you need
- **AI-Friendly API** - Fluent builders, clear abstractions, easy integration
- **Hardware Abstraction Layer** - Platform-independent hardware access
- **Full Lifecycle Management** - Initialize, start, stop, destroy patterns

### 📡 Sensors

- Vision (cameras, depth sensors)
- Range (LIDAR, ultrasonic, IR)
- IMU (accelerometer, gyroscope, magnetometer)
- Environmental (temperature, humidity, pressure)
- Encoder/odometry, GPS/GNSS
- Sensor fusion framework

### 🧠 Processors / AI

- Path planning (A*, RRT, Dijkstra)
- SLAM integration
- Behavior trees & FSM
- Decision frameworks
- ML/AI integration points

### ⚙️ Actuators

- Motors (DC, stepper, brushless)
- Servos (position, continuous)
- PID control loops
- Motion profiles

### 🌍 Simulation

- Physics engine integration
- Virtual robot support
- Environment modeling
- Hybrid real+virtual scenarios
- 3D visualization

### 🌐 Networking

- Peer-to-peer communication
- Server-mediated protocols
- Remote teleoperation
- Robot swarm coordination
- Secure communication (TLS)

### 🔌 Bridges

- ROS2 integration
- MQTT support
- Extensible protocol framework

## Quick Start

### Prerequisites

- Java 21 or higher
- Maven 3.9+

### Installation

```bash
git clone https://github.com/silvere-martin/jrobotics.git
cd jrobotics
mvn clean install
```

### Basic Usage

```java
import org.jrobotics.api.RoboticsAPI;
import org.jrobotics.robot.WheeledRobot;

public class MyRobot {
    public static void main(String[] args) {
        // Create a simple wheeled robot
        var robot = RoboticsAPI.builder()
            .withRobot(new WheeledRobot("MyBot"))
            .withSensor(Sensors.ultrasonic("front", 0.0, 1.0, 0.0))
            .withSensor(Sensors.imu("body"))
            .withActuator(Actuators.motor("leftWheel"))
            .withActuator(Actuators.motor("rightWheel"))
            .build();
        
        // Start the robot
        robot.start();
        
        // Move forward
        robot.move(1.0, 0.0);
        
        // Read sensor data
        double distance = robot.getSensor("front").read();
        
        // Stop when obstacle detected
        if (distance < 0.3) {
            robot.stop();
        }
    }
}
```

## Project Structure

```
jrobotics/
├── jrobotics-core        # Core interfaces and utilities
├── jrobotics-hal         # Hardware Abstraction Layer
├── jrobotics-sensor      # Sensor implementations
├── jrobotics-actuator    # Actuator implementations
├── jrobotics-processor   # AI/Reasoning processors
├── jrobotics-simulation  # Physics simulation
├── jrobotics-network     # Networking protocols
├── jrobotics-robot       # Robot implementations
├── jrobotics-bridge      # External framework bridges
├── jrobotics-benchmark   # Performance benchmarks
├── demo/                 # Demo applications
└── scripts/              # Launch and utility scripts
```

## Documentation

- [Architecture Overview](architecture.md)
- [API Reference](javadoc/)
- [Getting Started Guide](docs/getting-started.md)
- [Examples](demo/)

## Building

```bash
# Build all modules
mvn clean install

# Skip tests
mvn clean install -DskipTests

# Generate JavaDoc
mvn javadoc:aggregate

# Run benchmarks
mvn -pl jrobotics-benchmark exec:java
```

## Contributing

Contributions are welcome! Please read our contributing guidelines before submitting pull requests.

## License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

## Authors

- **Silvère Martin-Michiellot** - Lead Developer - <silvere.martin@gmail.com>
- **Gemini AI Assistant** - Co-Developer

## Acknowledgments

- jMonkeyEngine team for physics engine
- All open-source contributors whose work made this possible

---

*JRobotics v2 - Making robotics accessible to everyone*
