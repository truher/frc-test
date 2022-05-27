package frc.sensors;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import edu.wpi.first.util.sendable.Sendable;
import edu.wpi.first.util.sendable.SendableBuilder;
import edu.wpi.first.wpilibj.I2C;

/**
 * Adafruit 4517 contains this gyro, interfaced with I2C.
 * 
 * The detector is clockwise-negative, so this class inverts it to match
 * the WPI convention and the compass.
 * 
 * Constants come from github.com/stm32duino/LSM6DSOX/lsm6dsox_reg.h
 */
public class LSM6DSOX_I2C implements Sendable {
    // 8-bit addr is 0xd5, so 7-bit is shifted, 0x6A
    private static final byte LSM6DSOX_I2C_ADD_L = (byte) 0xD5;
    private static final byte LSM6DSOX_CTRL2_G = (byte) 0x11;
    // private static final byte LSM6DSOX_OUTX_L_G = (byte) 0x22;
    // private static final byte LSM6DSOX_OUTY_L_G = (byte) 0x24;
    // for now i only care about yaw
    private static final byte LSM6DSOX_OUTZ_L_G = (byte) 0x26;

    // seems like raw is about 50 quiescent, so remove it.
    private static final int kRawOffset = -50;

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
        LSM6DSOX_125dps(0b0000_0010, 4.375),
        LSM6DSOX_250dps(0b0000_0000, 8.75),
        LSM6DSOX_500dps(0b0000_0100, 17.5),
        LSM6DSOX_1000dps(0b0000_1000, 35),
        LSM6DSOX_2000dps(0b0000_1100, 70);

        public final byte value;
        public final double mdps;
        // Mask to erase the relevant bits.
        // (Note the ST code doesn't use a mask, it uses bit fields in a struct.)
        public static final byte mask = (byte) 0b0000_1110;

        private LSM6DSOX_FS_G_T(int value, double mdps) {
            this.value = (byte) value;
            this.mdps = mdps;
        }
    }

    private final I2C m_i2c;
    private LSM6DSOX_FS_G_T m_scale;

    public LSM6DSOX_I2C() {
        m_i2c = new I2C(I2C.Port.kMXP, LSM6DSOX_I2C_ADD_L >>> 1); // shift addr!
        setGyroDataRate(LSM6DSOX_ODR_G_T.LSM6DSOX_GY_ODR_104Hz); // medium speed
        setGyroScale(LSM6DSOX_FS_G_T.LSM6DSOX_250dps); // slowest
    }

    private void setGyroDataRate(LSM6DSOX_ODR_G_T data_rate) {
        ByteBuffer buf = ByteBuffer.allocate(1);
        buf.order(ByteOrder.LITTLE_ENDIAN);
        m_i2c.read(LSM6DSOX_CTRL2_G, 1, buf);
        byte ctrl2 = buf.get();
        ctrl2 &= ~LSM6DSOX_ODR_G_T.mask;
        ctrl2 |= data_rate.value;
        m_i2c.write(LSM6DSOX_CTRL2_G, ctrl2);
    }

    private void setGyroScale(LSM6DSOX_FS_G_T scale) {
        m_scale = scale;
        ByteBuffer buf = ByteBuffer.allocate(1);
        buf.order(ByteOrder.LITTLE_ENDIAN);
        m_i2c.read(LSM6DSOX_CTRL2_G, 1, buf);
        byte ctrl2 = buf.get();
        ctrl2 &= ~LSM6DSOX_FS_G_T.mask;
        ctrl2 |= scale.value;
        m_i2c.write(LSM6DSOX_CTRL2_G, ctrl2);
    }

    /**
     * Yaw rate in radians/sec
     */
    public double getRate() {
        return (double) getYawRateRaw() * m_scale.mdps * Math.PI / 180000;
    }

    /**
     * Yaw rate unit depends on config
     */
    public int getYawRateRaw() {
        ByteBuffer buf = ByteBuffer.allocate(2);
        buf.order(ByteOrder.LITTLE_ENDIAN);
        m_i2c.read(LSM6DSOX_OUTZ_L_G, 2, buf);
        return -1 * (buf.getShort() - kRawOffset);
    }

    @Override
    public void initSendable(SendableBuilder builder) {
        builder.setSmartDashboardType("Gyro");
        builder.addDoubleProperty("yaw rate raw", this::getYawRateRaw, null);
    }

}
