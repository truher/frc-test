package org.truher.radar;

import edu.wpi.first.networktables.BooleanPublisher;
import edu.wpi.first.networktables.BooleanTopic;
import edu.wpi.first.networktables.NetworkTable;
import edu.wpi.first.networktables.NetworkTableInstance;
import edu.wpi.first.util.CombinedRuntimeLoader;
import edu.wpi.first.util.WPIUtilJNI;

import java.io.IOException;

public final class Main {

  public static void main(String[] args) throws IOException, InterruptedException {

    WPIUtilJNI.Helper.setExtractOnStaticLoad(false);
    CombinedRuntimeLoader.loadLibraries(Main.class, "wpiutiljni");

    System.out.println("START ##############################################################################");

    NetworkTablesPlugin ntp = new NetworkTablesPlugin();
    ntp.onLoad();
    NetworkTable t = NetworkTableInstance.getDefault().getTable("");
    BooleanTopic b = t.getBooleanTopic("blarg2");
    BooleanPublisher p = b.publish();
    p.set(true);

    TargetSubscriber r = new TargetSubscriber();
    r.run();
    new TargetPublisher().run();

    System.out.println("END ##############################################################################");

    Thread.sleep(30000);
  }
}
