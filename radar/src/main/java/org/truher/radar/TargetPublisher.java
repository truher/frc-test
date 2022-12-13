package org.truher.radar;

import java.util.Random;

import org.msgpack.jackson.dataformat.MessagePackFactory;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import edu.wpi.first.math.geometry.Pose2d;
import edu.wpi.first.math.geometry.Rotation2d;
import edu.wpi.first.math.geometry.Transform2d;
import edu.wpi.first.math.geometry.Translation2d;
import edu.wpi.first.networktables.NetworkTable;
import edu.wpi.first.networktables.NetworkTableInstance;
import edu.wpi.first.networktables.RawPublisher;

/**
 * Publishes the target list.
 * 
 * Imagine these are AprilTag poses derived from a camera.
 * 
 * Targets are fixed to the earth; observer goes back and forth.
 */
public class TargetPublisher {
    // positions relative to the earth
    TargetList targetMap = new TargetList();
    RawPublisher mapPublisher;
    Target observer = new Target(Target.Type.SELF, 0, new Pose2d());

    // positions relative to the robot
    TargetList publisherTargetList = new TargetList();
    RawPublisher targetListPublisher;
    Random rand = new Random();
    private static final int walkPerStep = 10;
    int steps = 0;

    public TargetPublisher() {
        targetMap.targets.add(new Target(Target.Type.TAG, 0, new Pose2d(0, -200, Rotation2d.fromDegrees(-90))));
        targetMap.targets.add(new Target(Target.Type.TAG, 1, new Pose2d(200, -200, Rotation2d.fromDegrees(-90))));
        targetMap.targets.add(new Target(Target.Type.TAG, 2, new Pose2d(200, 200, Rotation2d.fromDegrees(90))));
        targetMap.targets.add(new Target(Target.Type.TAG, 3, new Pose2d(0, 200, Rotation2d.fromDegrees(90))));
        targetMap.targets.add(new Target(Target.Type.ALLY, 4, new Pose2d(-100, 0, new Rotation2d())));
        targetMap.targets.add(new Target(Target.Type.OPPONENT, 5, new Pose2d(300, 0, new Rotation2d())));
        targetMap.targets.add(observer);
    }

    /**
     * Drive around and publish targets.
     */
    public void run() {
        NetworkTableInstance inst = NetworkTableInstance.getDefault();
        inst.startServer("Radar Publisher");
        NetworkTable table = inst.getTable("radar");

        // The type "msgpack" is known to glass
        targetListPublisher = table.getRawTopic("targets").publish("msgpack");
        mapPublisher = table.getRawTopic("map").publish("msgpack");

        publish();

        while (true) {
            try {
                steps += 1;
                int phase = steps % 80;
                if (phase < 20) {
                    // walking
                    observer.pose = observer.pose
                            .plus(new Transform2d(new Translation2d(walkPerStep, 0), new Rotation2d()));
                } else if (phase < 40) {
                    // turning
                    observer.pose = observer.pose
                            .plus(new Transform2d(new Translation2d(walkPerStep, 0), Rotation2d.fromDegrees(180 / 20)));
                } else if (phase < 60) {
                    // walking
                    observer.pose = observer.pose
                            .plus(new Transform2d(new Translation2d(walkPerStep, 0), new Rotation2d()));
                } else {
                    // turning
                    observer.pose = observer.pose
                            .plus(new Transform2d(new Translation2d(walkPerStep, 0), Rotation2d.fromDegrees(180 / 20)));
                }
                Thread.sleep(50);
                publisherTargetList.targets.clear();
                for (Target mapTarget : targetMap.targets) {
                    publisherTargetList.targets
                            .add(new Target(mapTarget.type, mapTarget.id, mapTarget.pose.relativeTo(observer.pose)));
                }
                publish();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    private void publish() {
        ObjectMapper objectMapper = new ObjectMapper(new MessagePackFactory());
        try {
            targetListPublisher.set(objectMapper.writeValueAsBytes(publisherTargetList));
            mapPublisher.set(objectMapper.writeValueAsBytes(targetMap));
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
    }
}
