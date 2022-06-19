//import java.util.List;

import org.junit.Test;
import org.opencv.calib3d.Calib3d;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfDouble;
import org.opencv.core.MatOfPoint2f;
import org.opencv.core.MatOfPoint3f;
import org.opencv.core.Size;

import vision.VisionUtil;

/**
 * Binocular vision to try to improve distant pose estimation.
 */
public class TestBinocular {
    public TestBinocular() {
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
    }

    @Test
    public void testSimple() {
        int height = 540;
        int width = 960;
        Size dsize = new Size(width, height);
        double f = 512.0;
        Mat kMat = VisionUtil.makeIntrinsicMatrix(f, dsize);
        MatOfDouble dMat = new MatOfDouble(Mat.zeros(4, 1, CvType.CV_64F));

        // target is 0.4m wide, 0.1m high
        double targetWidth = 0.4;
        double targetHeight = 0.1;
        MatOfPoint3f targetGeometryMeters = VisionUtil.makeTargetGeometry3f2(targetWidth,
                targetHeight);

        // camera base at 0,0,-4, straight ahead, in meters, world coords
        // double xPos = 0.0;
        double yPos = 1.0;
        // double zPos = -4.0;
        // in radians
        double tilt = 0.0;
        double pan = 0.0;

        double base = 0.4; // 40cm camera separation (wide!)

        System.out.println("xPos, yPos, zPos, px, py, pz");
        for (double zPos = -10; zPos <= -1; zPos += 1.0) {
            for (double xPos = -4; xPos <= 4; xPos += 1.0) {

                MatOfPoint2f leftPts = VisionUtil.getImagePoints(xPos - base / 2, yPos, zPos, tilt, pan, kMat, dMat,
                        targetGeometryMeters);
                MatOfPoint2f rightPts = VisionUtil.getImagePoints(xPos + base / 2, yPos, zPos, tilt, pan, kMat, dMat,
                        targetGeometryMeters);

                // Camera projection matrices are offset but not rotated
                Mat Pleft = Mat.zeros(3, 4, CvType.CV_32F);
                Pleft.put(0, 0,
                        f, 0, width / 2, 0,
                        0, f, height / 2, 0,
                        0, 0, 1, 0);

                Mat Pright = Mat.zeros(3, 4, CvType.CV_32F);
                Pright.put(0, 0,
                        f, 0, width / 2, base * f,
                        0, f, height / 2, 0,
                        0, 0, 1, 0);

                Mat predictedHomogeneous = new Mat(); // points in the left camera's rectified coords, one channel
                Calib3d.triangulatePoints(Pleft, Pright, leftPts, rightPts, predictedHomogeneous);

                Mat fixBase = Mat.zeros(4, 4, CvType.CV_32F);
                fixBase.put(0, 0,
                        1, 0, 0, base / 2,
                        0, 1, 0, 0,
                        0, 0, 1, 0,
                        0, 0, 0, -1);
                Mat fixed = new Mat(); // translated to base center, homogeneous
                Core.gemm(fixBase, predictedHomogeneous, 1.0, new Mat(), 0.0, fixed);
                Mat fixedT = fixed.t();

                Mat homogeneousTarget = new Mat();
                Calib3d.convertPointsToHomogeneous(targetGeometryMeters, homogeneousTarget);

                Mat A = new Mat();
                Core.solve(homogeneousTarget.reshape(1), fixedT, A, Core.DECOMP_SVD);
                Mat Atp = A.t(); // why transpose? because it seems to produce the right answer? yuck.

                double scale = Atp.get(3, 3)[0];
                Mat tvec = Mat.zeros(3, 1, CvType.CV_32F);
                tvec.put(0, 0,
                        Atp.get(0, 3)[0] / scale, Atp.get(1, 3)[0] / scale, Atp.get(2, 3)[0] / scale);

                Mat rmat = Mat.zeros(3, 3, CvType.CV_32F);
                rmat.put(0, 0,
                        Atp.get(0, 0)[0] / scale, Atp.get(0, 1)[0] / scale, Atp.get(0, 2)[0] / scale,
                        Atp.get(1, 0)[0] / scale, Atp.get(1, 1)[0] / scale, Atp.get(1, 2)[0] / scale,
                        Atp.get(2, 0)[0] / scale, Atp.get(2, 1)[0] / scale, Atp.get(2, 2)[0] / scale);
                Mat rvec = new Mat();
                Mat jacobian = new Mat();
                Calib3d.Rodrigues(rmat, rvec, jacobian);

                System.out.printf("%5.3f, %5.3f, %5.3f, %5.3f, %5.3f, %5.3f\n", xPos, yPos, zPos,
                        -tvec.get(0, 0)[0], -tvec.get(1, 0)[0], -tvec.get(2, 0)[0]);
            }
        }

    }

}
