
package frc.robot;

import edu.wpi.first.util.sendable.Sendable;
import edu.wpi.first.util.sendable.SendableBuilder;
import edu.wpi.first.wpilibj.PWM;
import edu.wpi.first.wpilibj.Servo;
import edu.wpi.first.wpilibj.TimedRobot;
import edu.wpi.first.wpilibj.XboxController;
import edu.wpi.first.wpilibj.motorcontrol.PWMMotorController;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;

public class Robot extends TimedRobot implements Sendable {

    // LATER: put this stuff in like a subsystem or command or whatever
    // FEED
    private static final int FEED_DURATION = 7;
    private static final double FEED_SPEED = 1.0;

    private class Feed extends PWMMotorController {
        protected Feed(String name, int channel) {
            super(name, channel);
            m_pwm.setBounds(1.72, 1.52, 1.5, 1.48, 1.28); // parallax360
            m_pwm.setPeriodMultiplier(PWM.PeriodMultiplier.k4X);
            m_pwm.setSpeed(0.0);
            m_pwm.setZeroLatch();
        }
    }

    private final Feed m_feed = new Feed("feed", 7);
    boolean m_advance_feed = false;

    private enum Feedstate {
        OFF, REVERSING, ADVANCING
    }

    private int m_state_counter = 0;
    private Feedstate m_feedstate = Feedstate.OFF;

    // ELEVATION
    private static final double ELEVATION_SPEED = 0.01;
    int m_elevation_up_down = -1;
    private final Servo m_elevation = new Servo(6);
    // LATER: calibrate elevation
    private double m_current_elevation = 0.5;

    // WHEELS
    private static final int WHEEL_FULL = 4095;

    private class Wheel extends PWMMotorController {
        protected Wheel(String name, int channel) {
            super(name, channel);
            // m_pwm.setBounds(5.0, 0.0, 0.0, 0.0, 0.0);
            m_pwm.setPeriodMultiplier(PWM.PeriodMultiplier.k1X);
            // m_pwm.setSpeed(0.0);
            m_pwm.setRaw(0);
            // m_pwm.setZeroLatch();
        }

        public void setRaw(int value) {
            m_pwm.setRaw(value);
            feed();
        }

        public int getRaw() {
            return m_pwm.getRaw();
        }
    }

    double m_wheel_speed = 0;
    private final Wheel m_wheel1 = new Wheel("wheel1", 8) {
    };
    private final Wheel m_wheel2 = new Wheel("wheel2", 9) {
    };

    private final XboxController m_controller = new XboxController(0);

    public Robot() {
        SmartDashboard.putData("blarg", this);
    }

    @Override
    public void robotInit() {
    }

    @Override
    public void teleopInit() {
        m_feedstate = Feedstate.REVERSING; // back up a bit to reset the ball position
        m_state_counter = 0;
    }

    @Override
    public void teleopPeriodic() {
        // FEED
        m_advance_feed = m_controller.getLeftBumper(); // advance feed
        if (m_advance_feed) {
            m_state_counter = 0;
            switch (m_feedstate) {
                case OFF:
                    m_feedstate = Feedstate.ADVANCING;
                    break;
                case REVERSING:
                case ADVANCING:
                default:
                    // m_feedstate = Feedstate.OFF;
            }
        }
        if (m_state_counter > FEED_DURATION) { // stop after 1s
            m_state_counter = 0;
            switch (m_feedstate) {
                case OFF:
                    break;
                case REVERSING:
                    m_feedstate = Feedstate.OFF;
                    break;
                case ADVANCING:
                    m_feedstate = Feedstate.REVERSING; // reset position
                    break;
                default:
                    m_feedstate = Feedstate.OFF;
            }

        }
        switch (m_feedstate) {
            case OFF:
                m_feed.set(0.0);
                break;
            case REVERSING:
                m_feed.set(-FEED_SPEED);
                break;
            case ADVANCING:
                m_feed.set(FEED_SPEED);
                break;
            default:
                // ?
        }
        m_state_counter += 1;

        // ELEVATION
        m_elevation_up_down = m_controller.getPOV(); // elevation up/down
        if (m_elevation_up_down == 0) { // push = down
            m_current_elevation -= ELEVATION_SPEED;
        } else if (m_elevation_up_down == 180) { // pull = up
            m_current_elevation += ELEVATION_SPEED;
        }
        m_elevation.set(m_current_elevation);

        // WHEELS
        m_wheel_speed = m_controller.getLeftTriggerAxis(); // wheel speed
        m_wheel1.setRaw((int) (m_wheel_speed * WHEEL_FULL));
        m_wheel2.setRaw((int) (m_wheel_speed * WHEEL_FULL));
    }

    @Override
    public void initSendable(SendableBuilder builder) {
        builder.setSmartDashboardType("robot");

        // FEED
        builder.addBooleanProperty("m_advance_feed", () -> m_advance_feed, null);
        builder.addDoubleProperty("m_state_counter", () -> m_state_counter, null);
        builder.addStringProperty("m_feedstate", () -> m_feedstate.name(), null);
        builder.addDoubleProperty("m_feed", () -> m_feed.get(), null);

        // ELEVATIONS
        builder.addDoubleProperty("m_elevation_up_down", () -> m_elevation_up_down, null);
        builder.addDoubleProperty("m_current_elevation", () -> m_current_elevation, null);
        builder.addDoubleProperty("m_elevation", () -> m_elevation.get(), null);

        // WHEELS
        builder.addDoubleProperty("m_wheel_speed", () -> m_wheel_speed, null);
        builder.addDoubleProperty("m_wheel1", () -> m_wheel1.get(), null);
        builder.addDoubleProperty("m_wheel2", () -> m_wheel2.get(), null);
        builder.addDoubleProperty("m_wheel1 raw", () -> m_wheel1.getRaw(), null);
        builder.addDoubleProperty("m_wheel2 raw", () -> m_wheel2.getRaw(), null);

    }
}