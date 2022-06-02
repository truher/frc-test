import static org.junit.Assert.assertEquals;

import org.junit.Test;

import edu.wpi.first.math.Matrix;
import edu.wpi.first.math.VecBuilder;
import edu.wpi.first.math.Vector;
import edu.wpi.first.math.geometry.Pose2d;
import edu.wpi.first.math.geometry.Rotation2d;
import edu.wpi.first.math.geometry.Translation2d;
import edu.wpi.first.math.numbers.N1;
import edu.wpi.first.math.numbers.N2;
import frc.util.Vectors;

public class VectorTest {
    private static final double DELTA = 0.01;

    /**
     * so there's vector2d and translation2d but gah
     */
    @Test
    public void testVectors() {
        Vector<N2> v = VecBuilder.fill(2, 2);
        assertEquals(8.0, v.transpose().times(v).get(0, 0), DELTA);
        Vector<N2> v2 = VecBuilder.fill(3, 1);
        assertEquals(-1, v.minus(v2).get(0, 0), DELTA);
        assertEquals(1, v.minus(v2).get(1, 0), DELTA);
        assertEquals(2.83, Vectors.norm(v), DELTA);
    }

    @Test
    public void testFunction() {
        Translation2d aimingPoint = new Translation2d(0, 6);
        Pose2d pose = new Pose2d(2, 2, new Rotation2d(1, 2));
        double speed = Math.sqrt(5);
        assertEquals(1.78, Vectors.tangentialSpeedMetersPerSec(aimingPoint, pose, speed), DELTA);
    }

    @Test
    public void testProjection() {
        Vector<N2> robotPosition = VecBuilder.fill(2, 2);
        assertEquals(2.83, Vectors.norm(robotPosition), DELTA); // sqrt(2*2+2*2)

        Vector<N2> robotVelocity = VecBuilder.fill(1, 2);
        assertEquals(2.24, Vectors.norm(robotVelocity), DELTA); // sqrt(2*2+1*1)

        Vector<N2> targetPosition = VecBuilder.fill(0, 6);
        assertEquals(6, Vectors.norm(targetPosition), DELTA); // sqrt(0*0+6*6)

        Matrix<N2, N1> targetRelative = targetPosition.minus(robotPosition);
        assertEquals(-2, targetRelative.get(0, 0), DELTA);
        assertEquals(4, targetRelative.get(1, 0), DELTA);

        // unit
        Matrix<N2, N1> targetDirection = targetRelative.div(Vectors.norm(targetRelative));
        assertEquals(-0.44, targetDirection.get(0, 0), DELTA);
        assertEquals(0.89, targetDirection.get(1, 0), DELTA);

        double radialComponent = robotVelocity.transpose().times(targetDirection).get(0, 0);
        assertEquals(1.34, radialComponent, DELTA);

        Matrix<N2, N1> radialVelocity = targetDirection.times(radialComponent);
        assertEquals(-0.6, radialVelocity.get(0, 0), DELTA);
        assertEquals(1.2, radialVelocity.get(1, 0), DELTA);

        Matrix<N2, N1> tangentialVelocity = robotVelocity.minus(radialVelocity);
        assertEquals(1.6, tangentialVelocity.get(0, 0), DELTA);
        assertEquals(0.8, tangentialVelocity.get(1, 0), DELTA);

        assertEquals(1.78, Vectors.norm(tangentialVelocity), DELTA);

    }

}
