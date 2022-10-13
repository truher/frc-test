package frc.robot;

import edu.wpi.first.wpilibj.Joystick;
import edu.wpi.first.wpilibj2.command.Command;
import edu.wpi.first.wpilibj2.command.button.JoystickButton;
import frc.robot.commands.JoystickDumper;

public class RobotContainer {
    private final Joystick m_joystick = new Joystick(0);
    private final Command m_dumper = new JoystickDumper(m_joystick);

    public RobotContainer() {
        System.out.println("joystick name: " + m_joystick.getName());
        System.out.println("axis count: " + m_joystick.getAxisCount());
        System.out.println("button count: " + m_joystick.getButtonCount());

        configureButtonBindings();
    }

    private void configureButtonBindings() {
        new JoystickButton(m_joystick, 1).whileHeld(m_dumper);
    }

}
