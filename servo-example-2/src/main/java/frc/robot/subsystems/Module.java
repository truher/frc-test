package frc.robot.subsystems;

public class Module {
    public final Turner m_steer;
    public final Driver m_drive;

    public Module(int steerChannel, double steerOffset, int driveChannel) {
        m_steer = new Turner(steerChannel, steerOffset);
        m_drive = new Driver(driveChannel);
    }

}
