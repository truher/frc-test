package frc.robot.consoles;

import edu.wpi.first.wpilibj.DriverStation;
import edu.wpi.first.wpilibj2.command.NotifierCommand;
import edu.wpi.first.wpilibj2.command.button.Trigger;

public class AutopilotConsole extends BaseConsole {
    boolean outputState1 = false;

    public AutopilotConsole() {
        super(portFromName("Autopilot"));
        new Trigger(() -> getRawButton(0)).whileActiveOnce(new NotifierCommand(
                () -> setoutput1(), 0.1));
    }

    /*
     * Encodes the state in some of the outputs
     */
    private void setoutput1() {
        if (outputState1) {
            DriverStation.reportWarning("output1 high", false);
            setOutput(1, true);
            setOutput(2, true);
        } else {
            DriverStation.reportWarning("output1 low", false);
            setOutput(1, false);
            setOutput(2, false);
        }
        sendOutputs();
        outputState1 = !outputState1;
    }
}
