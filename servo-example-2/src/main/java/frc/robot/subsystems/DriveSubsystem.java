package frc.robot.subsystems;

import edu.wpi.first.math.controller.PIDController;
import edu.wpi.first.math.filter.LinearFilter;
import edu.wpi.first.util.sendable.SendableBuilder;
import edu.wpi.first.wpilibj.DutyCycleEncoder;
import edu.wpi.first.wpilibj.RobotController;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;
import edu.wpi.first.wpilibj2.command.PIDSubsystem;

public class DriveSubsystem extends PIDSubsystem {
    public final Parallax360 m_motor;
    public final DutyCycleEncoder m_input;
    private final LinearFilter m_dx;
    private final LinearFilter m_dt;

    public DriveSubsystem(int channel) {
        super(new PIDController(1, 0, 0.1), 0);
        setName(String.format("Drive %d", channel));
        m_motor = new Parallax360(String.format("Drive Motor %d", channel), channel);
        m_input = new DutyCycleEncoder(channel);
        m_input.setDutyCycleRange(0.027, 0.971);
        m_input.setDistancePerRotation(-1);
        //getController().enableContinuousInput(0, 1);
        // these gains yield dx per period, averaged over a few periods.
        double[] gains = new double[5];
        gains[0] = 0.25;
        gains[4] = -0.25;
        m_dx = new LinearFilter(gains, new double[0]);
        m_dt = new LinearFilter(gains, new double[0]);
        SmartDashboard.putData(getName(), this);
    }

    public double motorState() {
        return m_motor.get();
    }

    public double getSetpoint() {
        return getController().getSetpoint();
    }

    public double getGetPositionError() {
        return getController().getPositionError();
    }

    public double getGetVelocityError() {
        return getController().getVelocityError();
    }

    public boolean isConnected() {
        return m_input.isConnected();
    }

    public double getAngle() {
        return m_input.getAbsolutePosition();
    }

    public double getDistance() {
        return m_input.getDistance();
    }

    @Override
    protected void useOutput(double output, double setpoint) {
        m_motor.set(output); // positive == counterclockwise.
        //m_motor.set(0);
    }

    // for this system the variable of interest is velocity in radians/sec
    // or maybe turns/sec?
    @Override
    protected double getMeasurement() {
        return 1e6 * m_dx.calculate(m_input.getDistance()) /
                m_dt.calculate(RobotController.getFPGATime());
    }

    @Override
    public void initSendable(SendableBuilder builder) {
        super.initSendable(builder);
        builder.addDoubleProperty("measurement", this::getMeasurement, null);
        builder.addDoubleProperty("motor state", this::motorState, null);
        builder.addDoubleProperty("setpoint", this::getSetpoint, null);
        builder.addBooleanProperty("connected", this::isConnected, null);
        builder.addDoubleProperty("distance", this::getDistance, null);
        builder.addDoubleProperty("position error", this::getGetPositionError, null);
        builder.addDoubleProperty("velocity error", this::getGetVelocityError, null);
    }
}
