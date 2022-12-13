package org.truher.radar.net;

import java.io.IOException;
import java.util.EnumSet;
import java.util.function.Consumer;

import org.msgpack.jackson.dataformat.MessagePackFactory;
import org.truher.radar.model.TargetList;

import com.fasterxml.jackson.databind.ObjectMapper;

import edu.wpi.first.networktables.NetworkTable;
import edu.wpi.first.networktables.NetworkTableEvent;
import edu.wpi.first.networktables.NetworkTableInstance;

/**
 * Listens for updates to the target list, deserializes them, and gives them to
 * the consumer.
 */
public class Subscriber {
    static {
        NTSetup.setup();
    }
    private final Consumer<TargetList> consumer;
    private final String topicName;
    private final ObjectMapper objectMapper;
    private final NetworkTableInstance inst;

    public Subscriber(String topicName, Consumer<TargetList> consumer) {
        this.consumer = consumer;
        this.topicName = topicName;
        this.objectMapper = new ObjectMapper(new MessagePackFactory());
        this.inst = NetworkTableInstance.getDefault();
    }

    /**
     * Registers update listener and returns.
     */
    public void start() {
        System.out.printf("starting subscriber '%s'\n", topicName);
        inst.startClient4("Radar Subscriber");
        inst.setServer("localhost", NetworkTableInstance.kDefaultPort4);
        inst.startDSClient(); // use the DS addr if it exists

        NetworkTable table = inst.getTable("radar");

        inst.addListener(
                table.getEntry(topicName),
                EnumSet.of(NetworkTableEvent.Kind.kValueAll),
                (event) -> render(event));
    }

    /**
     * Closes the NT client and returns.
     */
    public void close() {
        System.out.printf("closing subscriber '%s'\n", topicName);
        inst.close();
    }

    /**
     * Deserializes the target list and gives it to the consumer.
     */
    private void render(NetworkTableEvent event) {
        try {
            consumer.accept(objectMapper.readValue(event.valueData.value.getRaw(), TargetList.class));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
