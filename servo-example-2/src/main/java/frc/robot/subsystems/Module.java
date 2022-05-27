package frc.robot.subsystems;

import edu.wpi.first.math.geometry.Rotation2d;
import edu.wpi.first.math.kinematics.SwerveModuleState;

public class Module {
    public final Turner m_steer;
    public final Driver m_drive;

    public Module(int steerChannel, double steerOffset, int driveChannel) {
        m_steer = new Turner(steerChannel, steerOffset);
        m_drive = new Driver(driveChannel);
    }

    public void setDesiredState(SwerveModuleState desiredState) {
        SwerveModuleState state = SwerveModuleState.optimize(desiredState,
                new Rotation2d(m_steer.getMeasurement()));
        m_drive.setSetpoint(state.speedMetersPerSecond);
        m_steer.setGoal(state.angle.getRadians());
    }

}
