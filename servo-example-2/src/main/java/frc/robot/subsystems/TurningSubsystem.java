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

public class TurningSubsystem extends ProfiledPIDSubsystem {
  public final Parallax360 m_motor;
  public final DutyCycleEncoder m_input;
  public final SimpleMotorFeedforward m_feedForward;
  // for logging
  private double m_feedForwardOutput;
  private double m_controllerOutput;
  // for feedforward
  private double m_accel;
  private double m_prevSetpointVelocity;
  private long m_prevTimeUs;
  private long m_dtUs;

  public TurningSubsystem(int channel) {
    super(
        new ProfiledPIDController(3, 0, 0,
            new TrapezoidProfile.Constraints(1.3, 5)), // observed max v is 1.7 t/s, max a is 7.5 t/s/s
        0);
    getController().enableContinuousInput(0, 1);
    getController().setTolerance(0.01, 0.01); // 3.6 degrees
    setName(String.format("Turning %d", channel));
    m_motor = new Parallax360(String.format("Turn Motor %d", channel), channel);
    m_input = new DutyCycleEncoder(channel);
    m_input.setDutyCycleRange(0.027, 0.971);
//    m_feedForward = new SimpleMotorFeedforward(0.08, 0.5, 0.15);  // observed is KS=0.1, KV=0.588, KA=0.133.
    m_feedForward = new SimpleMotorFeedforward(0.1, 0.1, 0.25);  // observed is KS=0.1, KV=0.588, KA=0.133.
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
    m_accel = (setpoint.velocity - m_prevSetpointVelocity) / 0.02; // dtS;
    m_controllerOutput = output;
    //m_feedForwardOutput = m_feedForward.calculate(setpoint.velocity, m_accel);
    m_feedForwardOutput = m_feedForward.calculate(m_prevSetpointVelocity, setpoint.velocity, 0.02); // dtS);
    setMotorOutput(m_controllerOutput + m_feedForwardOutput);
    //m_motor.set(m_controllerOutput);
    // m_motor.set(0);
    m_prevSetpointVelocity = setpoint.velocity;
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

  // return [0,1]
  @Override
  public double getMeasurement() {
    // position is measured in turns [0,1].
    // this needs to also return [0,1] but inverted, so it's not -1*position
    // it's 1-position.
    return 1 - m_input.getAbsolutePosition();
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
    return m_accel;
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
  }
}
