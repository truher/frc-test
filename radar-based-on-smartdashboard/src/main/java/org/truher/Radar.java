package org.truher;

import edu.wpi.first.networktables.NetworkTablesJNI;
import edu.wpi.first.wpiutil.CombinedRuntimeLoader;
import edu.wpi.first.wpiutil.WPIUtilJNI;
import java.io.IOException;

public class Radar {

  public static void main(final String[] args) throws IOException {
    System.out.println("main");
    NewThing n = new NewThing();
    n.run();
    WPIUtilJNI.Helper.setExtractOnStaticLoad(false);
    NetworkTablesJNI.Helper.setExtractOnStaticLoad(false);
    CombinedRuntimeLoader.loadLibraries(Radar.class, "wpiutiljni", "ntcorejni");
    NetworkTablesJNI.getDefaultInstance();

  }
}
