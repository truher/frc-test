package frc.robot;

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

    public TargetPublisher() {
        Target t = new Target();
        t.id = 0;
        t.pose = new Pose3d(-100, -200, 300, new Rotation3d(0, 0, 0));
        publisherTargetList.targets.add(t);
        t = new Target();
        t.id = 1;
        t.pose = new Pose3d(125, -225, 325, new Rotation3d(4.25, 5.25, 6.25));
        publisherTargetList.targets.add(t);
        t = new Target();
        t.id = 2;
        t.pose = new Pose3d(-150, 250, 350, new Rotation3d(4.5, 5.5, 6.5));
        publisherTargetList.targets.add(t);
    }

    /**
     * Moves the targets a little.
     */
    public void run() {
        System.out.println("running");

        NetworkTableInstance inst = NetworkTableInstance.getDefault();
        inst.startClient4("Radar Publisher");
        inst.setServer("localhost");
        NetworkTable table = inst.getTable("radar");

        targetListPublisher = table.getRawTopic("targets").publish("msgpack"); // the string "msgpack" is magic to glass

        publish();

        while (true) {
            try {
                Thread.sleep(250);
                System.out.println("running");
                Pose3d oldPose = publisherTargetList.targets.get(0).pose;
                publisherTargetList.targets.get(0).pose = new Pose3d(
                        oldPose.getX() + 5,
                        oldPose.getY() + 5,
                        oldPose.getZ() + 5,
                        oldPose.getRotation().plus(new Rotation3d(0,0,0.1)) );
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
