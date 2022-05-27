package frc.robot.subsystems;

import edu.wpi.first.wpilibj2.command.SubsystemBase;

public class Drivetrain extends SubsystemBase {
    private final Module[] m_modules;

    public Drivetrain() {
        m_modules = new Module[] {
                new Module(0, 0.806, 1),
                new Module(2, 0.790, 3),
                new Module(4, 0.185, 5)
        };
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
