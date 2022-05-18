// Copyright (c) FIRST and other WPILib contributors.
// Open Source Software; you can modify and/or share it under the terms of
// the WPILib BSD license file in the root directory of this project.

package frc.robot.subsystems;

import edu.wpi.first.util.sendable.SendableBuilder;
import edu.wpi.first.wpilibj.DutyCycleEncoder;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;
import edu.wpi.first.wpilibj2.command.SubsystemBase;

public class ExampleSubsystem2 extends SubsystemBase {
  public final Parallax360 m_motor;
  public final DutyCycleEncoder m_input;

  public ExampleSubsystem2() {
    m_motor = new Parallax360("example 2", 1);
    m_input = new DutyCycleEncoder(1);
    m_input.setDutyCycleRange(0.027, 0.971);
    SmartDashboard.putData("Example Subsystem 2", this);
  }

  public void move(double input) {
    m_motor.set(input);
  }

  public double motorState() {
    return m_motor.get();
  }

  public double sensor() {
    return m_input.getAbsolutePosition();
  }

  public boolean isConnected() {
    return m_input.isConnected();
  }

  @Override
  public void periodic() {
    // This method will be called once per scheduler run
  }

  @Override
  public void simulationPeriodic() {
    // This method will be called once per scheduler run during simulation
  }

  @Override
  public void initSendable(SendableBuilder builder) {
    super.initSendable(builder);
    builder.addDoubleProperty("motor state", this::motorState, null);
    builder.addDoubleProperty("input", this::sensor, null);
    builder.addBooleanProperty("connected", this::isConnected, null);
  }
}
