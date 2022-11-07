package frc.robot.consoles;

import edu.wpi.first.hal.HAL;
import edu.wpi.first.wpilibj.DriverStation;

public class BaseConsole {
    private final int m_port;
    private int m_outputs; // access only with synchronize

    protected BaseConsole(int port) {
        m_port = port;
    }

    /*
     * Look up the port number using the Windows name -- each subconsole will
     * have a different name.
     */
    public static int portFromName(String name) {
        for (int i = 0; i < DriverStation.kJoystickPorts; ++i) {
            if (DriverStation.getJoystickName(i) == name) {
                return i;
            }
        }
        return -1;
    }

    /**
     * axis: 0-7
     */
    protected double getRawAxis(int axis) {
        if (m_port < 0)
            return 0;
        return DriverStation.getStickAxis(m_port, axis);
    }

    /**
     * button: 0-31
     */
    protected boolean getRawButton(int button) {
        if (m_port < 0)
            return false;
        return DriverStation.getStickButton(m_port, (byte) (button + 1));
    }

    protected void sendOutputs() {
        if (m_port < 0)
            return;
        synchronized (this) {
            HAL.setJoystickOutputs((byte) m_port, m_outputs, (short) 0, (short) 0);
        }
    }

    /**
     * bit: 0-15
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
