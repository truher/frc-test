package org.truher.radar;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.geom.Path2D;
import java.awt.geom.AffineTransform;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Line2D;
import java.io.IOException;
import java.util.EnumSet;

import javax.swing.JFrame;
import javax.swing.JPanel;
import org.msgpack.jackson.dataformat.MessagePackFactory;

import com.fasterxml.jackson.databind.ObjectMapper;

import edu.wpi.first.networktables.NetworkTable;
import edu.wpi.first.networktables.NetworkTableEvent;
import edu.wpi.first.networktables.NetworkTableInstance;

/**
 * Listens for updates to the target list and renders them.
 */
public class TargetSubscriber extends JPanel {
    private static final int BOX_WIDTH = 50;
    private static final int BOX_HEIGHT = 50;
    private static final int WINDOW_HEIGHT = 800;
    private static final int WINDOW_WIDTH = 800;
    private static final int RADIUS = 350;
    TargetList subscriberTargetList;

    public TargetSubscriber() {
        JFrame frame = new JFrame("demo");
        frame.add(this);
        // someday, account for title bar and borders correctly
        // also allow resizing etc.
        frame.setSize(WINDOW_WIDTH+30, WINDOW_HEIGHT+60);
        frame.setVisible(true);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    }

    /**
     * Registers update listener and returns.
     */
    public void run() {
        System.out.println("subscriber run start");
        NetworkTableInstance inst = NetworkTableInstance.getDefault();
        inst.startClient4("Radar Subscriber");
        inst.setServer("localhost");
        NetworkTable table = inst.getTable("radar");
        inst.addListener(
                table.getEntry("targets"),
                EnumSet.of(NetworkTableEvent.Kind.kValueAll),
                (event) -> render(event));

        inst.startClient4("localhost");
        System.out.println("subscriber run done");

    }

    /**
     * Deserializes the target list to a member, and asks for repainting
     */
    private void render(NetworkTableEvent event) {
        byte[] newBytes = event.valueData.value.getRaw();
        ObjectMapper objectMapper = new ObjectMapper(new MessagePackFactory());
        try {
            subscriberTargetList = objectMapper.readValue(newBytes, TargetList.class);
            for (Target t : subscriberTargetList.targets) {
                System.out.printf("target id: %2d x: %6.1f y: %6.1f yaw: %6.1f\n",
                 t.id, t.pose.getTranslation().getX(),
                 t.pose.getTranslation().getY(),
                 t.pose.getRotation().getZ());
            }
            this.repaint();
        } catch (IOException e) {
            System.out.println("deserialization failed");
        }
    }

    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D graphic2d = (Graphics2D) g;
        graphic2d.setColor(Color.BLACK);
        graphic2d.setStroke(new BasicStroke(1));
        Ellipse2D e = new Ellipse2D.Double(
                WINDOW_WIDTH / 2 - RADIUS, WINDOW_HEIGHT / 2 - RADIUS, 2 * RADIUS,
                2 * RADIUS);
        graphic2d.draw(e);
        Line2D l = new Line2D.Double(0, WINDOW_HEIGHT/2, WINDOW_WIDTH, WINDOW_HEIGHT/2);
        graphic2d.draw(l);
        l = new Line2D.Double(WINDOW_WIDTH/2, 0, WINDOW_WIDTH/2, WINDOW_HEIGHT);
        graphic2d.draw(l);
        graphic2d.setColor(Color.BLUE);
        graphic2d.setStroke(new BasicStroke(3));
        graphic2d.setFont(new Font("Helvetica", Font.PLAIN, 18));
        FontMetrics fm = graphic2d.getFontMetrics();
        if (subscriberTargetList == null) {
            return;
        }
        for (Target t : subscriberTargetList.targets) {
            Rectangle r = new Rectangle((int) (-BOX_WIDTH / 2),
                    (int) (-BOX_HEIGHT / 2), BOX_WIDTH, BOX_HEIGHT);
            Path2D.Double path = new Path2D.Double();
            path.append(r, false);
            AffineTransform tr = AffineTransform.getRotateInstance(t.pose.getRotation().getZ()); // yaw
            path.transform(tr);
            tr = AffineTransform.getTranslateInstance(
                    WINDOW_WIDTH / 2 + (int) t.pose.getX(),
                    WINDOW_HEIGHT / 2 + (int) t.pose.getY());
            path.transform(tr);
            graphic2d.draw(path);
            String label = String.format("id %d", t.id);
            graphic2d.drawString(label,
                    WINDOW_WIDTH / 2 + (int) t.pose.getX() - fm.stringWidth(label) / 2,
                    WINDOW_HEIGHT / 2 + (int) t.pose.getY() + fm.getAscent() / 2);
        }
    }
}
