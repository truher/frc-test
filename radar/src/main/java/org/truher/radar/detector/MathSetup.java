package org.truher.radar.detector;

import java.io.IOException;

import edu.wpi.first.math.WPIMathJNI;
import edu.wpi.first.util.CombinedRuntimeLoader;
import edu.wpi.first.util.WPIUtilJNI;

/**
 * Static initalizer loads the JNI you need for WPIMath.
 * 
 * According to Dependency Walker wpimath depends on wpiutil.
 */
public class MathSetup {
    static {
        // Turns off the native loaders in static initializers, which only work
        // if the cache is already populated.
        WPIMathJNI.Helper.setExtractOnStaticLoad(false);
        WPIUtilJNI.Helper.setExtractOnStaticLoad(false);

        // Extracts specified dlls from the jar if they're listed in
        // ResourceInformation.json, and loads them. 
        // loadLibraries sets the DLL directory so that windows can find dependencies
        // even if they don't happen to be listed in dependency order.
        try {
            CombinedRuntimeLoader.loadLibraries(MathSetup.class,   "wpiutiljni", "wpimathjni");
        } catch (IOException e) {
            e.printStackTrace();
            throw new IllegalStateException("could not load native libs");
        }
    }
    public static void setup() {
        System.out.println("wpimath loaded");
    }
}
