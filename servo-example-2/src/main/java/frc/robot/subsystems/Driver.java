package frc.robot.subsystems;

import edu.wpi.first.math.controller.PIDController;
import edu.wpi.first.math.filter.LinearFilter;
import edu.wpi.first.util.sendable.SendableBuilder;
import edu.wpi.first.wpilibj.DutyCycleEncoder;
import edu.wpi.first.wpilibj.RobotController;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;
import edu.wpi.first.wpilibj2.command.PIDSubsystem;
import frc.motorcontrol.Parallax360;

public class Driver extends PIDSubsystem {
    private static final double kV = 0.4; // turns/sec
    private static final int kP = 0;
    private static final double kI = 0;
    private static final double kD = 0.1;

    public final Parallax360 m_motor;
    public final DutyCycleEncoder m_input;
    private final LinearFilter m_dx;
    private final LinearFilter m_dt;
    private double m_currentDx; // position units = meters
    private double m_currentDt; // time units = microsec
    private double m_feedForwardOutput;
    private double m_controllerOutput;
    private double m_velocity;
    private double m_setpointVelocity; // meters per second
    private double m_userInput; // [-1,1]
    private double m_setpointPosition;

    public Driver(int channel) {
        super(new PIDController(kP, kI, kD), 0);
        setName(String.format("Drive %d", channel));
        m_motor = new Parallax360(String.format("Drive Motor %d", channel), channel);
        m_input = new DutyCycleEncoder(channel);
        m_input.setDutyCycleRange(0.027, 0.971);
        m_input.setDistancePerRotation(-1 * Math.PI * 0.07); // wheels are 70mm diameter
        // getController().enableContinuousInput(0, 1);
        // these gains yield dx per period, averaged over a few periods.
        double[] gains = new double[5];
        gains[0] = 0.25;
        gains[4] = -0.25;
        m_dx = new LinearFilter(gains, new double[0]);
        m_dt = new LinearFilter(gains, new double[0]);
        enable();
        SmartDashboard.putData(getName(), this);
    }

    /**
     * The example swerve code wants to use this to set *speed*, but i'm using
     * the PID here to control *position*.  TODO: make speed PID work.
     */
    @Override
    public void setSetpoint(double setpoint) {
        super.setSetpoint(setpoint);
    }

    /**
     * Distance in meters.
     */
    public double getDistance() {
        return m_input.getDistance();
    }

    public void setMotorOutput(double value) {
        m_motor.set(value);
    }

    @Override
    protected void useOutput(double output, double setpoint) {
        m_controllerOutput = output;
        m_feedForwardOutput = kV * m_setpointVelocity / (0.02 * 0.07);
        setMotorOutput(m_controllerOutput + m_feedForwardOutput);
        // m_motor.set(output); // positive == counterclockwise.
        // m_motor.set(0);
    }

    @Override
    public void periodic() {
        m_setpointPosition = getDistance() + m_setpointVelocity;
        setSetpoint(m_setpointPosition);
        super.periodic();
    }

    /**
     * Current position in meters
     */
    @Override
    protected double getMeasurement() {
        double distance = getDistance();
        m_currentDt = m_dt.calculate(RobotController.getFPGATime());
        m_currentDx = m_dx.calculate(distance);
        m_velocity = 1e6 * m_currentDx / m_currentDt;
        return distance;
    }

    /**
     * an old control method, input [-1,1]
     */
    public void setThrottle(double input) {
        m_userInput = input;
        // 1.7 turn/sec, 70mm, 0.02 sec
        m_setpointVelocity = input * 1.7 * 0.07 * 0.02;
    }

    @Override
    public void initSendable(SendableBuilder builder) {
        super.initSendable(builder);
        builder.addDoubleProperty("velocity", () -> m_velocity, null);
        builder.addDoubleProperty("motor state", this::motorState, null);
        builder.addDoubleProperty("setpoint", this::getSetpoint, null);
        builder.addDoubleProperty("distance", this::getDistance, null);
        builder.addDoubleProperty("position error", this::getGetPositionError, null);
        builder.addDoubleProperty("velocity error", this::getGetVelocityError, null);
        builder.addDoubleProperty("current dt", () -> m_currentDt, null);
        builder.addDoubleProperty("current dx", () -> m_currentDx, null);
        builder.addDoubleProperty("controller output", () -> m_controllerOutput, null);
        builder.addDoubleProperty("feed forward output", () -> m_feedForwardOutput, null);
        builder.addDoubleProperty("user input", () -> m_userInput, null);
        builder.addDoubleProperty("setpoint velocity", () -> m_setpointVelocity, null);
        builder.addDoubleProperty("setpoint position", () -> m_setpointPosition, null);
    }

    // methods below are just for logging.

    /**
     * meters
     */
    public double getGetPositionError() {
        return getController().getPositionError();
    }

    /**
     * meters per sec
     */
    public double getGetVelocityError() {
        return getController().getVelocityError();
    }

    /**
     * Motor units [-1, 1]
     */
    public double motorState() {
        return m_motor.get();
    }

    /**
     * Setpoint in meters.
     */
    public double getSetpoint() {
        return getController().getSetpoint();
    }


}
