// Copyright (c) FIRST and other WPILib contributors.
// Open Source Software; you can modify and/or share it under the terms of
// the WPILib BSD license file in the root directory of this project.

package frc.robot.subsystems;

import edu.wpi.first.math.controller.ProfiledPIDController;
import edu.wpi.first.math.controller.SimpleMotorFeedforward;
import edu.wpi.first.math.trajectory.TrapezoidProfile;
import edu.wpi.first.math.trajectory.TrapezoidProfile.State;
import edu.wpi.first.util.sendable.SendableBuilder;
import edu.wpi.first.wpilibj.DutyCycleEncoder;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;
import edu.wpi.first.wpilibj2.command.ProfiledPIDSubsystem;

public class TurningSubsystem extends ProfiledPIDSubsystem {
  public final Parallax360 m_motor;
  public final DutyCycleEncoder m_input;
  public final SimpleMotorFeedforward m_feedForward;

  public TurningSubsystem(int channel) {
    super(
        new ProfiledPIDController(3, 0.5, 1,
            new TrapezoidProfile.Constraints(5, 0.5)),
        0);
    setName(String.format("Turning %d", channel));
    m_motor = new Parallax360(String.format("Turn Motor %d", channel), channel);
    m_input = new DutyCycleEncoder(channel);
    m_input.setDutyCycleRange(0.027, 0.971);
    getController().enableContinuousInput(0, 1);
    m_feedForward = new SimpleMotorFeedforward(0.1, 0.01);
    SmartDashboard.putData(getName(), this);
  }

  public double motorState() {
    return m_motor.get();
  }

  public boolean isConnected() {
    return m_input.isConnected();
  }

  public double getGoalPosition() {
    return getController().getGoal().position;
  }

  public double getGoalVelocity() {
    return getController().getGoal().velocity;
  }

  @Override
  protected void useOutput(double output, State setpoint) {
    double feedForward = m_feedForward.calculate(setpoint.velocity);
    m_motor.set(output + feedForward);
   // m_motor.set(output);
//    m_motor.set(0);
  }

  @Override
  public double getMeasurement() {
    return -1 * m_input.getAbsolutePosition();
  }

  public double error() {
    return getController().getPositionError();
  }

  public double getGetPositionError() {
    return getController().getPositionError();
}

public double getGetVelocityError() {
    return getController().getVelocityError();
}

  public double getDistance() {
    return m_input.getDistance();
}

  @Override
  public void initSendable(SendableBuilder builder) {
    super.initSendable(builder);
    builder.addDoubleProperty("motor state", this::motorState, null);
    builder.addDoubleProperty("measurement", this::getMeasurement, null);
    builder.addDoubleProperty("goal position", this::getGoalPosition, null);
    builder.addDoubleProperty("goal velocity", this::getGoalVelocity, null);
    builder.addBooleanProperty("connected", this::isConnected, null);
    builder.addDoubleProperty("position error", this::getGetPositionError, null);
    builder.addDoubleProperty("distance", this::getDistance, null);
    builder.addDoubleProperty("velocity error", this::getGetVelocityError, null);
  }
}
