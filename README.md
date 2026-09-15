# JRobotics v2

[![Build Status](https://github.com/silvere-martin/jrobotics/workflows/CI/badge.svg)](https://github.com/silvere-martin/jrobotics/actions)
[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](https://opensource.org/licenses/MIT)
[![Java Version](https://img.shields.io/badge/Java-21%2B-blue)](https://openjdk.org/)
[![Modules](https://img.shields.io/badge/Modules-14-green)](pom.xml)
[![Security Audited](https://img.shields.io/badge/Security-Hardened%20%26%20Audited-success)](ARCHITECTURE.md#security-architecture)

**World-Class General Purpose Robotics Framework & API for Java**

JRobotics is a modular, high-performance, and secure robotics platform for Java 21+. It provides an end-to-end toolkit spanning hardware abstraction, sensor fusion, computer vision, kinematics, artificial intelligence, multi-robot swarm orchestration, physical 2D/3D simulation, and real-time networking.

---

## 📊 Project Metrics

| Metric | Value |
|:---|:---|
| **Maven Modules** | 14 (13 submodules + root parent) |
| **Java Classes** | 214+ classes & records |
| **Lines of Code** | ~25,000 LOC |
| **Test Suites** | Unit, Integration, & JMH Benchmarks (100% pass rate) |
| **Target Java Version** | Java 21 LTS (records, pattern matching, concurrent utilities) |
| **License** | MIT Open Source |

---

## 🚀 Key Features

### 🤖 Core & Lifecycle (`jrobotics-core`)
- **Deterministic Lifecycle**: Uniform component states (`CREATED`, `INITIALIZED`, `STARTED`, `RUNNING`, `PAUSED`, `STOPPED`, `DESTROYED`) with thread-safe atomic transitions.
- **Asynchronous Event Bus**: High-throughput typed event publication and subscription.
- **Dedicated Threading Infrastructure**: `RobotExecutors` managing sensor acquisition, actuator command loops, and background I/O.
- **Internationalization (i18n)**: Fully localized diagnostics and logs (English, French, Spanish, German, Chinese).

### 🔌 Hardware Abstraction Layer (`jrobotics-hal`)
- **Unified Protocol Interfaces**: GPIO, I2C, SPI, and UART/Serial abstractions.
- **Target Drivers**: Native Raspberry Pi support (Pi4J), USB/Serial devices (JSerialComm), and automated virtual mock fallback for desktop simulation.

### 📡 Sensors & State Estimation (`jrobotics-sensor`)
- **Extensive Sensor Types**: LiDAR, Ultrasonic, Depth/RGB Vision, 9-DoF IMU, Encoders, GPS/GNSS, and Vosk Speech Recognition.
- **Sensor Fusion Algorithms**:
  - 1D Kalman Filter & Multi-dimensional **Extended Kalman Filter (EKF)** with Gauss-Jordan inversion.
  - Complementary Filter for low-latency orientation tracking.

### ⚙️ Actuators & Control (`jrobotics-actuator`)
- **Motors**: DC, Stepper, and Brushless motor controllers with speed ramp curves and zero-division guards.
- **Servos**: Position and continuous servos, `ServoBank` multi-channel controllers, acceleration profiles.
- **Closed-Loop Control**: Industrial-grade PID / Feedforward controllers with integral windup protection.

### 🧠 AI, Planning & Computer Vision (`jrobotics-processor`)
- **Path Planning & Navigation**:
  - Grid-based **A\*** (optimal priority queue re-evaluation).
  - Dynamic Window Approach (**DWA**) obstacle avoidance.
  - Rapidly-exploring Random Trees (**RRT**) and Dijkstra graph search.
- **Simultaneous Localization and Mapping (SLAM)**:
  - **FastSLAM 2.0** particle filter with landmark estimation.
  - Occupancy Grid Mapping with Bresenham ray-casting and log-odds updates.
- **Deep Learning & Computer Vision**:
  - Deep Java Library (**DJL**) engine support (PyTorch & TensorFlow inference).
  - Object detection pipelines (YOLO / SSD) with non-maximum suppression.
- **Reinforcement Learning**:
  - Deep Q-Network (**DQN**) agents with experience replay.
  - Proximal Policy Optimization (**PPO**) Actor-Critic agents with analytical backpropagation.
- **Behavior Trees**: Robust execution trees with composite, decorator, and leaf action/condition nodes.

### 🐝 Swarm & Distributed Systems (`jrobotics-processor` & `jrobotics-network`)
- **Swarm Intelligence**: Boids Reynolds flocking, V-shape/Circle/Grid formation control, and dynamic leader election.
- **Distributed Shared Memory (DSM)**: UDP multicast peer-to-peer state synchronization.
- **Federated Learning (FedAvg)**: Decentralized model aggregation across robotic swarms.
- **Hive Mind**: Centralized orchestration for multi-robot fleets.

### 🌐 Secure Networking & Telemetry (`jrobotics-network`)
- **Hardened Serialization**: JSON-over-TCP/UDP communication with length headers and maximum payload boundaries, eliminating Java native deserialization RCE risks (CWE-502).
- **Transport Security (TLS 1.3)**: Enforced TLS endpoint hostname verification (CWE-297).
- **Authentication & RBAC**: Token-based authentication with constant-time verification (`MessageDigest.isEqual`) against timing side-channel attacks (CWE-208).
- **Secure OTA Updates**: Over-The-Air firmware deployments with streaming SHA-256 validation and directory traversal prevention (CWE-22).
- **Real-Time Web Dashboard**: Lightweight HTTP/WebSocket monitoring server protected against Slowloris DoS.

### 🌍 Simulation & Physics (`jrobotics-simulation`)
- **Physics Engine**: Rigid body dynamics, gravity, friction, restitution, and collisions.
- **Zero-Allocation Pools**: `Vector3Pool` with double-release guards for GC-free physics hot loops.
- **Multi-Engine Visualizers**: Console ASCII renderer, JavaFX 2D canvas, Swing 2D viewport, and JMonkeyEngine (jME3) 3D world.

### 🦾 Robots & Kinematics (`jrobotics-robot`)
- **Mobile Platforms**: Differential drive, Ackermann, and Omnidirectional kinematics with real-time odometry.
- **Manipulators**: Multi-link `RoboticArm` with **CCD (Cyclic Coordinate Descent)** Inverse Kinematics solver.
- **Adapters**: TurtleBot3, iRobot Create3, Arduino, and custom DIY frames.

### 🌉 Bridges & External Ecosystems (`jrobotics-bridge`)
- **ROS 2 Integration**: Native ROS 2 bridge client for topics and services.
- **MQTT Bridge**: Clean Eclipse Paho MQTT bridge for IoT gateways.
- **Cloud Adapters**: Extensible protocols for AWS IoT and Azure IoT Hub.

### 🛠️ Developer Tooling (`jrobotics-cli` & `jrobotics-editor`)
- **CLI (`jrobotics-cli`)**: Command-line interface for robot discovery, diagnostics, and telemetry streaming.
- **Visual Editor (`jrobotics-editor`)**: Graphical canvas for designing Behavior Trees and robot setups.
- **Benchmarks (`jrobotics-benchmark`)**: JMH harnesses measuring microsecond-level performance.

---

## 📦 Quick Start

### Prerequisites
- **JDK 21+** (Eclipse Temurin, OpenJDK, or GraalVM recommended)
- **Maven 3.9+**
- Git

### Build & Test

```bash
# Clone the repository
git clone https://github.com/silvere-martin/jrobotics.git
cd jrobotics/v2

# Build all 14 modules and run the full test suite
mvn clean test

# Package all JAR artifacts
mvn clean package -DskipTests
```

### Running Demos

```bash
# Interactive 2D Visual / Console Simulation
./run-visual-demo.bat     # Windows
./run-visual-demo.sh      # Linux / macOS

# JMonkeyEngine 3D Physics Simulation
./run-3d-demo.bat         # Windows
./run-3d-demo.sh          # Linux / macOS

# Web Telemetry Dashboard
./run-dashboard-demo.bat  # Windows
./run-dashboard-demo.sh   # Linux / macOS

# JMH Microbenchmarks
./run-benchmarks.bat      # Windows
./run-benchmarks.sh       # Linux / macOS
```

---

## 💻 Code Examples

### 1. Initializing a Differential Drive Robot with Navigation
```java
import org.jrobotics.core.context.DefaultRobotContext;
import org.jrobotics.robot.mobile.DifferentialDriveRobot;
import org.jrobotics.processor.navigation.AStarPathPlanner;

// Create and initialize robot
DifferentialDriveRobot robot = new DifferentialDriveRobot("Scout-1", 0.15, 0.30);
robot.init(new DefaultRobotContext());
robot.start();

// Plan path on 100x100 occupancy grid
AStarPathPlanner planner = new AStarPathPlanner(100, 100);
List<int[]> path = planner.findPath(0, 0, 45, 80);

// Drive along path
robot.drive(0.5, 0.0); // 0.5 m/s forward
```

### 2. Multi-link Robotic Arm Inverse Kinematics
```java
import org.jrobotics.robot.manipulator.RoboticArm;
import org.jrobotics.robot.manipulator.CcdInverseKinematicsSolver;
import org.jrobotics.simulation.Vector3;

RoboticArm arm = new RoboticArm("Arm-6DoF");
arm.addSegment(1.0, Vector3.ZERO);
arm.addSegment(0.8, new Vector3(1.0, 0, 0));
arm.addSegment(0.5, new Vector3(1.8, 0, 0));

CcdInverseKinematicsSolver solver = new CcdInverseKinematicsSolver(50, 0.01);
boolean reached = solver.solve(arm, new Vector3(1.2, 0.5, 0.2));
```

### 3. Secure Peer-to-Peer Networking
```java
import org.jrobotics.network.server.TcpServer;
import org.jrobotics.network.server.TcpClient;
import org.jrobotics.network.security.SecureTransport;
import org.jrobotics.network.security.TLSConfig;

// Setup TLS configuration
TLSConfig tls = TLSConfig.builder()
    .keyStorePath("keystore.jks")
    .keyStorePassword("secret")
    .requireClientAuth(true)
    .build();

TcpServer server = new TcpServer(8443, tls);
server.start();

TcpClient client = new TcpClient("127.0.0.1", 8443, tls);
client.connect();
client.sendMessage("Scout-1", "TELEMETRY", Map.of("battery", 98.5));
```

---

## 📂 Project Architecture

```
jrobotics/
├── jrobotics-core         # Lifecycle, EventBus, Concurrency, i18n
├── jrobotics-hal          # GPIO, I2C, SPI, Serial, Hardware abstraction
├── jrobotics-sensor       # LiDAR, IMU, Vision, Vosk Voice, Kalman & EKF
├── jrobotics-actuator     # DC/Stepper/Brushless Motors, Servos, PID loops
├── jrobotics-processor    # A*, DWA, FastSLAM, OccupancyGrid, DJL, DQN, PPO
├── jrobotics-simulation   # Rigid-body physics, Vector3Pool, 2D/3D renderers
├── jrobotics-network      # TLS Sockets, RBAC, Safe JSON, Swarm DSM, OTA
├── jrobotics-robot        # Mobile robots, Manipulators, CCD IK, Adapters
├── jrobotics-bridge       # ROS 2, MQTT, Cloud IoT connectors
├── jrobotics-benchmark    # JMH performance measurement suites
├── jrobotics-demo         # Interactive 2D/3D & multi-robot scenarios
├── jrobotics-cli          # Operator CLI terminal
└── jrobotics-editor       # Visual Behavior Tree & Configuration designer
```

---

## 📖 Documentation Links

- 🏛️ [Architecture & Design Guide](ARCHITECTURE.md)
- 🌐 [Networking & Security Protocol Comparison](NETWORKING.md)
- 🗺️ [Development Roadmap & Milestones](ROADMAP.md)
- 👥 [Project Credits & Bibliography](CREDITS.md)

---

## 📄 License

This project is licensed under the **MIT License**. See the [LICENSE](LICENSE) file for details.

## 👨‍💻 Authors & Maintainers

- **Silvère Martin-Michiellot** - *Lead Architect & Developer* - <silvere.martin@gmail.com>
- **Google DeepMind / Antigravity AI** - *Co-Developer & QA Auditor*
