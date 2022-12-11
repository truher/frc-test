package frc.robot;

import java.awt.*;
import java.awt.geom.*;
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
    private static final int WINDOW_HEIGHT = 500;
    private static final int WINDOW_WIDTH = 1000;
    TargetList subscriberTargetList;

    public TargetSubscriber() {
        JFrame frame = new JFrame("demo");
        frame.add(this);
        frame.setSize(WINDOW_WIDTH, WINDOW_HEIGHT);
        frame.setVisible(true);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    }

    /**
     * Registers update listener and returns.
     */
    public void run() {
        NetworkTableInstance inst = NetworkTableInstance.getDefault();
        inst.startClient4("Radar Subscriber");
        inst.setServer("localhost");
        NetworkTable table = inst.getTable("radar");
        inst.addListener(
                table.getEntry("targets"),
                EnumSet.of(NetworkTableEvent.Kind.kValueAll),
                (event) -> render(event));

        inst.startClient4("localhost");
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
                System.out.printf("target id: %d pose: %s\n", t.id, t.pose);
                System.out.println(t.pose.getRotation().getAngle());
            }
            this.repaint();
        } catch (IOException e) {
            System.out.println("deserialization failed");
        }
    }

    @Override
    public void paintComponent(Graphics g) {
        System.out.println("paint");
        super.paintComponent(g);
        Graphics2D graphic2d = (Graphics2D) g;
        graphic2d.setColor(Color.BLUE);
        graphic2d.setStroke(new BasicStroke(3));
        graphic2d.setFont(new Font("Helvetica", Font.PLAIN, 18));
        FontMetrics fm = graphic2d.getFontMetrics();

        if (subscriberTargetList == null) {
            return;
        }
        for (Target t : subscriberTargetList.targets) {
            graphic2d.drawRect(WINDOW_WIDTH / 2 + (int) (t.pose.getX() - BOX_WIDTH / 2),
                    WINDOW_HEIGHT / 2 + (int) (t.pose.getY() - BOX_HEIGHT / 2), BOX_WIDTH, BOX_HEIGHT);

            Rectangle r = new Rectangle(
                WINDOW_WIDTH / 2 + (int) (t.pose.getX() - BOX_WIDTH / 2),
                WINDOW_HEIGHT / 2 + (int) (t.pose.getY() - BOX_HEIGHT / 2), BOX_WIDTH, BOX_HEIGHT);

            Path2D.Double path = new Path2D.Double();
            path.append(r, false);

            AffineTransform tr = new AffineTransform();
            double angle = t.pose.getRotation().getAngle();
            tr.rotate(angle);
            path.transform(tr);
            graphic2d.draw(path);
            String label = String.format("id %d", t.id);
            graphic2d.drawString(label,
                    WINDOW_WIDTH / 2 + (int) t.pose.getX() - fm.stringWidth(label) / 2,
                    WINDOW_HEIGHT / 2 + (int) t.pose.getY() + fm.getAscent() / 2);
        }

    }
}
