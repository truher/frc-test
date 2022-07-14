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
import edu.wpi.first.wpilibj.motorcontrol.VictorSP;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;
import edu.wpi.first.wpilibj2.command.SubsystemBase;

public class ExampleSubsystem extends SubsystemBase {
    public final AnalogEncoder a0;
    public final AnalogEncoder ch1;
    public final AnalogEncoder ch2;
    public final AnalogEncoder ch3;
    public AHRS ahrs;
    public final PWMMotorController m0;
    public final PWMMotorController m1;
    public final PWMMotorController m2;
    public final PWMMotorController m3;

    /** Creates a new ExampleSubsystem. */
    public ExampleSubsystem() {
        a0 = new AnalogEncoder(0);
        ch1 = new AnalogEncoder(1);
        ch2 = new AnalogEncoder(2);
        ch3 = new AnalogEncoder(3);
        m0 = new VictorSP(0);
        m1 = new VictorSP(1);
        m2 = new VictorSP(2);
        m3 = new VictorSP(3);

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
        builder.addDoubleProperty("ch0", () -> a0.getAbsolutePosition(), null);
        builder.addDoubleProperty("ch1", () -> ch1.getAbsolutePosition(), null);
        builder.addDoubleProperty("ch2", () -> ch2.getAbsolutePosition(), null);
        builder.addDoubleProperty("ch3", () -> ch3.getAbsolutePosition(), null);
        builder.addDoubleProperty("ch0 offset", () -> a0.getPositionOffset(), null);
        builder.addDoubleProperty("ch1 offset", () -> ch1.getPositionOffset(), null);
        builder.addDoubleProperty("ch2 offset", () -> ch2.getPositionOffset(), null);
        builder.addDoubleProperty("ch3 offset", () -> ch3.getPositionOffset(), null);
        builder.addDoubleProperty("gyro", () -> ahrs.getAngle(), null);

    }
}
