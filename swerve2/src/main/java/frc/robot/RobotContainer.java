// Copyright (c) FIRST and other WPILib contributors.
// Open Source Software; you can modify and/or share it under the terms of
// the WPILib BSD license file in the root directory of this project.

package frc.robot;

import edu.wpi.first.util.sendable.Sendable;
import edu.wpi.first.util.sendable.SendableBuilder;
import edu.wpi.first.wpilibj.GenericHID;
import edu.wpi.first.wpilibj.Joystick;
import edu.wpi.first.wpilibj.XboxController;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;
import frc.robot.commands.ExampleCommand;
import frc.robot.subsystems.ExampleSubsystem;
import edu.wpi.first.wpilibj2.command.Command;
import edu.wpi.first.wpilibj2.command.RunCommand;

/**
 * This class is where the bulk of the robot should be declared. Since
 * Command-based is a
 * "declarative" paradigm, very little robot logic should actually be handled in
 * the {@link Robot}
 * periodic methods (other than the scheduler calls). Instead, the structure of
 * the robot (including
 * subsystems, commands, and button mappings) should be declared here.
 */
public class RobotContainer implements Sendable {
    private final ExampleSubsystem m_exampleSubsystem;
    private final Joystick m_joystick;

    public RobotContainer() {
        configureButtonBindings();
        m_joystick = new Joystick(2);
        m_exampleSubsystem = new ExampleSubsystem();
        m_exampleSubsystem.setDefaultCommand(new RunCommand(
                () -> m_exampleSubsystem.doit(m_joystick.getX()),
                m_exampleSubsystem));
        SmartDashboard.putData("robot container", this);
    }

    /**
     * Use this method to define your button->command mappings. Buttons can be
     * created by
     * instantiating a {@link GenericHID} or one of its subclasses ({@link
     * edu.wpi.first.wpilibj.Joystick} or {@link XboxController}), and then passing
     * it to a {@link
     * edu.wpi.first.wpilibj2.command.button.JoystickButton}.
     */
    private void configureButtonBindings() {
    }

    @Override
    public void initSendable(SendableBuilder builder) {
        builder.setSmartDashboardType("container");
        builder.addDoubleProperty("X (positive right)", () -> m_joystick.getX(), null);
        builder.addDoubleProperty("Y (positive back)", () -> m_joystick.getY(), null);
        builder.addDoubleProperty("Twist (positive right)", () -> m_joystick.getTwist(), null);
        builder.addDoubleProperty("Throttle (positive back)", () -> m_joystick.getThrottle(), null);
        builder.addDoubleProperty("Button Count", () -> m_joystick.getButtonCount(), null);
        // these assignments all seem like they're maybe off by one?
        builder.addBooleanProperty("Button 0 (?)", () -> m_joystick.getRawButton(0), null);
        builder.addBooleanProperty("Button 1 (trigger)", () -> m_joystick.getRawButton(1), null);
        builder.addBooleanProperty("Button 2 (thumb)", () -> m_joystick.getRawButton(2), null);
        builder.addBooleanProperty("Button 3 (top left rear)", () -> m_joystick.getRawButton(3), null);
        builder.addBooleanProperty("Button 4 (top right rear)", () -> m_joystick.getRawButton(4), null);
        builder.addBooleanProperty("Button 5 (top left front)", () -> m_joystick.getRawButton(5), null);
        builder.addBooleanProperty("Button 6 (top right front)", () -> m_joystick.getRawButton(6), null);
        builder.addBooleanProperty("Button 7 (base front left)", () -> m_joystick.getRawButton(7), null);
        builder.addBooleanProperty("Button 8 (base front right)", () -> m_joystick.getRawButton(8), null);
        builder.addBooleanProperty("Button 9 (base middle left)", () -> m_joystick.getRawButton(9), null);
        builder.addBooleanProperty("Button 10 (base middle right)", () -> m_joystick.getRawButton(10), null);
        builder.addBooleanProperty("Button 11 (base rear left)", () -> m_joystick.getRawButton(11), null);
    }
}
