# JRobotics v2

[![Build Status](https://github.com/silvere-martin/jrobotics/workflows/CI/badge.svg)](https://github.com/silvere-martin/jrobotics/actions)
[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](https://opensource.org/licenses/MIT)
[![Java Version](https://img.shields.io/badge/Java-21%2B-blue)](https://openjdk.org/)

**World-Class General Purpose Robotics API for Java**

JRobotics is a comprehensive, modular robotics framework designed for hobbyists and professionals alike. It provides a complete toolkit for building, simulating, and controlling robots with an AI-friendly API.

## Project Statistics

| Metric | Value |
|--------|-------|
| Modules | 11 |
| Java Classes | 95+ |
| Unit Tests | 70+ |
| Git Commits | 11 |
| Lines of Code | ~10,000 |

## Features

### 🤖 Core Capabilities

- **Modular Architecture** - Use only what you need
- **AI-Friendly API** - Fluent builders, clear abstractions
- **Hardware Abstraction Layer** - Platform-independent hardware access
- **Full Lifecycle Management** - Initialize, start, stop, destroy patterns
- **Thread Pools** - Specialized executors for sensors, control, background

### 📡 Sensors

- Vision (cameras, depth sensors)
- Range (LIDAR, ultrasonic, IR)
- IMU (accelerometer, gyroscope, magnetometer)
- Environmental (temperature, humidity, pressure)
- Encoder/odometry, GPS/GNSS
- **Sensor Fusion** - Kalman Filter (1D + Extended), Complementary Filter

### 🧠 Processors / AI

- **Path Planning** - A* grid-based algorithm
- **Navigation** - GoToGoal reactive control
- **Behavior Trees** - Sequence, Selector, Condition, Action nodes
- **Decision Frameworks** - FSM patterns
- **Deep Learning** - DJL integration (PyTorch/TensorFlow) for Inference
- **SLAM** - FastSLAM 2.0 and Occupancy Grid Mapping
- **Reinforcement Learning** - DQN (Deep Q-Network) and PPO Agents
- **Computer Vision** - Object Detection (YOLO/SSD)

### 🐝 Swarm & Distributed

- **Swarm Intelligence** - Boids flocking, Leader selection
- **Distributed Shared Memory** - Synchronization across robots
- **Federated Learning** - Distributed model training (FedAvg)
- **Centralized Control** - Hive Mind patterns

### 🛡️ Security & OTA

- **Secure Transport** - TLS/SSL encryption
- **Access Control** - RBAC (Role-Based Access Control)
- **OTA Updates** - Remote firmware management and rollback

### ⚙️ Actuators

- Motors (DC, stepper, brushless)
- Servos (position, continuous)
- PID control loops
- Motion profiles

### 🌍 Simulation

- **Physics Engine** - Vector3, PhysicsBody, PhysicsWorld
- **Environment Builder** - Arena creation with obstacles
- **Visualization** - Console (ASCII), Swing2D, JavaFX2D, JMonkeyEngine 3D
- **Memory Efficient** - Vector3Pool for hot paths

### 🌐 Networking

- Peer-to-peer UDP communication
- Server-mediated TCP protocols
- Remote teleoperation (Master/Slave)
- Web Dashboard
- Pluggable NetworkTransport interface
- [Networking Comparison](docs/NETWORKING.md)

### 🔌 Bridges & Hardware

- ROS2 integration (stub, ready for rcljava)
- **Robot Adapters**: TurtleBot3, iRobot Create3, Arduino, Raspberry Pi
- Extensible protocol framework

## Quick Start

### Prerequisites

- Java 21 or higher
- Maven 3.9+
- (Optional) Docker for Swarm simulation

### Installation

```bash
git clone https://github.com/silvere-martin/jrobotics.git
cd jrobotics
mvn clean install
```

### Run Demos

```bash
# Console simulation (ASCII)
./run-visual-demo.bat     # Windows
./run-visual-demo.sh      # Linux/macOS

# 3D JMonkeyEngine simulation
./run-3d-demo.bat         # Windows
./run-3d-demo.sh          # Linux/macOS

# CLI Tool
java -jar jrobotics-cli/target/jrobotics-cli.jar --help

# Visual Editor
java -jar jrobotics-editor/target/jrobotics-editor.jar
```

### Basic Usage

```java
import org.jrobotics.simulation.*;
import org.jrobotics.processor.navigation.AStarPathPlanner;

// Create physics world
PhysicsWorld world = new PhysicsWorld();
world.addBody(PhysicsBody.dynamic("robot", 
    Vector3.ZERO, new Vector3(1, 0, 0), 0.5));

// Path planning
AStarPathPlanner planner = new AStarPathPlanner(100, 100);
List<int[]> path = planner.findPath(0, 0, 50, 50);
```

## Project Structure

```
jrobotics/
├── jrobotics-core        # Core interfaces, EventBus, Executors
├── jrobotics-hal         # GPIO, I2C, SPI, Serial abstraction
├── jrobotics-sensor      # Sensors, Kalman/EKF, Complementary filter
├── jrobotics-actuator    # Motors, Servos, PID control
├── jrobotics-processor   # SLAM, Deep Learning, RL, Navigation
├── jrobotics-simulation  # Physics, Vector3Pool, Visualization
├── jrobotics-network     # TCP/UDP, Swarm DSM, Security, OTA
├── jrobotics-robot       # Robot implementations, Hardware adapters
├── jrobotics-bridge      # ROS2, Cloud adapters
├── jrobotics-benchmark   # JMH performance tests
├── jrobotics-demo        # Demo applications
├── jrobotics-cli         # Command Line Interface tool
└── jrobotics-editor      # Visual Behaviour Tree editor
```

## Documentation

- [Architecture Overview](architecture.md)
- [Networking Comparison](docs/NETWORKING.md)
- [API Reference](javadoc/) - `mvn javadoc:aggregate`

## Building

```bash
# Build all modules
mvn clean install

# Run tests
mvn test

# Generate JavaDoc
mvn javadoc:aggregate

# Run benchmarks
java -jar jrobotics-benchmark/target/benchmarks.jar
```

## License

MIT License - see [LICENSE](LICENSE) file.

## Authors

- **Silvère Martin-Michiellot** - Lead Developer - <silvere.martin@gmail.com>
- **Gemini AI Assistant** - Co-Developer

## References

1. Siegwart, R., et al. (2011). *Introduction to Autonomous Mobile Robots*. MIT Press.
2. Thrun, S., et al. (2005). *Probabilistic Robotics*. MIT Press.
3. Colledanchise, M., & Ögren, P. (2018). *Behavior Trees in Robotics and AI*. CRC Press.

---

*JRobotics v2 - Making robotics accessible to everyone*
