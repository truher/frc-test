package frc.robot.consoles;

import edu.wpi.first.wpilibj2.command.InstantCommand;
import edu.wpi.first.wpilibj2.command.NotifierCommand;
import edu.wpi.first.wpilibj2.command.RunCommand;
import edu.wpi.first.wpilibj2.command.Subsystem;
import edu.wpi.first.wpilibj2.command.button.Trigger;

public class ArmConsole extends BaseConsole {

    /**
     * Just to show how to wire things up.
     */
    public class FakeTwoJointArm implements Subsystem {
        /**
         * Operates the arm motors completely manually; this is an override but
         * also the default command.
         */
        public void manual(double boom, double stick) {
        }

        /**
         * Uses trapezoid trajectories and inverse kinematics to put the effector
         * at the specified location.
         */
        public void setGoal(double height, double distance) {
        }

        /**
         * True if the current location is the goal location. This would be an
         * example of an observable.
         */
        public boolean atGoal() {
            return false;
        }

        /**
         * Sets the speed for future trajectories.
         */
        public void setSpeed(double speed) {
        }

        /**
         * Override everything and stop.
         */
        public void stop() {
        }
    }

    FakeTwoJointArm m_fakeArm = new FakeTwoJointArm();

    public static class Config {
        double slowSpeed = 0.25;
        double medSpeed = 0.5;
        double fastSpeed = 1.0;
        double highGoalHeight = 2.0;
        double highGoalDistance = 0.25;
        double lowGoalHeight = 0.5;
        double lowGoalDistance = 0.25;
        double farGoalHeight = 0.5;
        double farGoalDistance = 1.0;
        double notifierRate = 0.1;
    }

    public ArmConsole(Config config) {
        super(portFromName("Arm"));

        // manual control knobs
        m_fakeArm.setDefaultCommand(
                new RunCommand(
                        () -> m_fakeArm.manual(boomKnob(), stickKnob()), m_fakeArm));

        // speed buttons
        new Trigger(() -> stopButton()).whileActiveContinuous(
                new InstantCommand(
                        () -> m_fakeArm.stop(), m_fakeArm));
        new Trigger(() -> slowButton()).whenActive(
                new InstantCommand(
                        () -> m_fakeArm.setSpeed(config.slowSpeed), m_fakeArm));
        new Trigger(() -> medButton()).whenActive(
                new InstantCommand(
                        () -> m_fakeArm.setSpeed(config.medSpeed), m_fakeArm));
        new Trigger(() -> fastButton()).whenActive(
                new InstantCommand(
                        () -> m_fakeArm.setSpeed(config.fastSpeed), m_fakeArm));

        // goal setting buttons
        new Trigger(() -> highGoalButton()).whileActiveOnce(
                new InstantCommand(
                        () -> m_fakeArm.setGoal(
                                config.highGoalHeight, config.highGoalDistance),
                        m_fakeArm));
        new Trigger(() -> lowGoalButton()).whileActiveOnce(
                new InstantCommand(
                        () -> m_fakeArm.setGoal(
                                config.lowGoalHeight, config.lowGoalDistance),
                        m_fakeArm));
        new Trigger(() -> farGoalButton()).whileActiveOnce(
                new InstantCommand(
                        () -> m_fakeArm.setGoal(
                                config.farGoalHeight, config.farGoalDistance),
                        m_fakeArm));

        // goal observer
        new NotifierCommand(
                () -> setoutput1(), config.notifierRate).schedule();
    }

    private boolean highGoalButton() {
        return getRawButton(4);
    }

    private boolean lowGoalButton() {
        return getRawButton(5);
    }

    private boolean farGoalButton() {
        return getRawButton(6);
    }

    // speed control buttons

    private boolean stopButton() {
        return getRawButton(0);
    }

    private boolean slowButton() {
        return getRawButton(1);
    }

    private boolean medButton() {
        return getRawButton(2);
    }

    private boolean fastButton() {
        return getRawButton(3);
    }

    // manual control knobs

    private double boomKnob() {
        return getRawAxis(0);
    }

    private double stickKnob() {
        return getRawAxis(1);
    }

    // output channels
    
    private int atGoalLight() {
        return 0;
    }

    /*
     * Encodes some state in some outputs.
     */
    private void setoutput1() {
        if (m_fakeArm.atGoal()) {
            setOutput(atGoalLight());
        } else {
            clearOutput(atGoalLight());
        }
        sendOutputs();
    }

}
