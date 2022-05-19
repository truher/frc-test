package frc.robot.subsystems;

import java.util.ArrayList;
import java.util.List;

import edu.wpi.first.wpilibj2.command.Subsystem;
import edu.wpi.first.wpilibj2.command.SubsystemBase;

public class SubsystemGroup extends SubsystemBase {
    private final List<TurningSubsystem> m_steers;
    private final List<DriveSubsystem> m_drives;

    public SubsystemGroup() {
        m_steers = new ArrayList<TurningSubsystem>();
        m_drives = new ArrayList<DriveSubsystem>();
        for (int i = 0; i < 6; i = i + 2) {
            TurningSubsystem steer = new TurningSubsystem(i);
            steer.enable(); 
            m_steers.add(steer);
            DriveSubsystem drive = new DriveSubsystem(i + 1);
            drive.enable();
            m_drives.add(drive);
        }
    }

    public void setTurningGoal(double goal) {
        for (TurningSubsystem subsystem : m_steers) {
            subsystem.setGoal(goal);
        }
    }
    public void setDriveGoal(double goal) {
        for (DriveSubsystem drive : m_drives) {
            drive.setSetpoint(goal*3);
        }
    }

    @Override
    public void periodic() {
        // do this here because the scheduler only knows
        // about the top level subsystem
        for (Subsystem steer : m_steers) {
            steer.periodic();
        }
        for (Subsystem drive: m_drives) {
            drive.periodic();
        }
    }
}
