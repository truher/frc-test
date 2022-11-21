package frc.robot.subsystems;

import com.ctre.phoenix.motorcontrol.StatorCurrentLimitConfiguration;
import com.ctre.phoenix.motorcontrol.StatusFrameEnhanced;
import com.ctre.phoenix.motorcontrol.SupplyCurrentLimitConfiguration;
import com.ctre.phoenix.motorcontrol.can.WPI_TalonFX;
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
    public final AnalogEncoder m_steeringEncoder0;
    public final AnalogEncoder m_steeringEncoder1;
    public final AnalogEncoder m_steeringEncoder2;
    public final AnalogEncoder m_steeringEncoder3;
    public final PWMMotorController m_steeringMotor0;
    public final PWMMotorController m_steeringMotor1;
    public final PWMMotorController m_steeringMotor2;
    public final PWMMotorController m_steeringMotor3;
    public final WPI_TalonFX m_driveMotor11;
    public final WPI_TalonFX m_driveMotor12;
    public final WPI_TalonFX m_driveMotor21;
    public final WPI_TalonFX m_driveMotor22;
    public AHRS ahrs;

    public ExampleSubsystem() {
        m_steeringEncoder0 = new AnalogEncoder(0);
        m_steeringEncoder1 = new AnalogEncoder(1);
        m_steeringEncoder2 = new AnalogEncoder(2);
        m_steeringEncoder3 = new AnalogEncoder(3);

        m_steeringMotor0 = new VictorSP(0);
        m_steeringMotor1 = new VictorSP(1);
        m_steeringMotor2 = new VictorSP(2);
        m_steeringMotor3 = new VictorSP(3);

        m_driveMotor11 = new WPI_TalonFX(11);
        m_driveMotor12 = new WPI_TalonFX(12);
        m_driveMotor21 = new WPI_TalonFX(21);
        m_driveMotor22 = new WPI_TalonFX(22);

        // by default, feedback comes only every 250ms, which seems too slow.
        m_driveMotor11.setStatusFramePeriod(StatusFrameEnhanced.Status_21_FeedbackIntegrated, 20);
        m_driveMotor12.setStatusFramePeriod(StatusFrameEnhanced.Status_21_FeedbackIntegrated, 20);
        m_driveMotor21.setStatusFramePeriod(StatusFrameEnhanced.Status_21_FeedbackIntegrated, 20);
        m_driveMotor22.setStatusFramePeriod(StatusFrameEnhanced.Status_21_FeedbackIntegrated, 20);

        m_driveMotor11.configStatorCurrentLimit(new StatorCurrentLimitConfiguration(true, 5, 5, 0));
        m_driveMotor12.configStatorCurrentLimit(new StatorCurrentLimitConfiguration(true, 5, 5, 0));
        m_driveMotor21.configStatorCurrentLimit(new StatorCurrentLimitConfiguration(true, 5, 5, 0));
        m_driveMotor22.configStatorCurrentLimit(new StatorCurrentLimitConfiguration(true, 5, 5, 0));

        m_driveMotor11.configSupplyCurrentLimit(new SupplyCurrentLimitConfiguration(true, 5, 5, 0));
        m_driveMotor12.configSupplyCurrentLimit(new SupplyCurrentLimitConfiguration(true, 5, 5, 0));
        m_driveMotor21.configSupplyCurrentLimit(new SupplyCurrentLimitConfiguration(true, 5, 5, 0));
        m_driveMotor22.configSupplyCurrentLimit(new SupplyCurrentLimitConfiguration(true, 5, 5, 0));

        try {
            ahrs = new AHRS(Port.kUSB);
        } catch (RuntimeException ex) {
            DriverStation.reportError("Error instantiating navX MXP:  " + ex.getMessage(), true);
            throw ex;
        }
        SmartDashboard.putData("example subsystem", this);
    }

    @Override
    public void periodic() {
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
        builder.addDoubleProperty("ch0 throttle", () -> m_steeringMotor0.get(), null);
        builder.addDoubleProperty("ch1 throttle", () -> m_steeringMotor1.get(), null);
        builder.addDoubleProperty("ch2 throttle", () -> m_steeringMotor2.get(), null);
        builder.addDoubleProperty("ch3 throttle", () -> m_steeringMotor3.get(), null);
        // velocity is 1/2048ths of a turn per 100ms
        builder.addDoubleProperty("drive11 velocity",
                () -> m_driveMotor11.getSensorCollection().getIntegratedSensorVelocity(), null);
        builder.addDoubleProperty("drive12 velocity",
                () -> m_driveMotor12.getSensorCollection().getIntegratedSensorVelocity(), null);
        builder.addDoubleProperty("drive21 velocity",
                () -> m_driveMotor21.getSensorCollection().getIntegratedSensorVelocity(), null);
        builder.addDoubleProperty("drive22 velocity",
                () -> m_driveMotor22.getSensorCollection().getIntegratedSensorVelocity(), null);
        builder.addDoubleProperty("drive11 throttle", () -> m_driveMotor11.get(), null);
        builder.addDoubleProperty("drive12 throttle", () -> m_driveMotor12.get(), null);
        builder.addDoubleProperty("drive21 throttle", () -> m_driveMotor21.get(), null);
        builder.addDoubleProperty("drive22 throttle", () -> m_driveMotor22.get(), null);

        builder.addDoubleProperty("gyro", () -> ahrs.getAngle(), null);

    }

    public void test(double[][] desiredOutputs) {
        m_driveMotor11.set(desiredOutputs[0][0]);
        m_steeringMotor0.set(desiredOutputs[0][1]);
        m_driveMotor12.set(desiredOutputs[1][0]);
        m_steeringMotor1.set(desiredOutputs[1][1]);
        m_driveMotor21.set(desiredOutputs[2][0]);
        m_steeringMotor2.set(desiredOutputs[2][1]);
        m_driveMotor22.set(desiredOutputs[3][0]);
        m_steeringMotor3.set(desiredOutputs[3][1]);

    }
}
