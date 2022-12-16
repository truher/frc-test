// Copyright (c) FIRST and other WPILib contributors.
// Open Source Software; you can modify and/or share it under the terms of
// the WPILib BSD license file in the root directory of this project.

package frc.robot;

import java.util.ArrayList;
import java.util.List;

import edu.wpi.first.hal.HAL;
import edu.wpi.first.hal.HALValue;
import edu.wpi.first.math.Nat;
import edu.wpi.first.math.VecBuilder;
import edu.wpi.first.math.estimator.SwerveDrivePoseEstimator;
import edu.wpi.first.math.geometry.Pose2d;
import edu.wpi.first.math.geometry.Rotation2d;
import edu.wpi.first.math.geometry.Translation2d;
import edu.wpi.first.math.kinematics.ChassisSpeeds;
import edu.wpi.first.math.kinematics.SwerveDriveKinematics;
import edu.wpi.first.math.kinematics.SwerveModulePosition;
import edu.wpi.first.math.kinematics.SwerveModuleState;
import edu.wpi.first.math.numbers.N5;
import edu.wpi.first.math.numbers.N7;
import edu.wpi.first.math.util.Units;
import edu.wpi.first.wpilibj.AnalogGyro;
import edu.wpi.first.wpilibj.Timer;
import edu.wpi.first.wpilibj.simulation.AnalogGyroSim;
import edu.wpi.first.wpilibj.simulation.CallbackStore;
import edu.wpi.first.wpilibj.simulation.EncoderSim;
import edu.wpi.first.wpilibj.simulation.PWMSim;
import edu.wpi.first.wpilibj.smartdashboard.Field2d;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;

/** Represents a swerve drive style drivetrain. */
public class Drivetrain {
  public static final double kMaxSpeed = 3.0; // 3 meters per second
  public static final double kMaxAngularSpeed = Math.PI; // 1/2 rotation per second

  private final Translation2d m_frontLeftLocation = new Translation2d(0.381, 0.381);
  private final Translation2d m_frontRightLocation = new Translation2d(0.381, -0.381);
  private final Translation2d m_backLeftLocation = new Translation2d(-0.381, 0.381);
  private final Translation2d m_backRightLocation = new Translation2d(-0.381, -0.381);

  private final SwerveModule m_frontLeft = new SwerveModule(1, 2, 0, 1, 2, 3);
  private final SwerveModule m_frontRight = new SwerveModule(3, 4, 4, 5, 6, 7);
  private final SwerveModule m_backLeft = new SwerveModule(5, 6, 8, 9, 10, 11);
  private final SwerveModule m_backRight = new SwerveModule(7, 8, 12, 13, 14, 15);

  private final AnalogGyro m_gyro = new AnalogGyro(0);

  private final SwerveDriveKinematics m_kinematics = new SwerveDriveKinematics(
      m_frontLeftLocation, m_frontRightLocation, m_backLeftLocation, m_backRightLocation);

  /*
   * Here we use SwerveDrivePoseEstimator so that we can fuse odometry readings.
   * The numbers used
   * below are robot specific, and should be tuned.
   */
  private final SwerveDrivePoseEstimator<N7, N7, N5> m_poseEstimator = new SwerveDrivePoseEstimator<N7, N7, N5>(
      Nat.N7(),
      Nat.N7(),
      Nat.N5(),
      m_gyro.getRotation2d(),
      new SwerveModulePosition[] {
          m_frontLeft.getPosition(),
          m_frontRight.getPosition(),
          m_backLeft.getPosition(),
          m_backRight.getPosition()
      },
      new Pose2d(),
      m_kinematics,
      VecBuilder.fill(0.05, 0.05, Units.degreesToRadians(5), 0.05, 0.05, 0.05, 0.05),
      VecBuilder.fill(Units.degreesToRadians(0.01), 0.01, 0.01, 0.01, 0.01),
      VecBuilder.fill(0.5, 0.5, Units.degreesToRadians(30)));

  Field2d field;

  public Drivetrain() {
    m_gyro.reset();
    field = new Field2d();
    SmartDashboard.putData(field);

  }

  /**
   * Method to drive the robot using joystick info.
   *
   * @param xSpeed        Speed of the robot in the x direction (forward).
   * @param ySpeed        Speed of the robot in the y direction (sideways).
   * @param rot           Angular rate of the robot.
   * @param fieldRelative Whether the provided x and y speeds are relative to the
   *                      field.
   */
  public void drive(double xSpeed, double ySpeed, double rot, boolean fieldRelative) {
    var swerveModuleStates = m_kinematics.toSwerveModuleStates(
        fieldRelative
            ? ChassisSpeeds.fromFieldRelativeSpeeds(xSpeed, ySpeed, rot, m_gyro.getRotation2d())
            : new ChassisSpeeds(xSpeed, ySpeed, rot));
    SwerveDriveKinematics.desaturateWheelSpeeds(swerveModuleStates, kMaxSpeed);
    m_frontLeft.setDesiredState(swerveModuleStates[0]);
    m_frontRight.setDesiredState(swerveModuleStates[1]);
    m_backLeft.setDesiredState(swerveModuleStates[2]);
    m_backRight.setDesiredState(swerveModuleStates[3]);
  }

  /** Updates the field relative position of the robot. */
  public void updateOdometry() {
    m_poseEstimator.update(
        m_gyro.getRotation2d(),
        new SwerveModuleState[] {
            m_frontLeft.getState(),
            m_frontRight.getState(),
            m_backLeft.getState(),
            m_backRight.getState()
        },
        new SwerveModulePosition[] {
            m_frontLeft.getPosition(),
            m_frontRight.getPosition(),
            m_backLeft.getPosition(),
            m_backRight.getPosition()
        });

    // Also apply vision measurements. We use 0.3 seconds in the past as an example
    // -- on
    // a real robot, this must be calculated based either on latency or timestamps.

    // m_poseEstimator.addVisionMeasurement(
    // ExampleGlobalMeasurementSensor.getEstimatedGlobalPose(
    // m_poseEstimator.getEstimatedPosition()),
    // Timer.getFPGATimestamp() - 0.3);
    // System.out.println(m_poseEstimator.getEstimatedPosition());
    // field.setRobotPose(m_poseEstimator.getEstimatedPosition());
  }

  /// simulation stuff below
  List<CallbackStore> cbs = new ArrayList<CallbackStore>();
  EncoderSim frontLeftDriveEncoderSim = EncoderSim.createForChannel(0);
  EncoderSim frontLeftTurnEncoderSim = EncoderSim.createForChannel(2);
  EncoderSim frontRightDriveEncoderSim = EncoderSim.createForChannel(4);
  EncoderSim frontRightTurnEncoderSim = EncoderSim.createForChannel(6);
  EncoderSim backLeftDriveEncoderSim = EncoderSim.createForChannel(8);
  EncoderSim backLeftTurnEncoderSim = EncoderSim.createForChannel(10);
  EncoderSim backRightDriveEncoderSim = EncoderSim.createForChannel(12);
  EncoderSim backRightTurnEncoderSim = EncoderSim.createForChannel(14);

  PWMSim frontLeftDrivePWMSim = new PWMSim(1);
  PWMSim frontLeftTurnPWMSim = new PWMSim(2);
  PWMSim frontRightDrivePWMSim = new PWMSim(3);
  PWMSim frontRightTurnPWMSim = new PWMSim(4);
  PWMSim backLeftDrivePWMSim = new PWMSim(5);
  PWMSim backLeftTurnPWMSim = new PWMSim(6);
  PWMSim backRightDrivePWMSim = new PWMSim(7);
  PWMSim backRightTurnPWMSim = new PWMSim(8);
  AnalogGyroSim gyroSim = new AnalogGyroSim(0);

  public void simulationInit() {
    System.out.println("===start simulation init");
    HAL.initialize(500, 0);
    frontLeftDriveEncoderSim.resetData();
    System.out.println("===start simulation init 2");
    frontLeftTurnEncoderSim.resetData();
    System.out.println("===start simulation init 3");
    frontRightDriveEncoderSim.resetData();
    System.out.println("===start simulation init 4");
    frontRightTurnEncoderSim.resetData();
    System.out.println("===start simulation init 5");
    backLeftDriveEncoderSim.resetData();
    System.out.println("===start simulation init 6");
    backLeftTurnEncoderSim.resetData();
    System.out.println("===start simulation init 7");
    backRightDriveEncoderSim.resetData();
    System.out.println("===start simulation init 8");
    backRightTurnEncoderSim.resetData();
    System.out.println("=== simulation reset done");
    cbs.add(frontLeftDrivePWMSim.registerSpeedCallback((String name, HALValue value) -> {
      System.out.printf("frontLeftDrivePWMSim %s  %f\n", name, value.getDouble());
    }, true));
    cbs.add(frontLeftTurnPWMSim.registerSpeedCallback((String name, HALValue value) -> {
      System.out.printf("frontLeftTurnPWMSim  %s  %f\n", name, value.getDouble());
    }, true));
    cbs.add(frontRightDrivePWMSim.registerSpeedCallback((String name, HALValue value) -> {
      System.out.printf("frontRightDrivePWMSim %s  %f\n", name, value.getDouble());
    }, true));
    cbs.add(frontRightTurnPWMSim.registerSpeedCallback((String name, HALValue value) -> {
      System.out.printf("frontRightTurnPWMSim  %s  %f\n", name, value.getDouble());
    }, true));
    cbs.add(backLeftDrivePWMSim.registerSpeedCallback((String name, HALValue value) -> {
      System.out.printf("backLeftDrivePWMSim   %s  %f\n", name, value.getDouble());
    }, true));
    cbs.add(backLeftTurnPWMSim.registerSpeedCallback((String name, HALValue value) -> {
      System.out.printf("backLeftTurnPWMSim    %s  %f\n", name, value.getDouble());
    }, true));
    cbs.add(backRightDrivePWMSim.registerSpeedCallback((String name, HALValue value) -> {
      System.out.printf("backRightDrivePWMSim  %s  %f\n", name, value.getDouble());
    }, true));
    cbs.add(backRightTurnPWMSim.registerSpeedCallback((String name, HALValue value) -> {
      System.out.printf("backRightTurnPWMSim   %s  %f\n", name, value.getDouble());
    }, true));
    System.out.println("===registering done.");

    System.out.println("===simulation init done");
    System.out.flush();
  }

  public void simulationPeriodic() {
    // turn motor voltage back into speed. feedforward is
    // output = ks * signum(v) + kv * v.
    // so,
    // v = (output - ks*signum(output))/kv

    double frontLeftDriveOutput = frontLeftDrivePWMSim.getSpeed();
    double frontLeftTurnOutput = frontLeftTurnPWMSim.getSpeed();
    double frontRightDriveOutput = frontRightDrivePWMSim.getSpeed();
    double frontRightTurnOutput = frontRightTurnPWMSim.getSpeed();
    double backLeftDriveOutput = backLeftDrivePWMSim.getSpeed();
    double backLeftTurnOutput = backLeftTurnPWMSim.getSpeed();
    double backRightDriveOutput = backRightDrivePWMSim.getSpeed();
    double backRightTurnOutput = backRightTurnPWMSim.getSpeed();

    // drive velocities are meters per second
    // turn velocities are radians per second.
    double frontLeftDriveV = (frontLeftDriveOutput
        - SwerveModule.DRIVE_KS * Math.signum(frontLeftDriveOutput))
        / SwerveModule.DRIVE_KV;
    double frontLeftTurnV = (frontLeftTurnOutput
        - SwerveModule.TURN_KS * Math.signum(frontLeftTurnOutput))
        / SwerveModule.TURN_KV;
    double frontRightDriveV = (frontRightDriveOutput
        - SwerveModule.DRIVE_KS * Math.signum(frontRightDriveOutput))
        / SwerveModule.DRIVE_KV;
    double frontRightTurnV = (frontRightTurnOutput
        - SwerveModule.TURN_KS * Math.signum(frontRightTurnOutput))
        / SwerveModule.TURN_KV;
    double backLeftDriveV = (backLeftDriveOutput
        - SwerveModule.DRIVE_KS * Math.signum(backLeftDriveOutput))
        / SwerveModule.DRIVE_KV;
    double backLeftTurnV = (backLeftTurnOutput
        - SwerveModule.TURN_KS * Math.signum(backLeftTurnOutput))
        / SwerveModule.TURN_KV;
    double backRightDriveV = (backRightDriveOutput
        - SwerveModule.DRIVE_KS * Math.signum(backRightDriveOutput))
        / SwerveModule.DRIVE_KV;
    double backRightTurnV = (backRightTurnOutput
        - SwerveModule.TURN_KS * Math.signum(backRightTurnOutput))
        / SwerveModule.TURN_KV;

    double currentTimeSeconds = Timer.getFPGATimestamp();
    double dt = m_prevTimeSeconds >= 0 ? currentTimeSeconds - m_prevTimeSeconds : m_nominalDt;
    m_prevTimeSeconds = currentTimeSeconds;

    // these velocities affect all the encoders.
    frontLeftDriveEncoderSim.setRate(frontLeftDriveV);
    frontLeftDriveEncoderSim.setDistance(
        frontLeftDriveEncoderSim.getDistance() + frontLeftDriveV * dt);
    frontLeftTurnEncoderSim.setDistance(
        frontLeftTurnEncoderSim.getDistance() + frontLeftTurnV * dt);

    frontRightDriveEncoderSim.setRate(frontRightDriveV);
    frontRightDriveEncoderSim.setDistance(
        frontRightDriveEncoderSim.getDistance() + frontRightDriveV * dt);
    frontRightTurnEncoderSim.setDistance(
        frontRightTurnEncoderSim.getDistance() + frontRightTurnV * dt);

    backLeftDriveEncoderSim.setRate(backLeftDriveV);
    backLeftDriveEncoderSim.setDistance(
        backLeftDriveEncoderSim.getDistance() + backLeftDriveV * dt);
    backLeftTurnEncoderSim.setDistance(
        backLeftTurnEncoderSim.getDistance() + backLeftTurnV * dt);

    backRightDriveEncoderSim.setRate(backRightDriveV);
    backRightDriveEncoderSim.setDistance(
        backRightDriveEncoderSim.getDistance() + backRightDriveV * dt);
    backRightTurnEncoderSim.setDistance(
        backRightTurnEncoderSim.getDistance() + backRightTurnV * dt);

    SwerveModuleState[] states = new SwerveModuleState[] {
        m_frontLeft.getState(),
        m_frontRight.getState(),
        m_backLeft.getState(),
        m_backRight.getState()
    };
    states[0].angle = states[0].angle.plus(new Rotation2d(frontLeftTurnV));
    states[1].angle = states[1].angle.plus(new Rotation2d(frontRightTurnV));
    states[2].angle = states[2].angle.plus(new Rotation2d(backLeftTurnV));
    states[3].angle = states[3].angle.plus(new Rotation2d(backRightTurnV));

    states[0].speedMetersPerSecond = frontLeftDriveV;
    states[1].speedMetersPerSecond = frontRightDriveV;
    states[2].speedMetersPerSecond = backLeftDriveV;
    states[3].speedMetersPerSecond = backRightDriveV;

    ChassisSpeeds speeds = m_kinematics.toChassisSpeeds(states);

    // finally adjust the simulator gyro.
    gyroSim.setAngle(gyroSim.getAngle() + speeds.omegaRadiansPerSecond * dt);
    Pose2d newPose = new Pose2d(robotPose.getX() + speeds.vxMetersPerSecond * dt,
        robotPose.getY() + speeds.vyMetersPerSecond * dt,
        robotPose.getRotation().plus(new Rotation2d(speeds.omegaRadiansPerSecond * dt)));
    robotPose = newPose;
    // field.setRobotPose(m_poseEstimator.getEstimatedPosition());
    field.setRobotPose(newPose);

  }

  Pose2d robotPose = new Pose2d();
  private double m_prevTimeSeconds = Timer.getFPGATimestamp();
  private final double m_nominalDt = 0.02; // Seconds
}
