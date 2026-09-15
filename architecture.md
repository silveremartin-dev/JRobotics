# JRobotics v2 Architecture

This document provides a comprehensive technical breakdown of the architecture, design principles, component hierarchy, concurrency models, and security guarantees of the **JRobotics v2** framework.

---

## 1. Architectural Principles

1. **Strict Modularity**: Each module maintains explicit boundaries and minimal coupling. Projects can import only the specific capabilities they need (e.g., pure simulation vs. bare-metal HAL).
2. **Deterministic Lifecycle**: Every active component implements standard lifecycle state transitions (`CREATED`, `INITIALIZED`, `STARTED`, `RUNNING`, `PAUSED`, `STOPPED`, `DESTROYED`) with thread-safe atomic guarantees.
3. **Hardware Independence**: High-level algorithms (SLAM, A*, PID, Behavior Trees) operate on pure domain abstractions (`Pose2D`, `Vector3`, `Sensor<T>`, `Actuator`), decoupled from physical device protocols.
4. **Security by Design**: Native defense against network vulnerabilities (TLS endpoint validation, constant-time authentication, safe JSON serialization preventing CWE-502 RCE, anti-Slowloris rate limiting, and sandbox path verification).
5. **Real-Time Efficiency**: Zero-allocation pooling (`Vector3Pool`) on high-frequency loops ($60\text{ Hz} - 1000\text{ Hz}$), lock-free atomic references, and dedicated worker pools (`RobotExecutors`).

---

## 2. Module Hierarchy & Dependencies

```
┌────────────────────────────────────────────────────────────────────────┐
│                              Applications                              │
│                    (jrobotics-demo, CLI, User Code)                    │
├────────────────────────────────────────────────────────────────────────┤
│                            jrobotics-robot                             │
│       (Mobile Platforms, RoboticArm Manipulators, CCD IK Solver)       │
├──────────────┬──────────────┬──────────────┬─────────────┬─────────────┤
│  jrobotics-  │  jrobotics-  │  jrobotics-  │ jrobotics-  │ jrobotics-  │
│    sensor    │  actuator    │  processor   │   network   │   bridge    │
│  (LiDAR, IMU,│ (Motors,     │ (SLAM, AI,   │ (TLS, Swarm,│ (ROS 2,     │
│   EKF, Vosk) │  Servos, PID)│  A*, DJL)    │  DSM, OTA)  │  MQTT, IoT) │
├──────────────┴──────────────┴──────────────┴─────────────┴─────────────┤
│                          jrobotics-simulation                          │
│               (Rigid Body Physics, Vector3Pool, 2D/3D UI)               │
├────────────────────────────────────────────────────────────────────────┤
│                             jrobotics-hal                              │
│            (GPIO, I2C, SPI, Serial, Pi4J, JSerialComm Drivers)         │
├────────────────────────────────────────────────────────────────────────┤
│                            jrobotics-core                              │
│           (Lifecycle, EventBus, Concurrency, Context, i18n)            │
└────────────────────────────────────────────────────────────────────────┘
```

---

## 3. Module Breakdown

### 3.1 `jrobotics-core`
The foundational layer containing domain models, interfaces, and execution primitives:
- `Robot`: Central aggregation interface representing a physical or simulated autonomous agent.
- `Lifecycle`: Atomic state machine governing component startup, runtime state, graceful stopping, and resource destruction.
- `EventBus`: High-performance decoupled publish-subscribe bus supporting synchronous and asynchronous message delivery.
- `RobotExecutors`: Standardized thread pool factory providing dedicated pools for sensor acquisition, actuator control loops, simulation physics steps, and asynchronous background tasks.
- `I18n`: Localized diagnostic logging and message catalog supporting English, French, Spanish, German, and Chinese.

### 3.2 `jrobotics-hal`
Hardware Abstraction Layer isolating low-level peripheral drivers:
- `Device` & `Driver`: Pluggable physical hardware interfaces.
- `GpioPin`, `I2CBus`, `SpiDevice`, `SerialPort`: Standardized buses for sensors and microcontrollers.
- **Implementations**: Native Raspberry Pi integration via Pi4J, universal USB-UART communication via JSerialComm, and `VirtualHalProvider` for mock test execution.

### 3.3 `jrobotics-sensor`
Sensory acquisition and state estimation:
- **Range & Spatial**: 2D LiDAR (`LidarScan`), Ultrasonic distance, Time-of-Flight (ToF).
- **Inertial & Environmental**: 9-DoF IMU (accelerometer, gyroscope, magnetometer), encoders, GPS/GNSS coordinates, thermometers (DS18B20).
- **Acoustic & Vision**: Vosk offline speech recognition sensor and camera frames.
- **Sensor Fusion**:
  - `KalmanFilter1D`: Linear single-variable estimation.
  - `ExtendedKalmanFilter`: Non-linear state estimation featuring Gauss-Jordan matrix inversion for arbitrary dimensions.
  - `ComplementaryFilter`: Lightweight high-rate orientation estimation.

### 3.4 `jrobotics-actuator`
Kinematic control and propulsion:
- **Motors**: DC motors, Stepper motors, and Brushless ESCs with configurable acceleration curves, deadbands, and speed ramp limits.
- **Servos**: Angular position servos, continuous rotation servos, and multi-channel `ServoBank` coordinators.
- **Closed-Loop Feedback**: `PidController` with feedforward gain, output saturation limits, and anti-windup accumulator resets.

### 3.5 `jrobotics-processor`
Intelligence, cognitive planning, computer vision, and machine learning:
- **Navigation & Planning**:
  - `AStarPathPlanner`: 8-connected grid search with heap-invariant priority updates.
  - `DynamicWindowApproach` (DWA): Velocity-space trajectory rollout avoiding dynamic obstacles.
  - `RrtPathPlanner`: Rapidly-exploring Random Trees for high-dimensional configuration spaces.
- **SLAM (Simultaneous Localization and Mapping)**:
  - `FastSLAMProcessor`: Rao-Blackwellized particle filter maintaining individual landmark Kalman filters.
  - `OccupancyGridSLAM`: 2D probability grid map updated via Bresenham ray tracing and log-odds formulation.
- **Deep Learning & Computer Vision**:
  - `DeepLearningProcessor`: Generic DJL engine (PyTorch / TensorFlow / ONNX) running onboard neural inference.
  - `ObjectDetectionProcessor`: YOLO and SSD object detection pipelines with bounding-box regression.
- **Reinforcement Learning**:
  - `DQNAgent`: Deep Q-Learning agent with experience replay buffer and target network synchronization.
  - `PPOAgent`: Proximal Policy Optimization with two-layer Actor-Critic networks and clipped surrogate objectives.
- **Behavior Trees**: Deterministic decision graphs (`Sequence`, `Selector`, `Parallel`, `Inverter`, `ActionNode`, `ConditionNode`).

### 3.6 `jrobotics-simulation`
Real-time physics and 2D/3D visualizers:
- `PhysicsEngine`: Vector math (`Vector3`, `Quaternion`), rigid body collisions, gravity, friction, and kinematic constraints.
- `Vector3Pool`: Zero-garbage vector pooling for real-time physics loops with double-release guards.
- **Renderers**:
  - `ConsoleVisualizer`: ASCII rendering for headless terminals and CI environments.
  - `SwingVisualizer` & `JavaFxVisualizer`: High-speed 2D canvas displays.
  - `Jme3SimulationWorld`: Full 3D rendering powered by JMonkeyEngine.

### 3.7 `jrobotics-network` & Security Architecture
Robust, secure communication primitives:
- `TcpServer` & `TcpClient`: Hardened JSON framing with 4-byte length prefixing, packet size bounds (10 MB max), and zero native Java deserialization (CWE-502 safe).
- `SecureTransport`: TLS 1.3 encryption with explicit hostname endpoint identification (`SSLParameters.setEndpointIdentificationAlgorithm("HTTPS")`).
- `AuthenticationService` & `RBACManager`: Token validation with constant-time equality comparisons (`MessageDigest.isEqual`) preventing timing side-channel attacks (CWE-208).
- `DistributedSharedMemory` (DSM): Multicast UDP swarm key-value memory synchronization with UTF-8 encoding.
- `OTAUpdateManager`: Over-The-Air signed firmware installation with path-traversal prevention (CWE-22) and streaming SHA-256 verification (`DigestInputStream`).
- `DashboardServer`: Asynchronous HTTP/WebSocket telemetry server with read timeouts preventing Slowloris DoS.

### 3.8 `jrobotics-robot`
Ready-to-use robot assemblies and kinematics:
- `DifferentialDriveRobot`: Two-wheel mobile robot with odometry integration and differential kinematics.
- `RoboticArm`: Multi-joint kinematic chain with **CCD (Cyclic Coordinate Descent)** inverse kinematics solver.
- Platform adapters for TurtleBot3, iRobot Create3, and Arduino peripherals.

### 3.9 `jrobotics-bridge`
Interoperability with external robotics ecosystems:
- `Ros2Bridge`: Topics and services bridging with ROS 2 DDS networks.
- `MqttBridge`: Eclipse Paho MQTT pub/sub client for industrial IoT gateways.
- `CloudBridge`: Extensible cloud telemetry connectors.

---

## 4. Threading & Concurrency Model

```
┌────────────────────────────────────────────────────────┐
│                  Main Application Loop                 │
│               (Orchestration & High-Level AI)          │
├────────────────────────────────────────────────────────┤
│   Sensor Thread Pool      │    Actuator Thread Pool    │
│ (LiDAR, IMU, Camera @ Hz) │ (PID, PWM, Motors @ 100Hz) │
├────────────────────────────────────────────────────────┤
│          EventBus Worker (Async Event Dispatch)        │
├────────────────────────────────────────────────────────┤
│   Network I/O Workers     │   Physics Engine Step Loop │
│  (Netty/NIO TCP/UDP/TLS)  │   (Real-time delta-T tick) │
└────────────────────────────────────────────────────────┘
```

All shared states between threads use:
1. Lock-free atomics (`AtomicReference`, `AtomicBoolean`, `AtomicInteger`) for single-variable flags.
2. Concurrent collections (`ConcurrentHashMap`, `CopyOnWriteArrayList`, `ArrayBlockingQueue`).
3. Reentrant locks with bounded timeouts on critical resource updates.

---

## 5. Security Guarantees Summary

| Vulnerability Vector | Defense Implementation in JRobotics v2 |
| :--- | :--- |
| **Insecure Deserialization (CWE-502)** | Native Java `ObjectInputStream` eliminated; replaced with typed Jackson JSON parsing and length bounding. |
| **Timing Side-Channel Attacks (CWE-208)** | All auth token and password comparisons use constant-time `MessageDigest.isEqual()`. |
| **Improper TLS Hostname Verification (CWE-297)** | `SecureTransport` explicitly enforces `SSLParameters.setEndpointIdentificationAlgorithm("HTTPS")`. |
| **Path Traversal / Arbitrary File Overwrite (CWE-22)** | `OTAUpdateManager` verifies normalized destination paths stay strictly within the target base directory. |
| **Slowloris DoS on Telemetry Dashboard** | `DashboardServer` enforces socket read timeouts and offloads processing to a dedicated worker pool. |

---

*Document version: 2.0.0 — Updated: September 2026*

