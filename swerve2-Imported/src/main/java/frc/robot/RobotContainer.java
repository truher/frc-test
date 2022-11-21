package frc.robot;

import edu.wpi.first.util.sendable.Sendable;
import edu.wpi.first.util.sendable.SendableBuilder;
import edu.wpi.first.wpilibj.XboxController;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;
import frc.robot.subsystems.ExampleSubsystem;
import edu.wpi.first.wpilibj2.command.Command;
import edu.wpi.first.wpilibj2.command.RunCommand;

public class RobotContainer implements Sendable {
    private final ExampleSubsystem m_exampleSubsystem;
    private final XboxController m_driverController;

    public RobotContainer() {
        configureButtonBindings();
        m_driverController = new XboxController(0);
        m_exampleSubsystem = new ExampleSubsystem();
        m_exampleSubsystem.setDefaultCommand(new RunCommand(
                () -> {
                },
                m_exampleSubsystem));
        SmartDashboard.putData("robot container", this);
    }

    private void configureButtonBindings() {
    }

    public Command getAutonomousCommand() {
        return new RunCommand(() -> {
        }, m_exampleSubsystem);
    }

    public void runTest() {
        boolean rearLeft = m_driverController.getAButton();
        boolean rearRight = m_driverController.getBButton();
        boolean frontLeft = m_driverController.getXButton();
        boolean frontRight = m_driverController.getYButton();
        double driveControl = m_driverController.getLeftY();
        double turnControl = m_driverController.getLeftX();
        double[][] desiredOutputs = {
                { frontLeft ? driveControl : 0, frontLeft ? turnControl : 0 },
                { frontRight ? driveControl : 0, frontRight ? turnControl : 0 },
                { rearLeft ? driveControl : 0, rearLeft ? turnControl : 0 },
                { rearRight ? driveControl : 0, rearRight ? turnControl : 0 }
        };
        m_exampleSubsystem.test(desiredOutputs);
    }

    @Override
    public void initSendable(SendableBuilder builder) {
        builder.setSmartDashboardType("container");
        builder.addDoubleProperty("right y", () -> m_driverController.getRightY(), null);
        builder.addDoubleProperty("right x", () -> m_driverController.getRightX(), null);
        builder.addDoubleProperty("left x", () -> m_driverController.getLeftX(), null);
    }

    // this is the joystick version. i like the xbox version better, but it took
    // awhile to figure out which button was which.
    // @Override
    // public void initSendable(SendableBuilder builder) {
    // builder.setSmartDashboardType("container");
    // builder.addDoubleProperty("X (positive right)", () -> m_joystick.getX(),
    // null);
    // builder.addDoubleProperty("Y (positive back)", () -> m_joystick.getY(),
    // null);
    // builder.addDoubleProperty("Twist (positive right)", () ->
    // m_joystick.getTwist(), null);
    // builder.addDoubleProperty("Throttle (positive back)", () ->
    // m_joystick.getThrottle(), null);
    // builder.addDoubleProperty("Button Count", () -> m_joystick.getButtonCount(),
    // null);
    // // these assignments all seem like they're maybe off by one?
    // builder.addBooleanProperty("Button 0 (?)", () -> m_joystick.getRawButton(0),
    // null);
    // builder.addBooleanProperty("Button 1 (trigger)", () ->
    // m_joystick.getRawButton(1), null);
    // builder.addBooleanProperty("Button 2 (thumb)", () ->
    // m_joystick.getRawButton(2), null);
    // builder.addBooleanProperty("Button 3 (top left rear)", () ->
    // m_joystick.getRawButton(3), null);
    // builder.addBooleanProperty("Button 4 (top right rear)", () ->
    // m_joystick.getRawButton(4), null);
    // builder.addBooleanProperty("Button 5 (top left front)", () ->
    // m_joystick.getRawButton(5), null);
    // builder.addBooleanProperty("Button 6 (top right front)", () ->
    // m_joystick.getRawButton(6), null);
    // builder.addBooleanProperty("Button 7 (base front left)", () ->
    // m_joystick.getRawButton(7), null);
    // builder.addBooleanProperty("Button 8 (base front right)", () ->
    // m_joystick.getRawButton(8), null);
    // builder.addBooleanProperty("Button 9 (base middle left)", () ->
    // m_joystick.getRawButton(9), null);
    // builder.addBooleanProperty("Button 10 (base middle right)", () ->
    // m_joystick.getRawButton(10), null);
    // builder.addBooleanProperty("Button 11 (base rear left)", () ->
    // m_joystick.getRawButton(11), null);
    // }
}
