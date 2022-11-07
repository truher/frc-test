package frc.robot.consoles;

import edu.wpi.first.hal.HAL;
import edu.wpi.first.wpilibj.DriverStation;
import edu.wpi.first.wpilibj2.command.NotifierCommand;
import edu.wpi.first.wpilibj2.command.button.Trigger;

public class ArmConsole {
    private static final String FOO = "foo";
    private int m_port;
    private int m_outputs; // access only with synchronize
    boolean outputState1 = false;

    public ArmConsole() {
        // avoid confusion with "port numbers" by naming each HID differently.
        for (int i = 0; i < DriverStation.kJoystickPorts; ++i) {
            if (DriverStation.getJoystickName(i) == FOO) {
                m_port = i;
                break;
            }
        }

        new Trigger(() -> getRawButton(0)).whileActiveOnce(new NotifierCommand(
                () -> setoutput1(), 0.1));
    }

    // encode the state in some of the outputs
    private void setoutput1() {
        if (outputState1) {
            DriverStation.reportWarning("output1 high", false);
            setOutput(1, true);
            setOutput(2, true);
            sendOutputs();
        } else {
            DriverStation.reportWarning("output1 low", false);
            setOutput(1, false);
            setOutput(2, false);
            sendOutputs();
        }
        outputState1 = !outputState1;
    }

    protected double getRawAxis(int axis) {
        return DriverStation.getStickAxis(m_port, axis);
    }

    protected boolean getRawButton(int button) {
        return DriverStation.getStickButton(m_port, (byte) button);
    }

    protected void sendOutputs() {
        synchronized (this) {
            HAL.setJoystickOutputs((byte) m_port, m_outputs, (short) 0, (short) 0);
        }
    }

    /**
     * bit: 0-31
     */
    protected void setOutput(int bit, boolean value) {
        int bit_mask = 1 << bit; // 1 in the correct spot
        synchronized (this) {
            int masked_output = m_outputs & ~bit_mask; // set the bit to zero
            if (value) {
                m_outputs = masked_output | bit_mask;
            }
        }
    }
}
