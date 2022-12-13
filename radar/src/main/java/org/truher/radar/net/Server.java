package org.truher.radar.net;

import edu.wpi.first.networktables.NetworkTableInstance;

/**
 * Runs an NT server for the demo.
 */
public class Server {
    static {
        NTSetup.setup();
    }

    private final NetworkTableInstance inst;

    public Server() {
        inst = NetworkTableInstance.getDefault();
    }

    /**
     * Starts the NT server and returns.
     */
    public void start() {
        System.out.println("starting server");
        inst.startServer("Radar Demo Server");
    }

    /**
     * Closes the NT server and returns.
     */
    public void close() {
        System.out.println("closing server");
        inst.close();
    }
}
