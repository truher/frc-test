package frc.sensors;

import frc.util.EdgeCounter;

public class UnrolledAngle implements Angle {
    private final Angle m_angle;
    private final EdgeCounter m_counter;

    public UnrolledAngle(Angle angle) {
        m_angle = angle;
        m_counter = new EdgeCounter(-0.5 * Math.PI, 0.5 * Math.PI);
    }

    @Override
    public double getAngle() {
        return m_counter.update(m_angle.getAngle()) * 2 * Math.PI + m_angle.getAngle();
    }

}
