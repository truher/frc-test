package frc.sensors;

import edu.wpi.first.math.Matrix;
import edu.wpi.first.math.Nat;
import edu.wpi.first.math.estimator.KalmanFilter;
import edu.wpi.first.math.numbers.N1;
import edu.wpi.first.math.numbers.N2;
import edu.wpi.first.math.system.LinearSystem;
import edu.wpi.first.util.sendable.Sendable;
import edu.wpi.first.util.sendable.SendableBuilder;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;

/**
 * Combine mag and gyro with Kalman filter.
 */
public class FusedHeading implements Angle, Sendable {
    private static final double kDtSec = 0.02;
    // state is [position, velocity]
    private static final Matrix<N2, N2> kA = Matrix.mat(Nat.N2(), Nat.N2()).fill(0, 1, 0, 0);
    // input is torque
    private static final Matrix<N2, N1> kB = Matrix.mat(Nat.N2(), Nat.N1()).fill(0, 1);
    // output is [position, velocity] as measured by mag, gyro
    private static final Matrix<N2, N2> kC = Matrix.mat(Nat.N2(), Nat.N2()).fill(1, 0, 0, 1);
    private static final Matrix<N2, N1> kD = Matrix.mat(Nat.N2(), Nat.N1()).fill(0, 0);
    // system state is pretty firm
    private static final Matrix<N2, N1> kStateStdDevs = Matrix.mat(Nat.N2(), Nat.N1()).fill(0.1, 0.1);
    // observations are kinda iffy
    private static final Matrix<N2, N1> kOutputStdDevs = Matrix.mat(Nat.N2(), Nat.N1()).fill(1, 1);
    // for now there is no input
    private static final Matrix<N1, N1> kControlInput = Matrix.mat(Nat.N1(), Nat.N1()).fill(0);


    private final LIS3MDL_I2C m_magnetometer;
    private final UnrolledAngle m_unroller;
    private final LSM6DSOX_I2C m_gyro;
    // 2 states, 1 input, 2 outputs
    private final LinearSystem<N2, N1, N2> m_system;
    private final KalmanFilter<N2, N1, N2> m_filter;

    public FusedHeading() {
        m_magnetometer = new LIS3MDL_I2C();
        m_unroller = new UnrolledAngle(m_magnetometer);
        m_gyro = new LSM6DSOX_I2C();
        m_system = new LinearSystem<N2, N1, N2>(kA, kB, kC, kD);
        m_filter = new KalmanFilter<N2, N1, N2>(Nat.N2(), Nat.N2(), m_system, kStateStdDevs, kOutputStdDevs, kDtSec);
        SmartDashboard.putData("heading", this);
    }

    public Matrix<N2, N1> getObservations() {
        return Matrix.mat(Nat.N2(), Nat.N1()).fill(m_unroller.getAngle(), m_gyro.getRate());
    }

    public void reset() {
        m_filter.setXhat(getObservations());
    }

    @Override
    public double getAngle() {
        m_filter.predict(kControlInput, kDtSec);
        m_filter.correct(kControlInput, getObservations());
        return m_filter.getXhat().get(0,0);
    }

    @Override
    public void initSendable(SendableBuilder builder) {
        builder.addDoubleProperty("heading", this::getAngle, null);
        builder.addDoubleProperty("mag", () -> m_magnetometer.getAngle(), null);
        builder.addDoubleProperty("unrolled", () -> m_unroller.getAngle(), null);
        builder.addDoubleProperty("gyro rate", () -> m_gyro.getRate(), null);
        builder.addDoubleProperty("gyro yaw rate raw", () -> m_gyro.getYawRateRaw(), null);

    }

}
