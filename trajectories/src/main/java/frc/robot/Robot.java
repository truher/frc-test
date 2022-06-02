// Copyright (c) FIRST and other WPILib contributors.
// Open Source Software; you can modify and/or share it under the terms of
// the WPILib BSD license file in the root directory of this project.

package frc.robot;

import java.util.List;

import edu.wpi.first.math.geometry.Pose2d;
import edu.wpi.first.math.geometry.Rotation2d;
import edu.wpi.first.math.geometry.Translation2d;
import edu.wpi.first.math.kinematics.SwerveDriveKinematics;
import edu.wpi.first.math.trajectory.Trajectory;
import edu.wpi.first.math.trajectory.TrajectoryConfig;
import edu.wpi.first.math.trajectory.TrajectoryGenerator;
import edu.wpi.first.wpilibj.Joystick;
import edu.wpi.first.wpilibj.TimedRobot;
import edu.wpi.first.wpilibj.Timer;
import edu.wpi.first.wpilibj.drive.DifferentialDrive;
import edu.wpi.first.wpilibj.motorcontrol.PWMSparkMax;
import edu.wpi.first.wpilibj.smartdashboard.Field2d;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;

/**
 * The VM is configured to automatically run this class, and to call the
 * functions corresponding to
 * each mode, as described in the TimedRobot documentation. If you change the
 * name of this class or
 * the package after creating this project, you must also update the manifest
 * file in the resource
 * directory.
 */
public class Robot extends TimedRobot {
  private static final double kMaxWheelSpeedMS = 0.4;
  //private static final double kMaxWheelSpeedMS = 0.1;

  private final PWMSparkMax m_leftDrive = new PWMSparkMax(0);
  private final PWMSparkMax m_rightDrive = new PWMSparkMax(1);
  private final DifferentialDrive m_robotDrive = new DifferentialDrive(m_leftDrive, m_rightDrive);
  private final Joystick m_stick = new Joystick(0);
  private final Timer m_timer = new Timer();
  private final Field2d m_field;
  private final Trajectory m_trajectory;
  private final SwerveDriveKinematics m_kinematics;
  private final TrajectoryConfig m_config;
  private final Translation2d m_aimingPoint;

  public Robot() {
    m_field = new Field2d();
    m_kinematics = new SwerveDriveKinematics(
        new Translation2d(0.1613, 0), // front
        new Translation2d(-0.0807, 0.1397), // left rear
        new Translation2d(-0.0807, -0.1397)); // right rear
    m_aimingPoint = new Translation2d(5, 2);
    m_config = new TrajectoryConfig(1, 1).setKinematics(m_kinematics);
    m_config.addConstraint(new TrackingConstraint(m_kinematics, kMaxWheelSpeedMS, m_aimingPoint));
    m_trajectory = squareOfTranslations(m_config);
    setAimingPoint(m_trajectory, m_aimingPoint);
    m_field.getObject("traj").setTrajectory(m_trajectory);
    SmartDashboard.putData("Field", m_field);
  }

  /*
   * replace the rotation of the trajectory with a rotation aiming at a fixed
   * point.
   */
  public static Trajectory setAimingPoint(Trajectory trajectory, Translation2d aimingPoint) {
    for (Trajectory.State state : trajectory.getStates()) {
      double dx = aimingPoint.getX() - state.poseMeters.getX();
      double dy = aimingPoint.getY() - state.poseMeters.getY();
      Rotation2d rot = new Rotation2d(dx, dy);
      state.poseMeters = new Pose2d(state.poseMeters.getTranslation(), rot);
    }
    return trajectory;
  }

  /**
   * pose start, translation waypoints, pose end. this is the example from the
   * docs an s-shape.
   */
  public static Trajectory example(TrajectoryConfig config) {
    return TrajectoryGenerator.generateTrajectory(
        new Pose2d(0, 0, new Rotation2d(0)),
        List.of(
            new Translation2d(1, 1),
            new Translation2d(2, -1)),
        new Pose2d(3, 0, new Rotation2d(0)),
        config);
  }

  /**
   * 3x3 meter square (7.5, 4.5), using a list of poses, with the pose pointing
   * inward, which i thought would be a strafing square, but it's not, it uses the
   * pose to
   * determine the velocity at the corner, so it overshoots and approaches the
   * corner from the outside. the poses here are not the robot poses ... i mean,
   * they *are* if you're doing a tank drive; it seems like that's what this was
   * designed for.
   */
  public static Trajectory squareOfPoses(TrajectoryConfig config) {
    return TrajectoryGenerator.generateTrajectory(
        List.of(
            new Pose2d(6, 3, new Rotation2d(1, 1)),
            new Pose2d(9, 3, new Rotation2d(-1, 1)),
            new Pose2d(9, 6, new Rotation2d(-1, -1)),
            new Pose2d(6, 6, new Rotation2d(1, -1)),
            new Pose2d(6, 3, new Rotation2d(1, 1))),
        config);
  }

  /**
   * 3x3 meter square centered at (7.5, 4.5), using translations, with start and
   * end poses to make a circle.
   */
  public static Trajectory squareOfTranslations(TrajectoryConfig config) {
    return TrajectoryGenerator.generateTrajectory(
        new Pose2d(6, 3, new Rotation2d(1, -1)), // pose is in the direction of the first waypoint
        List.of(
            new Translation2d(9, 3),
            new Translation2d(9, 6),
            new Translation2d(6, 6)),
        new Pose2d(6, 3, new Rotation2d(1, -1)), // pose is in the direction from the last waypoint
        config);
  }

  /**
   * This function is run when the robot is first started up and should be used
   * for any
   * initialization code.
   */
  @Override
  public void robotInit() {
    // We need to invert one side of the drivetrain so that positive voltages
    // result in both sides moving forward. Depending on how your robot's
    // gearbox is constructed, you might have to invert the left side instead.
    m_rightDrive.setInverted(true);

  }

  /** This function is run once each time the robot enters autonomous mode. */
  @Override
  public void autonomousInit() {
    m_timer.reset();
    m_timer.start();
  }

  /** This function is called periodically during autonomous. */
  @Override
  public void autonomousPeriodic() {
    // Drive for 2 seconds
    if (m_timer.get() < 2.0) {
      m_robotDrive.arcadeDrive(0.5, 0.0); // drive forwards half speed
    } else {
      m_robotDrive.stopMotor(); // stop robot
    }
  }

  /**
   * This function is called once each time the robot enters teleoperated mode.
   */
  @Override
  public void teleopInit() {
  }

  /** This function is called periodically during teleoperated mode. */
  @Override
  public void teleopPeriodic() {
    m_robotDrive.arcadeDrive(m_stick.getY(), m_stick.getX());
    m_field.setRobotPose(new Pose2d(0, 0, new Rotation2d(0)));
  }

  /** This function is called once each time the robot enters test mode. */
  @Override
  public void testInit() {
  }

  /** This function is called periodically during test mode. */
  @Override
  public void testPeriodic() {
  }
}
