package edu.wpi.first.smartdashboard;

import edu.wpi.first.networktables.NetworkTablesJNI;

import edu.wpi.first.wpiutil.CombinedRuntimeLoader;
import edu.wpi.first.wpiutil.WPIUtilJNI;

import java.io.IOException;

import javax.swing.ProgressMonitor;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

/**
 * SmartDashboard logic
 *
 * @author Joe Grinstead
 * @author pmalmsten
 */
public class SmartDashboard {

  /**
   * Variable used in the {@link SmartDashboard#inCompetition() inCompetition()} method
   */
  private static boolean inCompetition = false;

  /**
   * Returns whether or not this is in "competition" mode. Competition mode
   * should be used on the netbook provided for teams to use the dashboard. If
   * the SmartDashboard is in competition mode, then it automatically sizes
   * itself to be the standard dashboard size and to remove the frame around
   * it. It can be set to be in competition if "competition" is one of the
   * words passed in through the command line.
   *
   * @return whether or not this is in "competition" mode
   */
  public static boolean inCompetition() {
    return inCompetition;
  }

  /**
   * Starts the program
   *
   * @param args the standard arguments. If "competition" is one of them, then the SmartDashboard
   *             will be in competition mode
   * @see SmartDashboard#inCompetition() inCompetition()
   */
  public static void main(final String[] args) throws IOException {
    NewThing n = new NewThing();
    n.run();
    WPIUtilJNI.Helper.setExtractOnStaticLoad(false);
    NetworkTablesJNI.Helper.setExtractOnStaticLoad(false);
    CombinedRuntimeLoader.loadLibraries(SmartDashboard.class, "wpiutiljni", "ntcorejni");


    NetworkTablesJNI.getDefaultInstance();

    try {
      SwingUtilities.invokeAndWait(new Runnable() {
        public void run() {
          try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
          } catch (Exception e) {
    
          }
        }
      });
    } catch (Exception ex) {
      ex.printStackTrace();
      System.exit(2);
    }

    // Present a loading bar (it will only show up if this is going slowly)
    final ProgressMonitor monitor
        = new ProgressMonitor(null, "Loading SmartDashboard", "Initializing internal code...", 0,
        1000);



    // Parse arguments
    ArgParser argParser = new ArgParser(args, true, true, new String[]{"ip"});
    inCompetition = argParser.hasFlag("competition");

    // Initialize GUI
    try {
      SwingUtilities.invokeAndWait(new Runnable() {
        public void run() {
       
        }
      });
    } catch (Exception ex) {
      ex.printStackTrace();
      System.exit(2);
    }

    if (argParser.hasValue("ip")) {
      monitor.setProgress(650);
      monitor.setNote("Connecting to robot at: " + argParser.getValue("ip"));

      System.out.println("IP: " + argParser.getValue("ip"));
    } else {
      monitor.setProgress(600);
      monitor.setNote("Getting Team Number");
     
      

      
      
    }

    try {
      SwingUtilities.invokeAndWait(new Runnable() {

        public void run() {
          
        }
      });
    } catch (Exception ex) {
      ex.printStackTrace();
      System.exit(2);
    }
  }
}
