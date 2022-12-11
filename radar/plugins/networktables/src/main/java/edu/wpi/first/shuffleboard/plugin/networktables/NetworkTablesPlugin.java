package edu.wpi.first.shuffleboard.plugin.networktables;


import edu.wpi.first.shuffleboard.api.plugin.Plugin;
import edu.wpi.first.shuffleboard.plugin.networktables.util.NetworkTableUtils;
import edu.wpi.first.util.CombinedRuntimeLoader;

import edu.wpi.first.networktables.NetworkTableInstance;
import edu.wpi.first.networktables.NetworkTablesJNI;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.beans.value.ChangeListener;


public class NetworkTablesPlugin extends Plugin {

  private int recorderUid = -1;
  private static final Logger log = Logger.getLogger(NetworkTablesPlugin.class.getName());
  private final NetworkTableInstance inst;

  private final StringProperty serverId = new SimpleStringProperty(this, "server", "localhost");
  






  private final HostParser hostParser = new HostParser();

  private final ChangeListener<String> serverChangeListener;

  private static NetworkTableInstance getDefaultInstance() {
    NetworkTablesJNI.Helper.setExtractOnStaticLoad(false);
    try {
      var files = CombinedRuntimeLoader.extractLibraries(NetworkTablesPlugin.class,
          "/ResourceInformation-NetworkTables.json");
      CombinedRuntimeLoader.loadLibrary("ntcorejni", files);
    } catch (IOException ex) {
      log.log(Level.SEVERE, "Failed to load NT Core Libraries", ex);
    }

    return NetworkTableInstance.getDefault();
  }

  /**
   * Constructs the NetworkTables plugin.
   */
  public NetworkTablesPlugin() {
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

      NetworkTableUtils.shutdown(inst);

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

  @Override
  public void onLoad() {





   


    serverChangeListener.changed(null, null, serverId.get());
    serverId.addListener(serverChangeListener);
  }

  @Override
  public void onUnload() {
  


    NetworkTablesJNI.removeListener(recorderUid);
    NetworkTableUtils.shutdown(inst);
   
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
