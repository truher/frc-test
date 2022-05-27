package frc.robot.subsystems;

import edu.wpi.first.math.geometry.Rotation2d;
import edu.wpi.first.math.kinematics.SwerveModuleState;

public class Module {
    public final Turner m_steer;
    public final Driver m_drive;

    /**
     * Offset is measured in sensor units, [0,1]. To adjust the module zero in the
     * positive (anticlockwise) direction, reduce the offset.
     * 
     * @param steerChannel
     * @param steerOffset
     * @param driveChannel
     */
    public Module(int steerChannel, double steerOffset, int driveChannel) {
        m_steer = new Turner(steerChannel, steerOffset);
        m_drive = new Driver(driveChannel);
    }

    public SwerveModuleState getState() {
        int driveMetersPerSec = 0;
        int angle = 0;
        return new SwerveModuleState(driveMetersPerSec, new Rotation2d(angle));
        // return new SwerveModuleState(m_driveEncoder.getRate(), new
        // Rotation2d(m_turningEncoder.get()));
    }

    public void setDesiredState(SwerveModuleState desiredState) {
        //SwerveModuleState state = SwerveModuleState.optimize(desiredState,
        //        new Rotation2d(m_steer.getMeasurement()));
        SwerveModuleState state = desiredState;
        m_drive.setSetpoint(state.speedMetersPerSecond);
        m_steer.setGoal(state.angle.getRadians());
    }

}
