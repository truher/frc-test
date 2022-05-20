// Copyright (c) FIRST and other WPILib contributors.
// Open Source Software; you can modify and/or share it under the terms of
// the WPILib BSD license file in the root directory of this project.

package frc.robot.commands;

import frc.robot.subsystems.SubsystemGroup;
import edu.wpi.first.util.sendable.SendableBuilder;
import edu.wpi.first.wpilibj.XboxController;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;
import edu.wpi.first.wpilibj2.command.CommandBase;

public class ExampleCommand extends CommandBase {
  private final XboxController m_input;
  private final SubsystemGroup m_subsystem;

  private double m_steer_input;
  private double m_drive_input;

  public ExampleCommand(XboxController input, SubsystemGroup subsystem) {
    m_input = input;
    m_subsystem = subsystem;
    addRequirements(subsystem);
    SmartDashboard.putData("Example Command", this);
  }

  @Override
  public void execute() {
    m_steer_input = (m_input.getLeftX() + 1) / 2;
    boolean button = m_input.getAButton();
    if (button) {
      m_subsystem.setTurningGoal(m_steer_input/2);
    } else {
      m_subsystem.setTurningGoal(m_steer_input);
    }

    m_drive_input = m_input.getRightX();
    m_subsystem.setDriveGoal(m_drive_input);

    // double desiredPosition = (m_input.getRightX() + 1) / 2;
    // double desiredVelocity = (m_input.getLeftX() + 1) / 2;
    // m_subsystem.setGoal(
    // new TrapezoidProfile.State(desiredPosition, desiredVelocity));
  }

  public double getSteerInput() {
    return m_steer_input;
  }

  public double getDriveInput() {
    return m_drive_input;
  }

  @Override
  public void initSendable(SendableBuilder builder) {
    super.initSendable(builder);
    builder.addDoubleProperty("steer input", this::getSteerInput, null);
    builder.addDoubleProperty("drive input", this::getDriveInput, null);
  }
}
