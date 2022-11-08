package frc.robot.consoles;

import edu.wpi.first.wpilibj.DriverStation;
import edu.wpi.first.wpilibj2.command.NotifierCommand;
import edu.wpi.first.wpilibj2.command.button.Trigger;

public class PilotConsole extends BaseConsole {
    boolean outputState1 = false;
    boolean outputState2 = false;

    public PilotConsole() {
        super(portFromName("Pilot"));
        new Trigger(() -> getRawButton(0))
                .whileActiveOnce(new NotifierCommand(() -> setoutput1(), 0.1));
        new Trigger(() -> getRawButton(1))
                .whileActiveOnce(new NotifierCommand(() -> setoutput2(), 0.1 * Math.sqrt(2)));
    }

    /*
     * Encodes the state in some of the outputs
     */
    private void setoutput1() {
        if (outputState1) {
            DriverStation.reportWarning("output1 high", false);
            setOutput(0, true);
            setOutput(1, true);
        } else {
            DriverStation.reportWarning("output1 low", false);
            setOutput(0, false);
            setOutput(1, false);
        }
        sendOutputs();
        outputState1 = !outputState1;
    }

    private void setoutput2() {
        if (outputState2) {
            DriverStation.reportWarning("output2 high", false);
            setOutput(2, true);
        } else {
            DriverStation.reportWarning("output2 low", false);
            setOutput(2, false);
        }
        sendOutputs();
        outputState2 = !outputState2;
    }
}
