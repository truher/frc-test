package org.truher.radar;

import edu.wpi.first.networktables.BooleanPublisher;
import edu.wpi.first.networktables.BooleanTopic;
import edu.wpi.first.networktables.NetworkTable;
import edu.wpi.first.networktables.NetworkTableInstance;
import edu.wpi.first.networktables.NetworkTablesJNI;
import edu.wpi.first.util.CombinedRuntimeLoader;
import edu.wpi.first.util.WPIUtilJNI;

import java.io.IOException;

public final class Main {

  public static void main(String[] args) throws IOException, InterruptedException {

    WPIUtilJNI.Helper.setExtractOnStaticLoad(false);
    CombinedRuntimeLoader.loadLibraries(Main.class, "wpiutiljni");

    NetworkTablesJNI.Helper.setExtractOnStaticLoad(false);
    // json filename matches build.gradle
    var files = CombinedRuntimeLoader.extractLibraries(Main.class, "/ResourceInformation-NetworkTables.json"); 
    CombinedRuntimeLoader.loadLibrary("ntcorejni", files);

    NetworkTableInstance inst =  NetworkTableInstance.getDefault(); 
    inst.setServer("localhost", NetworkTableInstance.kDefaultPort4);
    inst.startClient4("radar");
    inst.startDSClient();

    TargetSubscriber r = new TargetSubscriber();
    r.run();
    new TargetPublisher().run();

    Thread.sleep(30000);
  }
}
