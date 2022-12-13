package org.truher.radar.model;

import org.truher.radar.detector.MathSetup;

import edu.wpi.first.math.geometry.Pose2d;

/**
 * This is the payload encoded into MessagePack with Jackson and, somewhat
 * magically, deserialized even though the constructor is not visible and the
 * fields are final.
 */
public class Target {
    static {
        new MathSetup();
    }
    public enum Type {
        TAG, ALLY, OPPONENT, SELF, DEFAULT
    }

    public final Type type;
    public final int id;
    public Pose2d pose; // mutable because the "self" target moves around without being reaquired.

    public Target(Type type, int id, Pose2d pose) {
        this.type = type;
        this.id = id;
        this.pose = pose;
    }

    protected Target() {
        this.type = Type.DEFAULT;
        this.id = 0;
        this.pose = null;
    }

    public static Target newSelf() {
        return new Target(Type.SELF, 0, new Pose2d());
    }

    @Override
    public String toString() {
        return "Target [type=" + type + ", id=" + id + ", pose=" + pose + "]";
    }

}
