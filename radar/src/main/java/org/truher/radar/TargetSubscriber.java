package org.truher.radar;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Line2D;
import java.awt.geom.Path2D;
import java.awt.geom.Point2D;
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
 * 
 * There are both robot-relative "head up" and field-relative "north up"
 * displays, using
 * two different NetworkTable topics. Both use x-up and y-left. Symbols come
 * from NATO APP-6.
 * 
 * TODO: split the thing i'm actually trying to illustrate, which is the NT4
 * part, from the rendering part.
 */
public class TargetSubscriber extends JPanel {
    private static final Color FRAME_COLOR = new Color(0, 0, 0); // black
    private static final Color TAG_COLOR = new Color(170, 255, 170); // light green
    private static final Color ALLY_COLOR = new Color(0, 255, 255); // cyan
    private static final Color OPPONENT_COLOR = new Color(255, 0, 0); // red
    private static final int SCALE = 40; // scale of symbols
    private static final int WINDOW_HEIGHT = 800;
    private static final int WINDOW_WIDTH = 800;
    private static final int RADIUS = 350;
    TargetList subscriberTargetList;
    private final String topicName;

    public TargetSubscriber(String topicName) {
        JFrame frame = new JFrame("demo");
        frame.add(this);
        // TODO: account for title bar and borders correctly
        // also allow resizing etc.
        frame.setSize(WINDOW_WIDTH + 30, WINDOW_HEIGHT + 60);
        frame.setVisible(true);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.topicName = topicName;
    }

    /**
     * Registers update listener and returns.
     */
    public void run() {
        NetworkTableInstance inst = NetworkTableInstance.getDefault();
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
     * Deserializes the target list to a member, and asks for repainting
     */
    private void render(NetworkTableEvent event) {
        byte[] newBytes = event.valueData.value.getRaw();
        ObjectMapper objectMapper = new ObjectMapper(new MessagePackFactory());
        try {
            subscriberTargetList = objectMapper.readValue(newBytes, TargetList.class);
            repaint();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D graphic2d = (Graphics2D) g;
        drawCrosshairs(graphic2d);
        if (subscriberTargetList == null) {
            return;
        }
        for (Target target : subscriberTargetList.targets) {
            switch (target.type) {
                case TAG:
                    renderTag(graphic2d, target);
                    break;
                case ALLY:
                    renderAlly(graphic2d, target);
                    break;
                case OPPONENT:
                    renderOpponent(graphic2d, target);
                    break;
                case SELF:
                    renderSelf(graphic2d, target);
                    break;
                default:
                    System.out.println("skipping unknown target type");
            }
        }
    }

    private void drawCrosshairs(Graphics2D graphic2d) {
        graphic2d.setColor(Color.BLACK);
        graphic2d.setStroke(new BasicStroke(1));
        graphic2d.draw(
                new Ellipse2D.Double(WINDOW_WIDTH / 2 - RADIUS, WINDOW_HEIGHT / 2 - RADIUS, 2 * RADIUS, 2 * RADIUS));
        graphic2d.draw(new Line2D.Double(0, WINDOW_HEIGHT / 2, WINDOW_WIDTH, WINDOW_HEIGHT / 2));
        graphic2d.draw(new Line2D.Double(WINDOW_WIDTH / 2, 0, WINDOW_WIDTH / 2, WINDOW_HEIGHT));
    }

    /**
     * Three sided box with id. center of the long side is the reference point.
     * 
     * Open side of the box is in the zero-yaw direction.
     */
    private void renderTag(Graphics2D graphic2d, Target target) {
        final int BOX_WIDTH = SCALE;
        final int BOX_HEIGHT = (int) (SCALE * 0.5);
        Path2D.Double path = new Path2D.Double();
        path.moveTo(-BOX_WIDTH / 2, -BOX_HEIGHT);
        path.lineTo(-BOX_WIDTH / 2, 0);
        path.lineTo(BOX_WIDTH / 2, 0);
        path.lineTo(BOX_WIDTH / 2, -BOX_HEIGHT);

        // note inverse rotation
        AffineTransform rotationTransform = AffineTransform.getRotateInstance(-target.pose.getRotation().getRadians()); // yaw
        path.transform(rotationTransform);

        // note reversed axes
        AffineTransform translationTransform = AffineTransform.getTranslateInstance(
                WINDOW_WIDTH / 2 - (int) target.pose.getY(),
                WINDOW_HEIGHT / 2 - (int) target.pose.getX());
        path.transform(translationTransform);

        graphic2d.setColor(TAG_COLOR);
        graphic2d.fill(path);

        graphic2d.setColor(FRAME_COLOR);
        graphic2d.setStroke(new BasicStroke(1));
        graphic2d.draw(path);

        graphic2d.setFont(new Font("Helvetica", Font.PLAIN, 18));
        FontMetrics fm = graphic2d.getFontMetrics();
        String label = String.format("%d", target.id);
        // offsets in text coordinates (i.e. center to origin which is the bottom left
        // corner)
        // note reversed axes
        int xOffset = WINDOW_WIDTH / 2 - (int) target.pose.getY() - fm.stringWidth(label) / 2;
        int yOffset = WINDOW_HEIGHT / 2 - (int) target.pose.getX() + fm.getAscent() / 2;
        // unrotated offset from object reference point to text center
        Point2D textCenter = new Point2D.Double(0, -BOX_HEIGHT / 2);
        Point2D rotatedTextCenter = rotationTransform.transform(textCenter, null);
        graphic2d.drawString(label, xOffset + (int) rotatedTextCenter.getX(), yOffset + (int) rotatedTextCenter.getY());
    }

    /**
     * Ally is a rectangle centered on the reference point. No rotation, no label.
     */
    private void renderAlly(Graphics2D graphic2d, Target target) {
        final int BOX_HEIGHT = SCALE;
        final int BOX_WIDTH = (int) (SCALE * 1.6);
        Path2D.Double path = new Path2D.Double();
        path.moveTo(-BOX_WIDTH / 2, -BOX_HEIGHT / 2);
        path.lineTo(-BOX_WIDTH / 2, BOX_HEIGHT / 2);
        path.lineTo(BOX_WIDTH / 2, BOX_HEIGHT / 2);
        path.lineTo(BOX_WIDTH / 2, -BOX_HEIGHT / 2);
        path.closePath();

        // note reversed axes
        AffineTransform translationTransform = AffineTransform.getTranslateInstance(
                WINDOW_WIDTH / 2 - (int) target.pose.getY(),
                WINDOW_HEIGHT / 2 - (int) target.pose.getX());
        path.transform(translationTransform);

        graphic2d.setColor(ALLY_COLOR);
        graphic2d.fill(path);

        graphic2d.setColor(FRAME_COLOR);
        graphic2d.setStroke(new BasicStroke(1));
        graphic2d.draw(path);
    }

    /**
     * Opponent is a diamond centered on the reference point. No rotation, no label.
     */
    private void renderOpponent(Graphics2D graphic2d, Target target) {
        final int BOX_HEIGHT = (int) (SCALE * Math.sqrt(2));
        final int BOX_WIDTH = (int) (SCALE * Math.sqrt(2));
        Path2D.Double path = new Path2D.Double();
        path.moveTo(-BOX_WIDTH / 2, 0);
        path.lineTo(0, -BOX_HEIGHT / 2);
        path.lineTo(BOX_WIDTH / 2, 0);
        path.lineTo(0, BOX_HEIGHT / 2);
        path.closePath();

        // note reversed axes
        AffineTransform translationTransform = AffineTransform.getTranslateInstance(
                WINDOW_WIDTH / 2 - (int) target.pose.getY(),
                WINDOW_HEIGHT / 2 - (int) target.pose.getX());
        path.transform(translationTransform);

        graphic2d.setColor(OPPONENT_COLOR);
        graphic2d.fill(path);

        graphic2d.setColor(Color.BLACK);
        graphic2d.setStroke(new BasicStroke(1));
        graphic2d.draw(path);
    }

    /**
     * Self is a triangle pointing at the rotation.
     */
    private void renderSelf(Graphics2D graphic2d, Target target) {
        final int BOX_HEIGHT = (int) (SCALE);
        final int BOX_WIDTH = (int) (SCALE);
        Path2D.Double path = new Path2D.Double();
        path.moveTo(0, -BOX_HEIGHT / 2);
        path.lineTo(-BOX_WIDTH / 2, BOX_HEIGHT / 2);
        path.lineTo(BOX_WIDTH / 2, BOX_HEIGHT / 2);
        path.closePath();

        // note inverse rotation
        AffineTransform rotationTransform = AffineTransform.getRotateInstance(-target.pose.getRotation().getRadians()); // yaw
        path.transform(rotationTransform);

        // note reversed axes
        AffineTransform translationTransform = AffineTransform.getTranslateInstance(
                WINDOW_WIDTH / 2 - (int) target.pose.getY(),
                WINDOW_HEIGHT / 2 - (int) target.pose.getX());
        path.transform(translationTransform);

        graphic2d.setColor(ALLY_COLOR);
        graphic2d.fill(path);

        graphic2d.setColor(Color.BLACK);
        graphic2d.setStroke(new BasicStroke(1));
        graphic2d.draw(path);
    }
}
