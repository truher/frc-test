package frc.robot.subsystems;

import java.util.ArrayList;
import java.util.List;

import edu.wpi.first.wpilibj2.command.Subsystem;
import edu.wpi.first.wpilibj2.command.SubsystemBase;

public class SubsystemGroup extends SubsystemBase {
    private final List<ExampleSubsystem2> m_subsystems;

    public SubsystemGroup() {
        m_subsystems = new ArrayList<ExampleSubsystem2>();
        for (int i = 0; i < 6; ++i) {
            ExampleSubsystem2 sys = new ExampleSubsystem2(i);
            sys.enable();
            m_subsystems.add(sys);
        }
    }

    public void setGoal(double goal) {
        for (ExampleSubsystem2 subsystem : m_subsystems) {
            subsystem.setGoal(goal);
        }
    }

    @Override
    public void periodic() {
        // do this here because the scheduler only knows
        // about the top level subsystem
        for (Subsystem subsystem : m_subsystems) {
            subsystem.periodic();
        }
    }
}
