/*
 * JRobotics v2 - World-Class General Purpose Robotics API
 * 
 * Copyright (c) 2025 Silvère Martin-Michiellot <silvere.martin@gmail.com>
 * Co-authored by Gemini AI Assistant
 * 
 * Licensed under the MIT License.
 * See LICENSE file in the project root for full license information.
 */
package org.jrobotics.bridge.ros2;

/**
 * Common ROS2 message types.
 * 
 * <p>These records mirror standard ROS2 message definitions.</p>
 * 
 * @author Silvère Martin-Michiellot
 * @author Gemini AI Assistant
 * @version 2.0.0
 * @since 2.0.0
 */
public final class Ros2Messages {
    
    private Ros2Messages() {}
    
    /**
     * geometry_msgs/Twist
     */
    public record Twist(Vector3 linear, Vector3 angular) {
        public static Twist velocity(double linear, double angular) {
            return new Twist(
                new Vector3(linear, 0, 0),
                new Vector3(0, 0, angular)
            );
        }
    }
    
    /**
     * geometry_msgs/Vector3
     */
    public record Vector3(double x, double y, double z) {}
    
    /**
     * geometry_msgs/Pose
     */
    public record Pose(Point position, Quaternion orientation) {}
    
    /**
     * geometry_msgs/Point
     */
    public record Point(double x, double y, double z) {}
    
    /**
     * geometry_msgs/Quaternion
     */
    public record Quaternion(double x, double y, double z, double w) {
        public static Quaternion identity() {
            return new Quaternion(0, 0, 0, 1);
        }
        
        public static Quaternion fromYaw(double yaw) {
            return new Quaternion(0, 0, Math.sin(yaw / 2), Math.cos(yaw / 2));
        }
    }
    
    /**
     * nav_msgs/Odometry
     */
    public record Odometry(
        Header header,
        String childFrameId,
        PoseWithCovariance pose,
        TwistWithCovariance twist
    ) {}
    
    /**
     * std_msgs/Header
     */
    public record Header(int seq, Time stamp, String frameId) {}
    
    /**
     * builtin_interfaces/Time
     */
    public record Time(int sec, int nanosec) {
        public static Time now() {
            long millis = System.currentTimeMillis();
            return new Time((int) (millis / 1000), (int) ((millis % 1000) * 1_000_000));
        }
    }
    
    /**
     * geometry_msgs/PoseWithCovariance
     */
    public record PoseWithCovariance(Pose pose, double[] covariance) {}
    
    /**
     * geometry_msgs/TwistWithCovariance
     */
    public record TwistWithCovariance(Twist twist, double[] covariance) {}
    
    /**
     * sensor_msgs/LaserScan
     */
    public record LaserScan(
        Header header,
        float angleMin,
        float angleMax,
        float angleIncrement,
        float timeIncrement,
        float scanTime,
        float rangeMin,
        float rangeMax,
        float[] ranges,
        float[] intensities
    ) {}
    
    /**
     * sensor_msgs/Imu
     */
    public record Imu(
        Header header,
        Quaternion orientation,
        double[] orientationCovariance,
        Vector3 angularVelocity,
        double[] angularVelocityCovariance,
        Vector3 linearAcceleration,
        double[] linearAccelerationCovariance
    ) {}
}
