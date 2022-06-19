//import java.util.List;

import org.junit.Test;
import org.opencv.calib3d.Calib3d;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfDouble;
import org.opencv.core.MatOfPoint2f;
import org.opencv.core.MatOfPoint3f;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

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
        double pan = -0.1;

        double base = 0.4; // 40cm camera separation (wide!)
        Scalar green = new Scalar(0, 255, 0);
        int idx = 0;
        Rect viewport = new Rect(10, 10, width - 20, height - 20);
        System.out.println("idx, xPos, yPos, zPos, tilt, pan, px, py, pz, ptilt, ppan");
        for (double zPos = -10; zPos <= -10; zPos += 2.0) {
            // negative xpos means camera to the left of the target i.e. world coords
            // for camera, whicih means target should be in positive x territory
            point: for (double xPos = -4; xPos <= -4; xPos += 2.0) {
                ++idx;

                MatOfPoint2f leftPts = VisionUtil.getImagePoints(xPos - base / 2, yPos, zPos, tilt, pan, kMat, dMat,
                        targetGeometryMeters);

                Mat leftImage = Mat.zeros(height, width, CvType.CV_32FC3);
                for (Point pt : leftPts.toList()) {
                    if (!viewport.contains(pt))
                        break point;
                    Imgproc.circle(leftImage, pt, 6, green, 1);
                }
                Imgcodecs.imwrite(String.format("C:\\Users\\joelt\\Desktop\\pics\\stereo-%d-left.png", idx),
                        leftImage);

                MatOfPoint2f rightPts = VisionUtil.getImagePoints(xPos + base / 2, yPos, zPos, tilt, pan, kMat, dMat,
                        targetGeometryMeters);
                Mat rightImage = Mat.zeros(height, width, CvType.CV_32FC3);
                for (Point pt : rightPts.toList()) {
                    if (!viewport.contains(pt))
                        break point;
                    Imgproc.circle(rightImage, pt, 6, green, 1);
                }
                Imgcodecs.imwrite(String.format("C:\\Users\\joelt\\Desktop\\pics\\stereo-%d-right.png", idx),
                        rightImage);

                // Camera projection matrices are offset but not rotated
                Mat Pleft = Mat.zeros(3, 4, CvType.CV_32F);

                Pleft.put(0, 0,
                        f, 0, width / 2, -base * f / 2,
                        0, f, height / 2, 0,
                        0, 0, 1, 0);

                Mat Pright = Mat.zeros(3, 4, CvType.CV_32F);

                Pright.put(0, 0,
                        f, 0, width / 2, base * f / 2,
                        0, f, height / 2, 0,
                        0, 0, 1, 0);

                // points in the left camera's rectified coords, one channel
                Mat predictedHomogeneous = new Mat();
                Calib3d.triangulatePoints(Pleft, Pright, leftPts, rightPts, predictedHomogeneous);

                Mat fixBase = Mat.zeros(4, 4, CvType.CV_32F);

                fixBase.put(0, 0,
                        1, 0, 0, 0,
                        0, 1, 0, 0,
                        0, 0, 1, 0,
                        0, 0, 0, -1);
                // translated to base center, homogeneous
                Mat fixed = new Mat();
                Core.gemm(fixBase, predictedHomogeneous, 1.0, new Mat(), 0.0, fixed);
                Mat fixedT = fixed.t();
                System.out.println("fixedT == camera view from base center, homogeneous");
                System.out.println(fixedT.dump());
                //
                Mat fixedTNormal = new Mat();
                Calib3d.convertPointsFromHomogeneous(fixedT, fixedTNormal);
                System.out.println("fixedTNormal == target points in camera coords, just to see");
                System.out.println(fixedTNormal.dump());

                Mat homogeneousTarget = new Mat();
                Calib3d.convertPointsToHomogeneous(targetGeometryMeters, homogeneousTarget);
                System.out.println("target == world coords");
                System.out.println(homogeneousTarget.reshape(1).dump());
                Mat fullyFixed = new Mat();
                Calib3d.convertPointsFromHomogeneous(fixedT, fullyFixed);
                Calib3d.convertPointsToHomogeneous(fullyFixed, fixedT);
                System.out.println("fixedT == camera view from base center, homogeneous, normalized?");
                System.out.println(fixedT.dump());

                // solves Ax=b. A * camera triangulation = world coords.
                Mat A = new Mat();
                Core.solve(fixedT.reshape(1), homogeneousTarget.reshape(1), A, Core.DECOMP_SVD);

                Mat Atp = A.t(); // why transpose? because it seems to produce the right answer? yuck.
                System.out.println("Atp");
                System.out.println(Atp.dump());
                Mat reproject = new Mat();

                Core.gemm(Atp, fixedT.reshape(1).t(), 1.0, new Mat(), 0.0, reproject);

                System.out.println("reproject");
                System.out.println(reproject.t().dump());
                double scale = Atp.get(3, 3)[0];
                Mat tvec = Mat.zeros(3, 1, CvType.CV_32F);
                tvec.put(0, 0,
                        -Atp.get(0, 3)[0] / scale, -Atp.get(1, 3)[0] / scale, -Atp.get(2, 3)[0] / scale);
                System.out.println("tvec == translation");
                System.out.println(tvec.dump());
                Mat rmat = Mat.zeros(3, 3, CvType.CV_32F);
                rmat.put(0, 0,
                        Atp.get(0, 0)[0] / scale, Atp.get(0, 1)[0] / scale, Atp.get(0, 2)[0] / scale,
                        Atp.get(1, 0)[0] / scale, Atp.get(1, 1)[0] / scale, Atp.get(1, 2)[0] / scale,
                        Atp.get(2, 0)[0] / scale, Atp.get(2, 1)[0] / scale, Atp.get(2, 2)[0] / scale);

                System.out.println("rmat");
                System.out.println(rmat.dump());
                // Mat rvec = new Mat();
                // Mat jacobian = new Mat();
                // Calib3d.Rodrigues(rmat, rvec, jacobian);
                Mat euler = VisionUtil.rotm2euler(rmat);
                System.out.println("euler");
                System.out.println(euler.dump());

                System.out.printf("%d, %5.3f, %5.3f, %5.3f, %5.3f, %5.3f,"
                        + " %5.3f, %5.3f, %5.3f, %5.3f, %5.3f\n",
                        idx, xPos, yPos, zPos, tilt, pan,
                        -tvec.get(0, 0)[0], -tvec.get(1, 0)[0], -tvec.get(2, 0)[0],
                        euler.get(0, 0)[0], euler.get(1, 0)[0]);
            }
        }

    }

}
