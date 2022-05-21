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
import edu.wpi.first.wpilibj.RobotController;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;
import edu.wpi.first.wpilibj2.command.ProfiledPIDSubsystem;
import frc.math.Dither;

public class TurningSubsystem extends ProfiledPIDSubsystem {
  public final Parallax360 m_motor;
  public final DutyCycleEncoder m_input;
  public final SimpleMotorFeedforward m_feedForward;
  // for logging
  private double m_feedForwardOutput;
  private double m_controllerOutput;
  private double m_position;
  private double m_velocity;
  private double m_acceleration;
  // for feedforward
  private double m_setpointAccel;
  private double m_prevSetpointVelocity;
  private long m_prevTimeUs;
  private long m_dtUs;
  private Dither m_dither;

  public TurningSubsystem(int channel) {
    super(
        new ProfiledPIDController(1, 0, 0,
            new TrapezoidProfile.Constraints(1.3, 5)), // observed max v is 1.7 t/s, max a is 7.5 t/s/s
        0);
    getController().enableContinuousInput(0, 1);
    getController().setTolerance(0.01, 0.01); // 3.6 degrees
    setName(String.format("Turning %d", channel));
    m_motor = new Parallax360(String.format("Turn Motor %d", channel), channel);
    m_input = new DutyCycleEncoder(channel);
    m_input.setDutyCycleRange(0.027, 0.971);
    // observed is KS=0.1, KV=0.588, KA=0.133
    // TODO: why do KV and KA seem wrong?  use LQR instead.
    // m_feedForward = new SimpleMotorFeedforward(0.08, 0.5, 0.15);
    // KS is a lie, don't tell feedforward about it.
    m_feedForward = new SimpleMotorFeedforward(0, 0.5, 0.1);
    m_dither = new Dither(-0.15, 0.15);
    SmartDashboard.putData(getName(), this);
  }

  // TODO: use this in motor output calcs.
  public double getVoltage6V() {
    return RobotController.getVoltage6V();
  }

  public double getCurrent6V() {
    return RobotController.getCurrent6V();
  }

  public double getMotorOutput() {
    return m_motor.get();
  }

  public void setMotorOutput(double value) {
    m_motor.set(value);
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
    long tUs = RobotController.getFPGATime();
    m_dtUs = tUs - m_prevTimeUs;
    double dtS = 1e-6 * m_dtUs;
    m_prevTimeUs = tUs;
    m_setpointAccel = (setpoint.velocity - m_prevSetpointVelocity) / 0.02; // dtS;
    m_controllerOutput = output;
    // m_feedForwardOutput = m_feedForward.calculate(setpoint.velocity, m_accel);
    m_feedForwardOutput = m_feedForward.calculate(m_prevSetpointVelocity, setpoint.velocity, 0.02); // dtS);
    m_prevSetpointVelocity = setpoint.velocity;

    double desiredMotorOutput = m_controllerOutput + m_feedForwardOutput;
    double ditheredOutput = m_dither.calculate(desiredMotorOutput);

    setMotorOutput(ditheredOutput);
    // m_motor.set(m_controllerOutput);
    // m_motor.set(0);
  }

  public double getDtUs() {
    return (double) m_dtUs;
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
    double newPosition = 1 - m_input.getAbsolutePosition();
    double newVelocity = (newPosition - m_position) / 0.02; // todo: adjustable period?
    double newAcceleration = (newVelocity - m_velocity) / 0.02; // todo: adjustable period?
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
    builder.addDoubleProperty("measurement", this::getMeasurement, null);
    builder.addDoubleProperty("goal position", this::getGoalPosition, null);
    builder.addDoubleProperty("goal velocity", this::getGoalVelocity, null);
    builder.addDoubleProperty("setpoint position", this::getSetpointPosition, null);
    builder.addDoubleProperty("setpoint velocity", this::getSetpointVelocity, null);
    builder.addDoubleProperty("setpoint accel", this::getSetpointAccel, null);
    builder.addDoubleProperty("position error", this::getGetPositionError, null);
    builder.addDoubleProperty("velocity error", this::getGetVelocityError, null);
    builder.addBooleanProperty("connected", this::isConnected, null);
    builder.addDoubleProperty("dt us", this::getDtUs, null);
    builder.addDoubleProperty("position", this::getPosition, null);
    builder.addDoubleProperty("velocity", this::getVelocity, null);
    builder.addDoubleProperty("acceleration", this::getAcceleration, null);
  }
}
