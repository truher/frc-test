package org.truher.radar;

import java.util.Random;

import org.msgpack.jackson.dataformat.MessagePackFactory;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import edu.wpi.first.math.geometry.Pose3d;
import edu.wpi.first.math.geometry.Rotation3d;
import edu.wpi.first.networktables.NetworkTable;
import edu.wpi.first.networktables.NetworkTableInstance;
import edu.wpi.first.networktables.RawPublisher;

/**
 * Publishes the target list.
 * 
 * Imagine this is the camera-based localizer.
 */
public class TargetPublisher {
    TargetList publisherTargetList = new TargetList();
    RawPublisher targetListPublisher;
    Random rand = new Random();
    private static final int walkPerStep = 10;
    private static final double radPerStep = 0.25;

    public TargetPublisher() {
        System.out.println("publisher ctor start");
        Target t = new Target();
        t.id = 0;
        t.pose = new Pose3d(-100, -100, 0, new Rotation3d());
        publisherTargetList.targets.add(t);
        t = new Target();
        t.id = 1;
        t.pose = new Pose3d(100, -100, 0, new Rotation3d());
        publisherTargetList.targets.add(t);
        t = new Target();
        t.id = 2;
        t.pose = new Pose3d(100, 100, 0, new Rotation3d());
        publisherTargetList.targets.add(t);
        t = new Target();
        t.id = 3;
        t.pose = new Pose3d(-100, 100, 350, new Rotation3d());
        publisherTargetList.targets.add(t);
        System.out.println("publisher ctor done");
    }

    /**
     * Moves the targets a little.
     */
    public void run() {
        System.out.println("publisher run start");

        NetworkTableInstance inst = NetworkTableInstance.getDefault();
        inst.startClient4("Radar Publisher");
        inst.setServer("localhost");
        NetworkTable table = inst.getTable("radar");

        targetListPublisher = table.getRawTopic("targets").publish("msgpack"); // the string "msgpack" is magic to glass

        publish();

        while (true) {
            try {
                Thread.sleep(100);
                System.out.println("publish");
                for (int i = 0; i < publisherTargetList.targets.size(); ++i) {
                    Pose3d oldPose = publisherTargetList.targets.get(i).pose;
                    int dx = rand.nextInt(2 * walkPerStep - 1) - walkPerStep;
                    int dy = rand.nextInt(2 * walkPerStep - 1) - walkPerStep;
                    double drot = rand.nextDouble(-radPerStep, radPerStep);
                    publisherTargetList.targets.get(i).pose = new Pose3d(
                            oldPose.getX() + dx,
                            oldPose.getY() + dy,
                            oldPose.getZ(),
                            oldPose.getRotation().plus(new Rotation3d(0, 0, drot)));
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
            byte[] bytes = objectMapper.writeValueAsBytes(publisherTargetList);
            targetListPublisher.set(bytes);
        } catch (JsonProcessingException e) {
            System.out.println("publisher exception");
        }
    }
}
