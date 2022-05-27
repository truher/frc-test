// Copyright (c) FIRST and other WPILib contributors.
// Open Source Software; you can modify and/or share it under the terms of
// the WPILib BSD license file in the root directory of this project.

package frc.robot.subsystems;

import edu.wpi.first.math.MathUtil;
import edu.wpi.first.math.controller.ProfiledPIDController;
import edu.wpi.first.math.trajectory.TrapezoidProfile;
import edu.wpi.first.math.trajectory.TrapezoidProfile.State;
import edu.wpi.first.util.sendable.SendableBuilder;
import edu.wpi.first.wpilibj.DutyCycleEncoder;
import edu.wpi.first.wpilibj.RobotController;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;
import edu.wpi.first.wpilibj2.command.ProfiledPIDSubsystem;
//import frc.math.Dither;
import frc.motorcontrol.Parallax360;

public class Turner extends ProfiledPIDSubsystem {
  private static final double kP = 2;
  private static final double kD = 0.2;
  private static final double kMaxVelocity = 2.1;
  private static final int kMaxAcceleration = 8;
  private static final double kV = 0.4;
  private static final double kGearRatio = 4;
  // avoids moving from 0 to 0.5 on startup
  private static final double kInitialPosition = 0.5;

  public final Parallax360 m_motor;
  public final DutyCycleEncoder m_input;

  // for logging
  private double m_feedForwardOutput;
  private double m_controllerOutput;
  private double m_position;
  private double m_velocity;
  private double m_acceleration;
  private double m_setpointAccel;
  private double m_prevSetpointVelocity;
  private double m_userInput; // [-1,1]
  private final double m_offset;
  // private Dither m_dither;

  public Turner(int channel, double offset) {
    super(
        new ProfiledPIDController(kP * kGearRatio, 0, kD * kGearRatio,
            new TrapezoidProfile.Constraints(kMaxVelocity / kGearRatio, kMaxAcceleration / kGearRatio)),
        0);
    getController().enableContinuousInput(0, 1);
    getController().setTolerance(0.005, 0.005); // 1.8 degrees
    setName(String.format("Turning %d", channel));
    m_motor = new Parallax360(String.format("Turn Motor %d", channel), channel);
    m_input = new DutyCycleEncoder(channel);
    m_input.setDutyCycleRange(0.027, 0.971);
    m_input.setDistancePerRotation(1 / kGearRatio);
    m_offset = offset;
    m_input.setPositionOffset(offset);
    // m_dither = new Dither(-0.05, 0.05);
    SmartDashboard.putData(getName(), this);
  }

  // TODO: measure the effect of 6V voltage on accel and velocity.
  public double getVoltage6V() {
    return RobotController.getVoltage6V();
  }

  public double getCurrent6V() {
    return RobotController.getCurrent6V();
  }

  // control input [-1,1]
  public void setTurnRate(double input) {
    m_userInput = input;
    setGoal(MathUtil.inputModulus(m_position + input, 0, 1));
  }

  public double getMotorOutput() {
    return m_motor.get();
  }

  public void setMotorOutput(double value) {
    m_motor.set(value);
  }

  public double getGoalPosition() {
    return getController().getGoal().position;
  }

  public double getGoalVelocity() {
    return getController().getGoal().velocity;
  }

  @Override
  protected void useOutput(double output, State setpoint) {
    m_controllerOutput = output;
    m_setpointAccel = (setpoint.velocity - m_prevSetpointVelocity) / 0.02;
    m_prevSetpointVelocity = setpoint.velocity;
    // the motor is itself velocity controlled with more-or-less max acceleration
    // between setpoints, so simple velocity control is good enough.
    m_feedForwardOutput = kV * kGearRatio * setpoint.velocity;
    // dithering overcomes friction for very low outputs.
    // setMotorOutput(m_dither.calculate(m_controllerOutput + m_feedForwardOutput));
    setMotorOutput(m_controllerOutput + m_feedForwardOutput);
  }

  public double getFeedForwardOutput() {
    return m_feedForwardOutput;
  }

  public double getControllerOutput() {
    return m_controllerOutput;
  }

  // returns [0,1], inverting absolute position
  // also update positions etc
  @Override
  public double getMeasurement() {
    double distanceWrapped = (m_input.getDistance() - kInitialPosition) % 1; // might be negative
    distanceWrapped = Math.signum(distanceWrapped) >= 0 ? distanceWrapped : distanceWrapped + 1;
    double newPosition = 1 - distanceWrapped;
    double newVelocity = (newPosition - m_position) / 0.02;
    double newAcceleration = (newVelocity - m_velocity) / 0.02;
    m_position = newPosition;
    m_velocity = newVelocity;
    m_acceleration = newAcceleration;
    return m_position;
  }

  // what the PID thinks the difference is between the measurement and the
  // setpoint
  public double getGetPositionError() {
    return getController().getPositionError();
  }

  public double getGetVelocityError() {
    return getController().getVelocityError();
  }

  // what the trapezoid is telling the PID
  public double getSetpointPosition() {
    return getController().getSetpoint().position;
  }

  public double getSetpointVelocity() {
    return getController().getSetpoint().velocity;
  }

  public double getSetpointAccel() {
    return m_setpointAccel;
  }

  public double getPosition() {
    return m_position;
  }

  public double getVelocity() {
    return m_velocity;
  }

  public double getAcceleration() {
    return m_acceleration;
  }

  @Override
  public void initSendable(SendableBuilder builder) {
    super.initSendable(builder);
    builder.addDoubleProperty("voltage 6v", this::getVoltage6V, null);
    builder.addDoubleProperty("current 6v", this::getCurrent6V, null);
    builder.addDoubleProperty("controller output", this::getControllerOutput, null);
    builder.addDoubleProperty("feed forward output", this::getFeedForwardOutput, null);
    builder.addDoubleProperty("motor output", this::getMotorOutput, null);
    builder.addDoubleProperty("goal position", this::getGoalPosition, null);
    builder.addDoubleProperty("goal velocity", this::getGoalVelocity, null);
    builder.addDoubleProperty("setpoint position", this::getSetpointPosition, null);
    builder.addDoubleProperty("setpoint velocity", this::getSetpointVelocity, null);
    builder.addDoubleProperty("setpoint accel", this::getSetpointAccel, null);
    builder.addDoubleProperty("position error", this::getGetPositionError, null);
    builder.addDoubleProperty("velocity error", this::getGetVelocityError, null);
    builder.addDoubleProperty("position", this::getPosition, null);
    builder.addDoubleProperty("velocity", this::getVelocity, null);
    builder.addDoubleProperty("acceleration", this::getAcceleration, null);
    builder.addDoubleProperty("user input", () -> m_userInput, null);
  }

  public void initialize() {
    m_input.reset(); // erase the counter to remove quarter-turn error
    m_input.setPositionOffset(m_offset);
    enable();
  }
}
