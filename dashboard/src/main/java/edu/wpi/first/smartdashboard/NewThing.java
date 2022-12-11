package edu.wpi.first.smartdashboard;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;

import javax.swing.JFrame;
import javax.swing.JPanel;

public class NewThing extends JPanel {
    private static final int BOX_WIDTH = 50;
    private static final int BOX_HEIGHT = 50;
    private static final int WINDOW_HEIGHT = 500;
    private static final int WINDOW_WIDTH = 1000;

    public NewThing() {
        JFrame frame = new JFrame("demo");
        frame.add(this);
        frame.setSize(WINDOW_WIDTH, WINDOW_HEIGHT);
        frame.setVisible(true);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    }

    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D graphic2d = (Graphics2D) g;
        graphic2d.setColor(Color.BLUE);
        graphic2d.setStroke(new BasicStroke(3));
        graphic2d.setFont(new Font("Helvetica", Font.PLAIN, 18));
        FontMetrics fm = graphic2d.getFontMetrics();
        int x = 200; // t.pose.getX()
        int y = 100; // t.pose.getY()

        graphic2d.drawRect(WINDOW_WIDTH / 2 + (x - BOX_WIDTH / 2),
                WINDOW_HEIGHT / 2 + (y - BOX_HEIGHT / 2), BOX_WIDTH, BOX_HEIGHT);
        String label = "arg";
        graphic2d.drawString(label,
                WINDOW_WIDTH / 2 + x - fm.stringWidth(label) / 2,
                WINDOW_HEIGHT / 2 + y + fm.getAscent() / 2);

    }

    public void run() {
        // do nothing
    }

}
