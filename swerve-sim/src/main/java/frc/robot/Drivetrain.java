// Copyright (c) FIRST and other WPILib contributors.
// Open Source Software; you can modify and/or share it under the terms of
// the WPILib BSD license file in the root directory of this project.

package frc.robot;

import java.util.ArrayList;
import java.util.List;

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
import edu.wpi.first.networktables.DoubleArrayPublisher;
import edu.wpi.first.networktables.DoublePublisher;
import edu.wpi.first.networktables.NetworkTable;
import edu.wpi.first.networktables.NetworkTableInstance;
import edu.wpi.first.networktables.StringPublisher;
import edu.wpi.first.wpilibj.AnalogGyro;
import edu.wpi.first.wpilibj.Timer;
import edu.wpi.first.wpilibj.simulation.AnalogGyroSim;
import edu.wpi.first.wpilibj.simulation.CallbackStore;
import edu.wpi.first.wpilibj.simulation.EncoderSim;
import edu.wpi.first.wpilibj.simulation.PWMSim;

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

  Pose2d robotPose = new Pose2d();
  private double m_prevTimeSeconds = Timer.getFPGATimestamp();
  private final double m_nominalDt = 0.02; // Seconds

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

  NetworkTableInstance inst = NetworkTableInstance.getDefault();

  DoublePublisher xSpeedPub = inst.getTable("desired").getDoubleTopic("xspeed m_s").publish();
  DoublePublisher ySpeedPub = inst.getTable("desired").getDoubleTopic("yspeed m_s").publish();
  DoublePublisher thetaSpeedPub = inst.getTable("desired").getDoubleTopic("thetaspeed rad_s").publish();

  NetworkTable frontLeft = inst.getTable("FrontLeft");
  NetworkTable frontRight = inst.getTable("FrontRight");
  NetworkTable backLeft = inst.getTable("BackLeft");
  NetworkTable backRight = inst.getTable("BackRight");

  // distance
  DoublePublisher frontLeftDriveEncoderPub = frontLeft.getDoubleTopic("driveEncoderDistance").publish();
  DoublePublisher frontLeftTurnEncoderPub = frontLeft.getDoubleTopic("turnEncoderDistance").publish();
  DoublePublisher frontRightDriveEncoderPub = frontRight.getDoubleTopic("driveEncoderDistance").publish();
  DoublePublisher frontRightTurnEncoderPub = frontRight.getDoubleTopic("turnEncoderDistance").publish();
  DoublePublisher backLeftDriveEncoderPub = backLeft.getDoubleTopic("driveEncoderDistance").publish();
  DoublePublisher backLeftTurnEncoderPub = backLeft.getDoubleTopic("turnEncoderDistance").publish();
  DoublePublisher backRightDriveEncoderPub = backRight.getDoubleTopic("driveEncoderDistance").publish();
  DoublePublisher backRightTurnEncoderPub = backRight.getDoubleTopic("turnEncoderDistance").publish();

  // drive rate only; turn rate is ignored
  DoublePublisher frontLeftDriveEncoderRatePub = frontLeft.getDoubleTopic("driveEncoderRate").publish();
  // DoublePublisher frontLeftTurnEncoderRatePub =
  // frontLeft.getDoubleTopic("turnEncoderRate").publish();
  DoublePublisher frontRightDriveEncoderRatePub = frontRight.getDoubleTopic("driveEncoderRate").publish();
  // DoublePublisher frontRightTurnEncoderRatePub =
  // frontRight.getDoubleTopic("turnEncoderRate").publish();
  DoublePublisher backLeftDriveEncoderRatePub = backLeft.getDoubleTopic("driveEncoderRate").publish();
  // DoublePublisher backLeftTurnEncoderRatePub =
  // backLeft.getDoubleTopic("turnEncoderRate").publish();
  DoublePublisher backRightDriveEncoderRatePub = backRight.getDoubleTopic("driveEncoderRate").publish();
  // DoublePublisher backRightTurnEncoderRatePub =
  // backRight.getDoubleTopic("turnEncoderRate").publish();

  // motor output
  DoublePublisher frontLeftDrivePWMPub = frontLeft.getDoubleTopic("driveMotorSpeed").publish();
  DoublePublisher frontLeftTurnPWMPub = frontLeft.getDoubleTopic("turnMotorSpeed").publish();
  DoublePublisher frontRightDrivePWMPub = frontRight.getDoubleTopic("driveMotorSpeed").publish();
  DoublePublisher frontRightTurnPWMPub = frontRight.getDoubleTopic("turnMotorSpeed").publish();
  DoublePublisher backLeftDrivePWMPub = backLeft.getDoubleTopic("driveMotorSpeed").publish();
  DoublePublisher backLeftTurnPWMPub = backLeft.getDoubleTopic("turnMotorSpeed").publish();
  DoublePublisher backRightDrivePWMPub = backRight.getDoubleTopic("driveMotorSpeed").publish();
  DoublePublisher backRightTurnPWMPub = backRight.getDoubleTopic("turnMotorSpeed").publish();

  // desired velocity
  DoublePublisher frontLeftDriveVPub = inst.getTable("desiredV/FrontLeft/drive").getDoubleTopic("speed").publish();
  DoublePublisher frontLeftTurnVPub = inst.getTable("desiredV/FrontLeft/turn").getDoubleTopic("speed").publish();
  DoublePublisher frontRightDriveVPub = inst.getTable("desiredV/FrontRight/drive").getDoubleTopic("speed").publish();
  DoublePublisher frontRightTurnVPub = inst.getTable("desiredV/FrontRight/turn").getDoubleTopic("speed").publish();
  DoublePublisher backLeftDriveVPub = inst.getTable("desiredV/BackLeft/drive").getDoubleTopic("speed").publish();
  DoublePublisher backLeftTurnVPub = inst.getTable("desiredV/BackLeft/turn").getDoubleTopic("speed").publish();
  DoublePublisher backRightDriveVPub = inst.getTable("desiredV/BackRight/drive").getDoubleTopic("speed").publish();
  DoublePublisher backRightTurnVPub = inst.getTable("desiredV/BackRight/turn").getDoubleTopic("speed").publish();

  DoubleArrayPublisher fieldPub = inst.getTable("blarb").getDoubleArrayTopic("eh").publish();
  StringPublisher fieldTypePub;

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

  public Drivetrain() {
    m_gyro.reset();
    // field = new Field2d();
    // SmartDashboard.putData(field);
    inst.startClient4("blarg");
    fieldTypePub = inst.getTable("blarb").getStringTopic(".type").publish();
    fieldTypePub.set("Field2d");
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
    // requestedXSpeed = xSpeed;
    // requestedYSpeed = ySpeed;
    // requestedThetaSpeed = rot;
    xSpeedPub.set(xSpeed);
    ySpeedPub.set(ySpeed);
    thetaSpeedPub.set(rot);

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
    // -- on a real robot, this must be calculated based either on latency or
    // timestamps.

    // m_poseEstimator.addVisionMeasurement(
    // ExampleGlobalMeasurementSensor.getEstimatedGlobalPose(
    // m_poseEstimator.getEstimatedPosition()),
    // Timer.getFPGATimestamp() - 0.3);

  }

  public void pubSim(PWMSim sim, DoublePublisher pub) {
    cbs.add(sim.registerSpeedCallback((name, value) -> pub.set(value.getDouble()), true));
  }

  public void simulationInit() {
    pubSim(frontLeftDrivePWMSim, frontLeftDrivePWMPub);
    pubSim(frontLeftTurnPWMSim, frontLeftTurnPWMPub);
    pubSim(frontRightDrivePWMSim, frontRightDrivePWMPub);
    pubSim(frontRightTurnPWMSim, frontRightTurnPWMPub);
    pubSim(backLeftDrivePWMSim, backLeftDrivePWMPub);
    pubSim(backLeftTurnPWMSim, backLeftTurnPWMPub);
    pubSim(backRightDrivePWMSim, backRightDrivePWMPub);
    pubSim(backRightTurnPWMSim, backRightTurnPWMPub);
  }

  /**
   * turn motor voltage back into speed
   * 
   * inverting the feedforward is surely wrong but it should work.
   * 
   * feedforward is
   * output = ks * signum(v) + kv * v.
   * so,
   * v = (output - ks*signum(output))/kv
   * 
   * to slow things down, divide by ... two?
   */
  public double vFromOutput(double output, double ks, double kv) {
    return 0.5 * (output - ks * Math.signum(output)) / kv;
  }

  public void simulationPeriodic() {
    double currentTimeSeconds = Timer.getFPGATimestamp();
    double dt = m_prevTimeSeconds >= 0 ? currentTimeSeconds - m_prevTimeSeconds : m_nominalDt;
    m_prevTimeSeconds = currentTimeSeconds;

    // drive velocities are meters per second
    // turn velocities are radians per second.
    double frontLeftDriveV = vFromOutput(frontLeftDrivePWMSim.getSpeed(), SwerveModule.DRIVE_KS, SwerveModule.DRIVE_KV);
    double frontLeftTurnV = vFromOutput(frontLeftTurnPWMSim.getSpeed(), SwerveModule.TURN_KS, SwerveModule.TURN_KV);
    double frontRightDriveV = vFromOutput(frontRightDrivePWMSim.getSpeed(), SwerveModule.DRIVE_KS,
        SwerveModule.DRIVE_KV);
    double frontRightTurnV = vFromOutput(frontRightTurnPWMSim.getSpeed(), SwerveModule.TURN_KS, SwerveModule.TURN_KV);
    double backLeftDriveV = vFromOutput(backLeftDrivePWMSim.getSpeed(), SwerveModule.DRIVE_KS, SwerveModule.DRIVE_KV);
    double backLeftTurnV = vFromOutput(backLeftTurnPWMSim.getSpeed(), SwerveModule.TURN_KS, SwerveModule.TURN_KV);
    double backRightDriveV = vFromOutput(backRightDrivePWMSim.getSpeed(), SwerveModule.DRIVE_KS, SwerveModule.DRIVE_KV);
    double backRightTurnV = vFromOutput(backRightTurnPWMSim.getSpeed(), SwerveModule.TURN_KS, SwerveModule.TURN_KV);

    frontLeftDriveVPub.set(frontLeftDriveV);
    frontLeftTurnVPub.set(frontLeftTurnV);
    frontRightDriveVPub.set(frontRightDriveV);
    frontRightTurnVPub.set(frontRightTurnV);
    backLeftDriveVPub.set(backLeftDriveV);
    backLeftTurnVPub.set(backLeftTurnV);
    backRightDriveVPub.set(backRightDriveV);
    backRightTurnVPub.set(backRightTurnV);

    frontLeftDriveEncoderSim.setRate(frontLeftDriveV);
    frontLeftDriveEncoderSim.setDistance(frontLeftDriveEncoderSim.getDistance() + frontLeftDriveV * dt);
    frontLeftTurnEncoderSim.setDistance(frontLeftTurnEncoderSim.getDistance() + frontLeftTurnV * dt);

    frontRightDriveEncoderSim.setRate(frontRightDriveV);
    frontRightDriveEncoderSim.setDistance(frontRightDriveEncoderSim.getDistance() + frontRightDriveV * dt);
    frontRightTurnEncoderSim.setDistance(frontRightTurnEncoderSim.getDistance() + frontRightTurnV * dt);

    backLeftDriveEncoderSim.setRate(backLeftDriveV);
    backLeftDriveEncoderSim.setDistance(backLeftDriveEncoderSim.getDistance() + backLeftDriveV * dt);
    backLeftTurnEncoderSim.setDistance(backLeftTurnEncoderSim.getDistance() + backLeftTurnV * dt);

    backRightDriveEncoderSim.setRate(backRightDriveV);
    backRightDriveEncoderSim.setDistance(backRightDriveEncoderSim.getDistance() + backRightDriveV * dt);
    backRightTurnEncoderSim.setDistance(backRightTurnEncoderSim.getDistance() + backRightTurnV * dt);

    frontLeftDriveEncoderPub.set(frontLeftDriveEncoderSim.getDistance());
    frontLeftTurnEncoderPub.set(frontLeftTurnEncoderSim.getDistance());
    frontRightDriveEncoderPub.set(frontRightDriveEncoderSim.getDistance());
    frontRightTurnEncoderPub.set(frontRightTurnEncoderSim.getDistance());
    backLeftDriveEncoderPub.set(backLeftDriveEncoderSim.getDistance());
    backLeftTurnEncoderPub.set(backLeftTurnEncoderSim.getDistance());
    backRightDriveEncoderPub.set(backRightDriveEncoderSim.getDistance());
    backRightTurnEncoderPub.set(backRightTurnEncoderSim.getDistance());

    frontLeftDriveEncoderRatePub.set(frontLeftDriveEncoderSim.getRate());
    // frontLeftTurnEncoderRatePub.set(frontLeftTurnEncoderSim.getRate());
    frontRightDriveEncoderRatePub.set(frontRightDriveEncoderSim.getRate());
    // frontRightTurnEncoderRatePub.set(frontRightTurnEncoderSim.getRate());
    backLeftDriveEncoderRatePub.set(backLeftDriveEncoderSim.getRate());
    // backLeftTurnEncoderRatePub.set(backLeftTurnEncoderSim.getRate());
    backRightDriveEncoderRatePub.set(backRightDriveEncoderSim.getRate());
    // backRightTurnEncoderRatePub.set(backRightTurnEncoderSim.getRate());

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

    fieldPub.set(new double[] {
        newPose.getX(),
        newPose.getY(),
        newPose.getRotation().getDegrees()
    });
  }
}
