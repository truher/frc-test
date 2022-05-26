package frc.sensors;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import edu.wpi.first.math.geometry.Rotation2d;
import edu.wpi.first.util.sendable.Sendable;
import edu.wpi.first.util.sendable.SendableBuilder;
import edu.wpi.first.wpilibj.I2C;
import edu.wpi.first.wpilibj.RobotController;
import edu.wpi.first.wpilibj.interfaces.Gyro;

/**
 * Adafruit 4517 contains this gyro, interfaced with I2C.
 * 
 * Constants come from github.com/stm32duino/LSM6DSOX/lsm6dsox_reg.h
 */
public class LSM6DSOX_I2C implements Gyro, Sendable {
    // 8-bit addr is 0xd5, so 7-bit is shifted, 0x6A
    private static final byte LSM6DSOX_I2C_ADD_L = (byte) 0xD5;
    private static final byte LSM6DSOX_CTRL2_G = (byte) 0x11;
    // private static final byte LSM6DSOX_OUTX_L_G = (byte) 0x22;
    // private static final byte LSM6DSOX_OUTY_L_G = (byte) 0x24;
    // for now i only care about yaw
    private static final byte LSM6DSOX_OUTZ_L_G = (byte) 0x26;

    private Rotation2d m_angle;

    // Output Data Rate
    // these are the high 4 bits in CTRL2_G
    public enum LSM6DSOX_ODR_G_T {
        LSM6DSOX_GY_ODR_OFF(0b0000_0000),
        LSM6DSOX_GY_ODR_12Hz5(0b0001_0000),
        LSM6DSOX_GY_ODR_26Hz(0b0010_0000),
        LSM6DSOX_GY_ODR_52Hz(0b0011_0000),
        LSM6DSOX_GY_ODR_104Hz(0b0100_0000),
        LSM6DSOX_GY_ODR_208Hz(0b0101_0000),
        LSM6DSOX_GY_ODR_417Hz(0b0110_0000),
        LSM6DSOX_GY_ODR_833Hz(0b0111_0000),
        LSM6DSOX_GY_ODR_1667Hz(0b1000_0000),
        LSM6DSOX_GY_ODR_3333Hz(0b1001_0000),
        LSM6DSOX_GY_ODR_6667Hz(0b1010_0000);

        // the correctly bit-offset value
        public final byte value;
        // Mask to erase the relevant bits.
        // (Note the ST code doesn't use a mask, it uses bit fields in a struct.)
        public static final byte mask = (byte) 0b1111_0000;

        private LSM6DSOX_ODR_G_T(int value) {
            this.value = (byte) value;
        }
    }

    // Sensitivity
    // these are bits 1-3 in CTRL2_G
    public enum LSM6DSOX_FS_G_T {
        LSM6DSOX_125dps(0b0000_0010),
        LSM6DSOX_250dps(0b0000_0000),
        LSM6DSOX_500dps(0b0000_0100),
        LSM6DSOX_1000dps(0b0000_1000),
        LSM6DSOX_2000dps(0b0000_1100);

        public final byte value;
        // Mask to erase the relevant bits.
        // (Note the ST code doesn't use a mask, it uses bit fields in a struct.)
        public static final byte mask = (byte) 0b0000_1110;

        private LSM6DSOX_FS_G_T(int value) {
            this.value = (byte) value;
        }
    }

    private long m_prevTimeUs;
    private final I2C m_i2c;

    public LSM6DSOX_I2C() {
        m_prevTimeUs =  = RobotController.getFPGATime();
        m_i2c = new I2C(I2C.Port.kMXP, LSM6DSOX_I2C_ADD_L >>> 1); // shift addr!
        setGyroDataRate(LSM6DSOX_ODR_G_T.LSM6DSOX_GY_ODR_104Hz); // medium speed
        setGyroScale(LSM6DSOX_FS_G_T.LSM6DSOX_2000dps); // fastest
    }

    private void setGyroDataRate(LSM6DSOX_ODR_G_T data_rate) {
        // TODO: make this a member
        ByteBuffer buf = ByteBuffer.allocate(1);
        buf.order(ByteOrder.LITTLE_ENDIAN); // TODO: check
        m_i2c.read(LSM6DSOX_CTRL2_G, 1, buf);
        byte ctrl2 = buf.get();
        ctrl2 &= ~LSM6DSOX_ODR_G_T.mask;
        ctrl2 |= data_rate.value;
        m_i2c.write(LSM6DSOX_CTRL2_G, ctrl2);
    }

    // TODO: extract some common parts of these setters
    private void setGyroScale(LSM6DSOX_FS_G_T scale) {
        ByteBuffer buf = ByteBuffer.allocate(1);
        buf.order(ByteOrder.LITTLE_ENDIAN); // TODO: check
        m_i2c.read(LSM6DSOX_CTRL2_G, 1, buf);
        byte ctrl2 = buf.get();
        ctrl2 &= ~LSM6DSOX_FS_G_T.mask;
        ctrl2 |= scale.value;
        m_i2c.write(LSM6DSOX_CTRL2_G, ctrl2);
    }

    // call this often if you want it to be right
    // TODO: do it differently somehow?
    @Override
    public double getAngle() {
        long timeUs = RobotController.getFPGATime();
        long timeDiffUs = timeUs - m_prevTimeUs;
        m_prevTimeUs = timeUs;
        double degPerSec = getYawRateRaw()  * 0.00058687;
        double angleIncrement = degPerSec * timeDiffUs / 1e6;
        m_angle = m_angle.plus(new Rotation2d(angleIncrement));
        return m_angle.getRadians();
    }

    @Override
    public void close() throws Exception {
        m_i2c.close();
    }

    @Override
    public void calibrate() {
        // TODO Auto-generated method stub

    }

    @Override
    public void reset() {
        // TODO Auto-generated method stub

    }

    // degrees per second
    @Override
    public double getRate() {
        // at 2000dps full scale, 104 hz update rate means
        // 19.231 degrees per update full scale, 16 bits signed
        // means 0.00058687 dps per unit
        // TODO: use the config for this
        return (double) getYawRateRaw()  * 0.00058687;
    }

    // unit depends on config
    private short getYawRateRaw() {
        ByteBuffer data = ByteBuffer.allocate(2);
        m_i2c.read(LSM6DSOX_OUTZ_L_G, 2, data);
        return data.getShort();
    }

    @Override
    public void initSendable(SendableBuilder builder) {
        builder.setSmartDashboardType("Gyro");
        builder.addDoubleProperty("Value", this::getAngle, null);
    }

}
