package org.truher.radar.detector;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.function.BiConsumer;

import org.truher.radar.model.Target;
import org.truher.radar.model.TargetList;

import edu.wpi.first.math.geometry.Pose2d;
import edu.wpi.first.math.geometry.Rotation2d;
import edu.wpi.first.math.geometry.Transform2d;
import edu.wpi.first.math.geometry.Translation2d;

/**
 * Produces fake targets for the demo.
 * 
 * Imagine these are AprilTag poses derived from a camera.
 * 
 * Targets are fixed to the earth; observer goes back and forth.
 */
public class Producer {
    static {
        new MathSetup();
    }

    private static final int PX_PER_STEP = 10;

    private final BiConsumer<String, TargetList> publisher;
    private final ScheduledExecutorService scheduler;

    // positions relative to the earth
    private final TargetList targetMap;
    private final Target observer;

    // positions relative to the robot
    private final TargetList publisherTargetList;

    int steps = 0;

    public Producer(BiConsumer<String, TargetList> publisher) {
        this.publisher = publisher;
        this.targetMap = new TargetList();
        this.scheduler = Executors.newScheduledThreadPool(1);
        this.observer = new Target(Target.Type.SELF, 0, new Pose2d());
        this.publisherTargetList = new TargetList();
    }

    /**
     * Closes the NT client and returns.
     */
    public void close() {
        System.out.println("closing publisher");
        scheduler.shutdown();
    }

    /**
     * Set up targets and scheduler, and return.
     */
    public void start() {
        System.out.println("starting producer");
        targetMap.targets.add(new Target(Target.Type.TAG, 0, new Pose2d(0, -200, Rotation2d.fromDegrees(-90))));
        targetMap.targets.add(new Target(Target.Type.TAG, 1, new Pose2d(200, -200, Rotation2d.fromDegrees(-90))));
        targetMap.targets.add(new Target(Target.Type.TAG, 2, new Pose2d(200, 200, Rotation2d.fromDegrees(90))));
        targetMap.targets.add(new Target(Target.Type.TAG, 3, new Pose2d(0, 200, Rotation2d.fromDegrees(90))));
        targetMap.targets.add(new Target(Target.Type.ALLY, 4, new Pose2d(-100, 0, new Rotation2d())));
        targetMap.targets.add(new Target(Target.Type.OPPONENT, 5, new Pose2d(300, 0, new Rotation2d())));
        targetMap.targets.add(observer);
        scheduler.scheduleAtFixedRate(this::step, 0, 50, TimeUnit.MILLISECONDS);
    }

    /**
     * Drive one step, recalculate targets.
     */
    private void step() {
        steps += 1;
        int phase = steps % 80;
        if (phase < 20) {
            // walking
            observer.pose = observer.pose
                    .plus(new Transform2d(new Translation2d(PX_PER_STEP, 0), new Rotation2d()));
        } else if (phase < 40) {
            // turning
            observer.pose = observer.pose
                    .plus(new Transform2d(new Translation2d(PX_PER_STEP, 0), Rotation2d.fromDegrees(180 / 20)));
        } else if (phase < 60) {
            // walking
            observer.pose = observer.pose
                    .plus(new Transform2d(new Translation2d(PX_PER_STEP, 0), new Rotation2d()));
        } else {
            // turning
            observer.pose = observer.pose
                    .plus(new Transform2d(new Translation2d(PX_PER_STEP, 0), Rotation2d.fromDegrees(180 / 20)));
        }
        publisherTargetList.targets.clear();
        for (Target mapTarget : targetMap.targets) {
            publisherTargetList.targets
                    .add(new Target(mapTarget.type, mapTarget.id, mapTarget.pose.relativeTo(observer.pose)));
        }

        publisher.accept("targets", publisherTargetList);
        publisher.accept("map", targetMap);
    }
}
