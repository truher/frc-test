package frc.robot.subsystems;

import edu.wpi.first.math.controller.PIDController;
import edu.wpi.first.math.filter.LinearFilter;
import edu.wpi.first.wpilibj.DutyCycleEncoder;
import edu.wpi.first.wpilibj.RobotController;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;
import edu.wpi.first.wpilibj2.command.PIDSubsystem;

public class ExampleSubsystem3 extends PIDSubsystem {
    public final Parallax360 m_motor;
    public final DutyCycleEncoder m_input;
    private final LinearFilter m_filter;
    private double m_previousValue;
    private long m_previousTimeNs;

    public ExampleSubsystem3(int channel) {
        super(new PIDController(4, 1, 0.1), 0);
        setName(String.format("Example Subsystem 3 %d", channel));
        m_motor = new Parallax360(String.format("Drive Motor %d", channel), channel);
        m_input = new DutyCycleEncoder(channel);
        m_input.setDutyCycleRange(0.027, 0.971);
        getController().enableContinuousInput(0, 1);
        m_filter = LinearFilter.singlePoleIIR(10, 1);
        SmartDashboard.putData(getName(), this);
    }

    @Override
    protected void useOutput(double output, double setpoint) {
        m_motor.set(output);
    }

    @Override
    protected double getMeasurement() {
        double newValue = m_input.get();
        double deltaValue = newValue - m_previousValue;
        long newTimeNs = RobotController.getFPGATime();
        double deltaTimeNs = newTimeNs - m_previousTimeNs;
        double rateTurnsPerSec = 1e6 * deltaValue / deltaTimeNs;
        m_previousValue = newValue;
        m_previousTimeNs = newTimeNs;
        return m_filter.calculate(rateTurnsPerSec);
    }
}
