package org.truher.radar;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import edu.wpi.first.math.WPIMathJNI;
import edu.wpi.first.net.WPINetJNI;
import edu.wpi.first.networktables.NetworkTableInstance;
import edu.wpi.first.networktables.NetworkTablesJNI;
import edu.wpi.first.util.CombinedRuntimeLoader;
import edu.wpi.first.util.WPIUtilJNI;

public final class Main {
  public static void main(String[] args) throws IOException, InterruptedException {
    System.out.println("""
        Radar: NT4 example dashboard app.

        Usage: java -jar Radar-winx64.jar          NT server, publish fake data to 'targets' and 'map'
           or: java -jar Radar-winx64.jar [topic]  NT client, display targets from specified topic
        """);

    String topicName = null;
    if (args.length > 0) {
      topicName = args[0]; // currently "targets" or "map"
    }
    if (args.length > 1) {
      System.out.printf("ignoring extra arguments: %s\n",
          String.join(" ", Arrays.copyOfRange(args, 1, args.length)));
    }

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

    // NetworkTableInstance inst = NetworkTableInstance.getDefault();
    // inst.setServer("localhost", NetworkTableInstance.kDefaultPort4);
    // inst.startClient4("radar");
    // inst.startDSClient();

    if (topicName == null) {
      System.out.println("running publisher");
      new TargetPublisher().run();
    } else {
      System.out.printf("subscribing to topic %s\n", topicName);
      new TargetSubscriber(topicName).run();
    }

    while (true) {
      Thread.sleep(1000);
    }
  }
}
