package org.truher.radar.net;

import java.util.HashMap;
import java.util.Map;
import java.util.function.BiConsumer;

import org.msgpack.jackson.dataformat.MessagePackFactory;
import org.truher.radar.model.TargetList;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import edu.wpi.first.networktables.NetworkTable;
import edu.wpi.first.networktables.NetworkTableInstance;
import edu.wpi.first.networktables.RawPublisher;

/**
 * Publishes the msgpack'ed target list to the specified topic.
 */
public class Publisher implements BiConsumer<String, TargetList> {
    static {
       NTSetup.setup();
    }

    private final NetworkTableInstance inst;
    private final Map<String, RawPublisher> publishers;
    private final ObjectMapper objectMapper;

    public Publisher() {
        inst = NetworkTableInstance.getDefault();
        publishers = new HashMap<String, RawPublisher>();
        objectMapper = new ObjectMapper(new MessagePackFactory());
    }

    /**
     * Closes the NT client and returns.
     */
    public void close() {
        System.out.println("closing publisher");
        inst.close();
    }

    /**
     * Drive around and publish targets.
     */
    public void start() {
        System.out.println("starting publisher");
        inst.setServer("localhost", NetworkTableInstance.kDefaultPort4);
    }

    /**
     * Msgpack the list and publish it on the topic.
     */
    @Override
    public void accept(String topic, TargetList list) {
        if (!publishers.containsKey(topic)) {
            NetworkTable table = inst.getTable("radar");
            // The type "msgpack" is known to glass
            publishers.put(topic, table.getRawTopic(topic).publish("msgpack"));
        }
        try {
            publishers.get(topic).set(objectMapper.writeValueAsBytes(list));
        } catch (JsonProcessingException e) {
            System.out.printf("publish failed for topic '%s'.\n", topic);
            e.printStackTrace();
        }
    }
}
