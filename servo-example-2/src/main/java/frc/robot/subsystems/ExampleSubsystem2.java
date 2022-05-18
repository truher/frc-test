// Copyright (c) FIRST and other WPILib contributors.
// Open Source Software; you can modify and/or share it under the terms of
// the WPILib BSD license file in the root directory of this project.

package frc.robot.subsystems;

import edu.wpi.first.math.controller.ProfiledPIDController;
import edu.wpi.first.math.trajectory.TrapezoidProfile;
import edu.wpi.first.math.trajectory.TrapezoidProfile.State;
import edu.wpi.first.util.sendable.SendableBuilder;
import edu.wpi.first.wpilibj.DutyCycleEncoder;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;
import edu.wpi.first.wpilibj2.command.ProfiledPIDSubsystem;

public class ExampleSubsystem2 extends ProfiledPIDSubsystem {
  public final Parallax360 m_motor;
  public final DutyCycleEncoder m_input;
  // public final SimpleMotorFeedforward m_feedForward;

  public ExampleSubsystem2() {
    super(
        new ProfiledPIDController(4, 1, 0.1,
            new TrapezoidProfile.Constraints(100, 10)),
        0);
    m_motor = new Parallax360("example 2", 1);
    m_input = new DutyCycleEncoder(1);
    m_input.setDutyCycleRange(0.027, 0.971);
    getController().enableContinuousInput(0, 1);
    // m_feedForward = new SimpleMotorFeedforward(1, 1);
    SmartDashboard.putData("Example Subsystem 2", this);
  }

  public void move(double input) {
    setGoal(input);
  }

  public double motorState() {
    return m_motor.get();
  }

  public double sensor() {
    return -1 * m_input.getAbsolutePosition();
  }

  public boolean isConnected() {
    return m_input.isConnected();
  }

  public double getGoal() {
    return getController().getGoal().position;
  }

  @Override
  protected void useOutput(double output, State setpoint) {
    // double feedForward = m_feedForward.calculate(setpoint.position,
    // setpoint.velocity);
    // m_motor.set(output + feedForward);
    m_motor.set(output);
  }

  @Override
  protected double getMeasurement() {
    return sensor();
  }

  public double error() {
    return getController().getPositionError();
  }

  @Override
  public void initSendable(SendableBuilder builder) {
    super.initSendable(builder);
    builder.addDoubleProperty("motor state", this::motorState, null);
    builder.addDoubleProperty("input", this::sensor, null);
    builder.addDoubleProperty("goal", this::getGoal, null);
    builder.addBooleanProperty("connected", this::isConnected, null);
    builder.addDoubleProperty("error", this::error, null);
  }
}
