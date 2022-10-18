package frc.robot;

import edu.wpi.first.wpilibj.DriverStation;
import edu.wpi.first.wpilibj.Joystick;
import edu.wpi.first.wpilibj2.command.Command;
import edu.wpi.first.wpilibj2.command.NotifierCommand;
import edu.wpi.first.wpilibj2.command.button.JoystickButton;
import frc.robot.commands.JoystickDumper;

public class RobotContainer {
    private final Joystick m_joystick = new Joystick(0);
    private final Command m_dumper = new JoystickDumper(m_joystick);
    boolean outputState = false;
    // private final NotifierCommand m_recurring = new NotifierCommand(
    // () -> DriverStation.reportError("hello", false),
    // 1);
    private final NotifierCommand m_recurring = new NotifierCommand(
            () -> setoutput(), 0.1);

    public RobotContainer() {
        System.out.println("joystick name: " + m_joystick.getName());
        System.out.println("axis count: " + m_joystick.getAxisCount());
        System.out.println("button count: " + m_joystick.getButtonCount());

        configureButtonBindings();
    }

    private void setoutput() {
        if (outputState) {
            DriverStation.reportWarning("output high", false);
          //  m_joystick.setOutputs(1023);
            //m_joystick.setOutput(1, true);
            //for (int i = 1; i <= 32; ++i) {
            //    m_joystick.setOutput(i, true);  
            //}
            m_joystick.setOutput(1, true);
            m_joystick.setOutput(2, true);
            m_joystick.setOutput(3, true);
            m_joystick.setOutput(5, true);
            m_joystick.setOutput(7, true);
            m_joystick.setOutput(11, true);
            m_joystick.setOutput(13, true);
            // 16 max
        } else {
            DriverStation.reportWarning("output low", false);
           // m_joystick.setOutputs(0);
            //m_joystick.setOutput(1, false);
            for (int i = 1; i <= 32; ++i) {
                m_joystick.setOutput(i, false);  
            }
        }
        outputState = !outputState;
    }

    private void configureButtonBindings() {
        // new JoystickButton(m_joystick, 2).whileHeld(m_dumper);
        new JoystickButton(m_joystick, 2).whileHeld(m_recurring);
    }

}
