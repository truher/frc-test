package frc.robot.subsystems;

import java.util.ArrayList;
import java.util.List;

import edu.wpi.first.util.sendable.SendableBuilder;
import edu.wpi.first.wpilibj2.command.SubsystemBase;

public class SubsystemGroup extends SubsystemBase {
    private final List<TurningSubsystem> m_steers;
    private final List<DriveSubsystem> m_drives;

    public SubsystemGroup() {
        m_steers = new ArrayList<TurningSubsystem>();
        m_drives = new ArrayList<DriveSubsystem>();
        for (int i = 0; i < 6; i = i + 2) {
            TurningSubsystem steer = new TurningSubsystem(i);
            steer.enable();
            m_steers.add(steer);
            DriveSubsystem drive = new DriveSubsystem(i + 1);
            drive.enable();
            m_drives.add(drive);
        }
    }

    public void setTurningGoal(double goal) {
        for (TurningSubsystem subsystem : m_steers) {
            subsystem.setGoal(goal);
        }
    }

    public void setDriveGoal(double goal) {
        for (DriveSubsystem drive : m_drives) {
            drive.setSetpoint(goal * 3);
        }
    }

    public void runTest(double value) {
        m_steers.get(0).periodic();  // for logging
        m_steers.get(0).setMotorOutput(value);
        m_steers.get(1).setMotorOutput(0);
        m_steers.get(2).setMotorOutput(0);
    }

    private double m_testOutput;
    public double getTestOutput() {
        return m_testOutput;
    }
    //private Dither m_dither = new Dither(-0.1, 0.1);

    double m_fullAhead = 1.72;
    double m_highDeadband = 1.52; // 1.5225;
    double m_center = 1.5;
    double m_lowDeadband = 1.48; //1.4775;
    double m_fullAstern = 1.28;

    public double getHighDeadband() {
        return m_highDeadband;
    }
    public void setHighDeadband(double value) {
        m_highDeadband = value;
    }

    public double getLowDeadband() {
        return m_lowDeadband;
    }
    public void setLowDeadband(double value) {
        m_lowDeadband = value;
    }

    public void runTest2(boolean value) {
        m_testOutput += value ? 0.0005 : -0.0005;
        m_steers.get(0).setMotorOutput(m_testOutput);  // is there still a deadband?
        //m_steers.get(0).setMotorOutput(m_dither.calculate(m_testOutput));
        //m_steers.get(0).setMotorOutput(m_dither.calculate(0));  // will it hold a position?
        m_steers.get(1).setMotorOutput(0);
        m_steers.get(2).setMotorOutput(0);
    }

    public void runTest3(boolean value) {
        m_steers.get(0).setGoal(value?0.1:0.5);
        m_steers.get(1).setMotorOutput(0);
        m_steers.get(2).setMotorOutput(0);     
    }

    private boolean direction = false;
    private double step = 0.0005;

    public void runTest4() {
        if (direction) { // forward
            m_testOutput += step;
            if (m_testOutput > 0.2) {
                direction = false;
            }
        } else { // reverse
            m_testOutput -= step;
            if (m_testOutput < -0.2) {
                direction = true;
            }
        }
        m_steers.get(0).m_motor.setBounds(m_fullAhead, m_highDeadband, m_center, m_lowDeadband, m_fullAstern);
        m_steers.get(0).setMotorOutput(m_testOutput);
        m_steers.get(1).setMotorOutput(0);
        m_steers.get(2).setMotorOutput(0);
        m_drives.get(0).setMotorOutput(0);  
        m_drives.get(1).setMotorOutput(0);  
        m_drives.get(2).setMotorOutput(0);  
    }

    @Override
    public void initSendable(SendableBuilder builder) {
      super.initSendable(builder);
      builder.addDoubleProperty("test output", this::getTestOutput, null);
      builder.addDoubleProperty("high deadband", this::getHighDeadband, this::setHighDeadband);
      builder.addDoubleProperty("low deadband", this::getLowDeadband, this::setLowDeadband);
    }
}
