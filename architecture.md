# JRobotics Architecture

This document describes the overall architecture of the JRobotics v2 robotics framework.

## Design Principles

1. **Modularity** - Each component is self-contained and can be used independently
2. **Extensibility** - Easy to add new sensors, actuators, processors, and robots
3. **Abstraction** - Hardware-independent interfaces allow code reuse across platforms
4. **AI-Friendliness** - Clear APIs designed for easy integration with AI/ML systems
5. **Real-time Capable** - Efficient algorithms for time-critical operations
6. **Thread-Safety** - Concurrent access patterns built-in from the ground up

## Module Overview

```
┌──────────────────────────────────────────────────────────────────┐
│                         Applications                              │
│                    (Demo apps, User code)                        │
├──────────────────────────────────────────────────────────────────┤
│                         jrobotics-robot                          │
│              (Complete robot implementations)                     │
├─────────────┬──────────────┬─────────────┬──────────────────────┤
│  jrobotics  │  jrobotics   │  jrobotics  │     jrobotics        │
│   sensor    │  processor   │  actuator   │     network          │
├─────────────┴──────────────┴─────────────┴──────────────────────┤
│                      jrobotics-simulation                        │
│                    (Physics, environment)                        │
├──────────────────────────────────────────────────────────────────┤
│    jrobotics-cli    │   jrobotics-editor  │  jrobotics-bench     │
│       (Tools)       │     (Visualizer)    │    (Performance)     │
├──────────────────────────────────────────────────────────────────┤
│                         jrobotics-hal                            │
│                 (Hardware Abstraction Layer)                     │
├──────────────────────────────────────────────────────────────────┤
│                        jrobotics-core                            │
│           (Base interfaces, events, utilities)                   │
└──────────────────────────────────────────────────────────────────┘
```

## Core Concepts

### Component Lifecycle

All major components follow a consistent lifecycle:

```
CREATED → INITIALIZED → STARTED → RUNNING ↔ PAUSED → STOPPED → DESTROYED
```

### Event System

Components communicate via an event bus:

```java
eventBus.publish(new SensorDataEvent(sensorId, data));
eventBus.subscribe(SensorDataEvent.class, this::handleSensorData);
```

### Capability Discovery

Robots expose their capabilities dynamically:

```java
robot.getCapabilities(); // Returns Set<Capability>
robot.hasCapability(Capability.VISION);
robot.hasCapability(Capability.LOCOMOTION);
```

## Module Details

### jrobotics-core

Core interfaces and utilities used by all other modules:

- `Robot` - Main robot interface
- `Component` - Base for sensors, actuators, processors
- `Capability` - Capability discovery
- `Event` / `EventBus` - Event system
- `Lifecycle` - Component lifecycle management
- Time utilities, threading infrastructure

### jrobotics-hal

Hardware Abstraction Layer for platform-independent hardware access:

- `Device` - Physical device abstraction
- `Driver` - Platform-specific drivers
- GPIO, I2C, SPI, Serial protocols
- Platform detection (Raspberry Pi, Jetson, PC)

### jrobotics-sensor

Sensor implementations and fusion:

- `Sensor<T>` - Generic sensor interface
- Camera (RGB, Depth), LIDAR, IMU, ultrasonic, etc.
- `SensorFusion` - Kalman filter (1D, EKF), complementary filter

### jrobotics-actuator

Actuator implementations:

- `Actuator` - Base actuator interface
- Motors (DC, stepper, brushless)
- Servos (position, continuous)
- PID controllers
- Motion profiles

### jrobotics-processor

AI and reasoning components:

- `Processor` - Processing pipeline interface
- **Path Planning** - A*, RRT, Dijkstra
- **Behavior Trees** - Nodes, Composites, Decorators
- **SLAM** - FastSLAM 2.0, Occupancy Grid
- **Deep Learning** - DJL integration for inference
- **Reinforcement Learning** - DQN, PPO agents
- **Swarm Intelligence** - Distributed logic (Boids)

### jrobotics-simulation

Physics simulation environment:

- `Environment` - Virtual world
- `PhysicsEngine` - Rigid body physics
- Entity management
- Hybrid mode support (Simulated + Real)

### jrobotics-network

Networking and communication:

- P2P protocols (UDP)
- Server-mediated communication (TCP)
- **Swarm** - Distributed Shared Memory (DSM), Federated Learning
- **Security** - TLS/SSL, RBAC Manager
- **OTA** - Firmware update manager
- Remote control interfaces
- Message serialization

### jrobotics-robot

Complete robot implementations:

- `WheeledRobot` - Differential drive, etc.
- `LeggedRobot`
- `RoboticArm`
- `Drone`

### jrobotics-bridge

Integration with external frameworks:

- ROS2 bridge (using jros2client)
- Cloud bridge (AWS/Azure IoT adapters)
- MQTT bridge
- Extensible protocol framework

### jrobotics-cli

Command-line tooling:

- Robot management (list, connect, status)
- Configuration
- Firmware updates

### jrobotics-editor

Visual IDE:

- Behavior Tree editor
- Robot configuration visualizer
- Real-time telemetry dashboard (JavaFX)

## Threading Model

```
┌─────────────────────────────────────────────────────┐
│                   Main Thread                        │
│              (Application logic)                     │
├─────────────────────────────────────────────────────┤
│  Sensor Thread Pool     │   Actuator Thread Pool    │
│  (Data acquisition)     │   (Command execution)     │
├─────────────────────────────────────────────────────┤
│         Event Bus Thread (Async dispatch)           │
├─────────────────────────────────────────────────────┤
│  Network I/O Thread     │   Simulation Thread       │
└─────────────────────────────────────────────────────┘
```

## Data Flow

```
Sensors → Sensor Fusion → Processors → Decision → Actuators
    ↓                         ↑
 Event Bus ←──────────────────┘
    ↓
 Logging/Telemetry
```

## Configuration

All configuration uses JSON files:

```json
{
  "robot": {
    "name": "MyRobot",
    "type": "wheeled"
  },
  "sensors": [...],
  "actuators": [...],
  "processors": [...]
}
```

## Internationalization

All user-facing messages support i18n:

- English (en)
- French (fr)
- Spanish (es)
- German (de)

Resource bundles located in `src/main/resources/i18n/`.

---

*Architecture document version 2.0.0*
