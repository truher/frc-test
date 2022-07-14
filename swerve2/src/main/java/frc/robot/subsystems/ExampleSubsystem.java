// Copyright (c) FIRST and other WPILib contributors.
// Open Source Software; you can modify and/or share it under the terms of
// the WPILib BSD license file in the root directory of this project.

package frc.robot.subsystems;

import com.kauailabs.navx.frc.AHRS;

import edu.wpi.first.util.sendable.SendableBuilder;
import edu.wpi.first.wpilibj.AnalogEncoder;
import edu.wpi.first.wpilibj.DriverStation;
import edu.wpi.first.wpilibj.SerialPort.Port;
import edu.wpi.first.wpilibj.motorcontrol.PWMMotorController;
import edu.wpi.first.wpilibj.motorcontrol.Talon;
import edu.wpi.first.wpilibj.motorcontrol.VictorSP;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;
import edu.wpi.first.wpilibj2.command.SubsystemBase;

public class ExampleSubsystem extends SubsystemBase {
    public final AnalogEncoder m_steeringEncoder0;
    public final AnalogEncoder m_steeringEncoder1;
    public final AnalogEncoder m_steeringEncoder2;
    public final AnalogEncoder m_steeringEncoder3;
    public AHRS ahrs;
    public final PWMMotorController m_steeringMotor0;
    public final PWMMotorController m_steeringMotor1;
    public final PWMMotorController m_steeringMotor2;
    public final PWMMotorController m_steeringMotor3;
    public final Talon m_driveMotor11;
    public final Talon m_driveMotor12;
    public final Talon m_driveMotor21;
    public final Talon m_driveMotor22;



    /** Creates a new ExampleSubsystem. */
    public ExampleSubsystem() {
        m_steeringEncoder0 = new AnalogEncoder(0);
        m_steeringEncoder1 = new AnalogEncoder(1);
        m_steeringEncoder2 = new AnalogEncoder(2);
        m_steeringEncoder3 = new AnalogEncoder(3);
        m_steeringMotor0 = new VictorSP(0);
        m_steeringMotor1 = new VictorSP(1);
        m_steeringMotor2 = new VictorSP(2);
        m_steeringMotor3 = new VictorSP(3);
        m_driveMotor11 = new Talon(11);
        m_driveMotor12 = new Talon(12);
        m_driveMotor21 = new Talon(21);
        m_driveMotor22 = new Talon(22);

        try {
            ahrs = new AHRS(Port.kUSB);
        } catch (RuntimeException ex) {
            DriverStation.reportError("Error instantiating navX MXP:  " + ex.getMessage(), true);
        }

        SmartDashboard.putData("example subsystem", this);
    }

    public void doit(double blarg) {
        // nothing
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
        builder.setSmartDashboardType("blarg");
        builder.addDoubleProperty("ch0", () -> m_steeringEncoder0.getAbsolutePosition(), null);
        builder.addDoubleProperty("ch1", () -> m_steeringEncoder1.getAbsolutePosition(), null);
        builder.addDoubleProperty("ch2", () -> m_steeringEncoder2.getAbsolutePosition(), null);
        builder.addDoubleProperty("ch3", () -> m_steeringEncoder3.getAbsolutePosition(), null);
        builder.addDoubleProperty("ch0 offset", () -> m_steeringEncoder0.getPositionOffset(), null);
        builder.addDoubleProperty("ch1 offset", () -> m_steeringEncoder1.getPositionOffset(), null);
        builder.addDoubleProperty("ch2 offset", () -> m_steeringEncoder2.getPositionOffset(), null);
        builder.addDoubleProperty("ch3 offset", () -> m_steeringEncoder3.getPositionOffset(), null);
        builder.addDoubleProperty("gyro", () -> ahrs.getAngle(), null);

    }
}
