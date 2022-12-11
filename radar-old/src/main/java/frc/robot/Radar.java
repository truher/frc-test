package frc.robot;

import javax.swing.JPanel;

/**
 * Target visualization example.
 * 
 * There's a data class representing a target pose, and another class
 * representing a collection of poses.
 * 
 * The publisher has a list of fake poses and publishes it using
 * MessagePack, mutating it periodically.
 * 
 * The subscriber listens for updates and renders them visually.
 */
public class Radar extends JPanel {
  public static void main(String... args) {
    TargetSubscriber r = new TargetSubscriber();
    r.run();
    new TargetPublisher().run();
  }
}
