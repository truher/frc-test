package edu.wpi.first.shuffleboard.plugin.networktables;

import edu.wpi.first.util.CombinedRuntimeLoader;

import edu.wpi.first.networktables.NetworkTableInstance;
import edu.wpi.first.networktables.NetworkTablesJNI;

import java.io.IOException;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.beans.value.ChangeListener;

public class NetworkTablesPlugin {

  private final NetworkTableInstance inst;

  private final StringProperty serverId = new SimpleStringProperty(this, "server", "localhost");

  private final HostParser hostParser = new HostParser();

  private final ChangeListener<String> serverChangeListener;

  private static NetworkTableInstance getDefaultInstance() throws IOException {
    NetworkTablesJNI.Helper.setExtractOnStaticLoad(false);

    var files = CombinedRuntimeLoader.extractLibraries(NetworkTablesPlugin.class,
        "/ResourceInformation-NetworkTables.json");
    CombinedRuntimeLoader.loadLibrary("ntcorejni", files);

    return NetworkTableInstance.getDefault();
  }

  /**
   * Constructs the NetworkTables plugin.
   * 
   * @throws IOException
   */
  public NetworkTablesPlugin() throws IOException {
    this(getDefaultInstance());
  }

  /**
   * Constructs the NetworkTables plugin.
   */
  public NetworkTablesPlugin(NetworkTableInstance inst) {
    this.inst = inst;

    serverChangeListener = (observable, oldValue, newValue) -> {
      var hostInfoOpt = hostParser.parse(newValue);

      if (hostInfoOpt.isEmpty()) {
        // Invalid input - reset to previous value
        serverId.setValue(oldValue);
        return;
      }

      var hostInfo = hostInfoOpt.get();

      inst.stopDSClient();
      inst.stopClient();
      inst.stopServer();

      if (hostInfo.getTeam().isPresent()) {
        inst.setServerTeam(hostInfo.getTeam().getAsInt(), hostInfo.getPort());
      } else if (hostInfo.getHost().isEmpty()) {
        inst.setServer("localhost", hostInfo.getPort());
      } else {
        inst.setServer(hostInfo.getHost(), hostInfo.getPort());
      }
      inst.startClient4("shuffleboard");
      inst.startDSClient();
    };
  }

  public void onLoad() {

    serverChangeListener.changed(null, null, serverId.get());
    serverId.addListener(serverChangeListener);
  }

  public void onUnload() {
    inst.stopDSClient();
    inst.stopClient();
    inst.stopServer();
  }

  public String getServerId() {
    return serverId.get();
  }

  public StringProperty serverIdProperty() {
    return serverId;
  }

  public void setServerId(String serverId) {
    this.serverId.set(serverId);
  }

}
