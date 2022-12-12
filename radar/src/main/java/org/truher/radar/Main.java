package org.truher.radar;

import java.io.IOException;

import edu.wpi.first.math.WPIMathJNI;
import edu.wpi.first.net.WPINetJNI;
import edu.wpi.first.networktables.NetworkTableInstance;
import edu.wpi.first.networktables.NetworkTablesJNI;
import edu.wpi.first.util.CombinedRuntimeLoader;
import edu.wpi.first.util.WPIUtilJNI;

public final class Main {

  public static void main(String[] args) throws IOException, InterruptedException {
    // Turns off the native loaders in static initializers, which only work
    // if the cache is already populated.
    NetworkTablesJNI.Helper.setExtractOnStaticLoad(false);
    WPIMathJNI.Helper.setExtractOnStaticLoad(false);
    WPINetJNI.Helper.setExtractOnStaticLoad(false);
    WPIUtilJNI.Helper.setExtractOnStaticLoad(false);

    // Extracts specified dlls from the jar if they're listed in
    // ResourceInformation.json, and loads them. Note: ntcore depends on wpinet and
    // wpiutil; loadLibraries sets the DLL directory so that windows can find them
    // if they don't happen to be listed in dependency order.
    CombinedRuntimeLoader.loadLibraries(Main.class, "wpinetjni", "ntcorejni", "wpiutiljni", "wpimathjni");

    NetworkTableInstance inst = NetworkTableInstance.getDefault();
    inst.setServer("localhost", NetworkTableInstance.kDefaultPort4);
    inst.startClient4("radar");
    inst.startDSClient();

    TargetSubscriber r = new TargetSubscriber();
    r.run();
    new TargetPublisher().run();

    Thread.sleep(30000);
  }
}
