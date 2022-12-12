package org.truher.radar;

import edu.wpi.first.util.CombinedRuntimeLoader;

import edu.wpi.first.networktables.NetworkTableInstance;
import edu.wpi.first.networktables.NetworkTablesJNI;
import java.io.IOException;

public class NetworkTablesPlugin {

  private final NetworkTableInstance inst;

  private static NetworkTableInstance getDefaultInstance() throws IOException {
    NetworkTablesJNI.Helper.setExtractOnStaticLoad(false);

    var files = CombinedRuntimeLoader.extractLibraries(NetworkTablesPlugin.class,
        "/ResourceInformation-NetworkTables.json"); // matches build.gradle
    CombinedRuntimeLoader.loadLibrary("ntcorejni", files);

    return NetworkTableInstance.getDefault();
  }

  public NetworkTablesPlugin() throws IOException {
    this.inst = getDefaultInstance();
  }

  public void onLoad() {
    inst.stopDSClient();
    inst.stopClient();
    inst.stopServer();
    inst.setServer("localhost", NetworkTableInstance.kDefaultPort4);
    inst.startClient4("radar");
    inst.startDSClient();
  }

  public void onUnload() {
    inst.stopDSClient();
    inst.stopClient();
    inst.stopServer();
  }
}
