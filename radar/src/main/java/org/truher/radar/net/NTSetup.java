package org.truher.radar.net;

import java.io.IOException;

import edu.wpi.first.net.WPINetJNI;
import edu.wpi.first.networktables.NetworkTablesJNI;
import edu.wpi.first.util.CombinedRuntimeLoader;
import edu.wpi.first.util.WPIUtilJNI;

/**
 * Static initializer loads all the JNI you need for Network Tables.
 * 
 * According to Dependency Walker ntcore depends on wpiutil and wpinet.
 */
public class NTSetup {
    static {
        // Turns off the native loaders in static initializers, which only work
        // if the cache is already populated.
        NetworkTablesJNI.Helper.setExtractOnStaticLoad(false);
        WPINetJNI.Helper.setExtractOnStaticLoad(false);
        WPIUtilJNI.Helper.setExtractOnStaticLoad(false);

        // Extracts specified dlls from the jar if they're listed in
        // ResourceInformation.json, and loads them. 
        // loadLibraries sets the DLL directory so that windows can find dependencies
        // even if they don't happen to be listed in dependency order.
        try {
            CombinedRuntimeLoader.loadLibraries(Publisher.class, "wpinetjni", "ntcorejni", "wpiutiljni");
        } catch (IOException e) {
            e.printStackTrace();
            throw new IllegalStateException("could not load native libs");
        }
    }
    public static void setup() {
        System.out.println("ntcore loaded");
    }
}
