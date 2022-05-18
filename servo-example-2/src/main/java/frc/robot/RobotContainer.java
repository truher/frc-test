// Copyright (c) FIRST and other WPILib contributors.
// Open Source Software; you can modify and/or share it under the terms of
// the WPILib BSD license file in the root directory of this project.

package frc.robot;

import edu.wpi.first.wpilibj.XboxController;
import frc.robot.commands.ExampleCommand;
import frc.robot.subsystems.SubsystemGroup;
import edu.wpi.first.wpilibj2.command.Command;

public class RobotContainer {
  private final XboxController m_driverController;
  private final SubsystemGroup m_subsystemGroup;
  private final Command m_teleopCommand;

  public RobotContainer() {
    m_driverController = new XboxController(1);
    m_subsystemGroup = new SubsystemGroup();
    m_teleopCommand = new ExampleCommand(m_driverController, m_subsystemGroup);
  }

  public Command getTeleopCommand() {
    return m_teleopCommand;
  }
}
