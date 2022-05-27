package frc.robot.subsystems;

import edu.wpi.first.math.geometry.Rotation2d;
import edu.wpi.first.math.geometry.Translation2d;
import edu.wpi.first.math.kinematics.ChassisSpeeds;
import edu.wpi.first.math.kinematics.SwerveDriveKinematics;
import edu.wpi.first.math.kinematics.SwerveDriveOdometry;
import edu.wpi.first.math.kinematics.SwerveModuleState;
import edu.wpi.first.wpilibj2.command.SubsystemBase;
import frc.sensors.Angle;
import frc.sensors.FusedHeading;

public class Drivetrain extends SubsystemBase {
    private static final SwerveDriveKinematics kDriveKinematics = new SwerveDriveKinematics(
            new Translation2d(0.11, 0),
            new Translation2d(-0.11, 0.14),
            new Translation2d(-0.11, -0.14));
    private final Module[] m_modules;
    private final SwerveDriveOdometry m_odometry;
    private final Angle m_gyro;

    public Drivetrain() {
        m_modules = new Module[] {
                new Module(0, 0.806, 1),
                new Module(2, 0.790, 3),
                new Module(4, 0.185, 5)
        };
        m_odometry = new SwerveDriveOdometry(kDriveKinematics, new Rotation2d(0));
        m_gyro = new FusedHeading();
    }

    public void drive(double xSpeed, double ySpeed, double rot) {
        SwerveModuleState[] swerveModuleStates =
         kDriveKinematics.toSwerveModuleStates(ChassisSpeeds.fromFieldRelativeSpeeds(xSpeed, ySpeed, rot, new Rotation2d(m_gyro.getAngle())));
         SwerveDriveKinematics.desaturateWheelSpeeds(swerveModuleStates, 0.2);
         m_modules[0].setDesiredState(swerveModuleStates[0]);
         m_modules[1].setDesiredState(swerveModuleStates[1]);
         m_modules[2].setDesiredState(swerveModuleStates[2]);
    }

    // for drone mode, set angle goal directly
    public void setTurnGoal(double input) {
        for (Module module : m_modules) {
            module.m_steer.setGoal(input);
        }
    }

    // for normal mode, increment angle goal
    public void setTurnRate(double input) {
        for (Module module : m_modules) {
            module.m_steer.setTurnRate(input);
        }
    }

    public void setThrottle(double input) {
        for (Module module : m_modules) {
            module.m_drive.setThrottle(input);
        }
    }

    public void initialize() {
        m_modules[0].m_steer.initialize();
        m_modules[1].m_steer.initialize();
        m_modules[2].m_steer.initialize();
    }
}
