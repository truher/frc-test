package frc.robot;

import edu.wpi.first.wpilibj.DriverStation;
import edu.wpi.first.wpilibj.Joystick;
import edu.wpi.first.wpilibj.TimedRobot;
import edu.wpi.first.wpilibj2.command.SubsystemBase;
import edu.wpi.first.wpilibj2.command.button.JoystickButton;

public final class Robot extends TimedRobot {

  // private final GenericHID m_joystick = new GenericHID(0);

  private final Joystick m_joystick = new Joystick(0);
  // button index is one-based
  private final JoystickButton m_button0 = new JoystickButton(m_joystick, 1);

  public Robot() {
    // simgui can't see the buttons.
    m_button0.whenPressed(() -> System.out.println("foo"), m_noting);
  }

  @Override
  public void teleopPeriodic() {

    //// System.out.println(m_joystick.getName());
    // System.out.println(DriverStation.getJoystickName(0));
    System.out.println(DriverStation.getStickButtonCount(0)); // zero in sim

  }

}
