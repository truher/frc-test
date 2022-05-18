package frc.robot.subsystems;

import java.util.ArrayList;
import java.util.List;

import edu.wpi.first.math.trajectory.TrapezoidProfile;
import edu.wpi.first.wpilibj2.command.Subsystem;
import edu.wpi.first.wpilibj2.command.SubsystemBase;

public class SubsystemGroup extends SubsystemBase {
    private final List<ExampleSubsystem2> m_subsystems;
    private final List<ExampleSubsystem3> m_drives;

    public SubsystemGroup() {
        m_subsystems = new ArrayList<ExampleSubsystem2>();
        m_drives = new ArrayList<ExampleSubsystem3>();
        for (int i = 0; i < 6; i = i + 2) {
            ExampleSubsystem2 steer = new ExampleSubsystem2(i);
            steer.enable();
            m_subsystems.add(steer);
            ExampleSubsystem3 drive = new ExampleSubsystem3(i + 1);
            drive.enable();
            m_drives.add(drive);
        }
    }

    public void setGoal(TrapezoidProfile.State goal) {
        for (ExampleSubsystem2 subsystem : m_subsystems) {
            subsystem.setGoal(goal.position);
        }
        for (ExampleSubsystem3 drive : m_drives) {
            drive.setSetpoint(goal.velocity);
        }
    }

    @Override
    public void periodic() {
        // do this here because the scheduler only knows
        // about the top level subsystem
        for (Subsystem subsystem : m_subsystems) {
            subsystem.periodic();
        }
        for (Subsystem subsystem: m_drives) {
            subsystem.periodic();
        }
    }
}
