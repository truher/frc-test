// Copyright (c) FIRST and other WPILib contributors.
// Open Source Software; you can modify and/or share it under the terms of
// the WPILib BSD license file in the root directory of this project.

package frc.robot.commands;

import frc.robot.subsystems.SubsystemGroup;
import edu.wpi.first.math.trajectory.TrapezoidProfile;
import edu.wpi.first.wpilibj.XboxController;
import edu.wpi.first.wpilibj2.command.CommandBase;

public class ExampleCommand extends CommandBase {
  private final XboxController m_input;
  private final SubsystemGroup m_subsystem;

  public ExampleCommand(XboxController input, SubsystemGroup subsystem) {
    m_input = input;
    m_subsystem = subsystem;
    addRequirements(subsystem);
  }

  @Override
  public void execute() {
    double desiredPosition = (m_input.getRightX() + 1) / 2;
    double desiredVelocity = (m_input.getLeftX() + 1) / 2;
    TrapezoidProfile.State state = new TrapezoidProfile.State(desiredPosition, desiredVelocity);
    m_subsystem.setGoal(state);
  }
}
