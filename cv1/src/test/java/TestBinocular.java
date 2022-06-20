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
        System.out.println("kMat");
        System.out.println(kMat.dump());
        MatOfDouble dMat = new MatOfDouble(Mat.zeros(4, 1, CvType.CV_64F));

        // target is 0.4m wide, 0.1m high
        double targetWidth = 0.4;
        double targetHeight = 0.1;
        MatOfPoint3f targetGeometryMeters = VisionUtil.makeTargetGeometry3f2(targetWidth,
                targetHeight);
        System.out.println("targetGeometryMeters");
        System.out.println(targetGeometryMeters.dump());

        // camera base at 0,0,-4, straight ahead, in meters, world coords
        // double xPos = 0.0;
        // double yPos = 1.0; // camera is below the target
        double yPos = 0.0;
        // double zPos = -4.0;
        // in radians, camera coords, positive = right
        double tilt = 0.0;
        // double pan = 0.785398; // 45 degrees to the right
        double pan = 0.0;

        double base = 0.5; // 50cm camera separation (wide!)
        Scalar green = new Scalar(0, 255, 0);
        int idx = 0;
        Rect viewport = new Rect(10, 10, width - 20, height - 20);
        System.out.println("idx, xPos, yPos, zPos, tilt, pan, px, py, pz, ptilt, ppan ");
        for (double zPos = -4; zPos <= -4; zPos += 2.0) {
            // negative xpos means camera to the left of the target i.e. world coords
            // for camera, whicih means target should be in positive x territory
            // point: for (double xPos = -4; xPos <= -4; xPos += 2.0) {
            point: for (double xPos = -0; xPos <= -0; xPos += 2.0) {
                ++idx;

                MatOfPoint2f leftPts = VisionUtil.getImagePoints(xPos - base / 2, yPos, zPos, tilt, pan, kMat, dMat,
                        targetGeometryMeters);
                System.out.println("leftPts");
                System.out.println(leftPts.dump());

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
                System.out.println("rightPts");
                System.out.println(rightPts.dump());
                Mat rightImage = Mat.zeros(height, width, CvType.CV_32FC3);
                for (Point pt : rightPts.toList()) {
                    if (!viewport.contains(pt))
                        break point;
                    Imgproc.circle(rightImage, pt, 6, green, 1);
                }
                Imgcodecs.imwrite(String.format("C:\\Users\\joelt\\Desktop\\pics\\stereo-%d-right.png", idx),
                        rightImage);

                Mat camRV = VisionUtil.panTilt(-pan, -tilt);
                Mat camRM = new Mat();
                Calib3d.Rodrigues(camRV, camRM);
                Mat camTV = Mat.zeros(3, 1, CvType.CV_32F);
                camTV.put(0, 0,
                        -xPos, -yPos, -zPos);
                System.out.println(camRM.t().type());
                System.out.println(camTV.type());
                Mat worldTV = new Mat();
                Core.gemm(camRM.t(), camTV, -1.0, new Mat(), 0.0, worldTV);
                Mat targetCameraRt = Mat.zeros(4, 4, CvType.CV_32F);
                targetCameraRt.put(0, 0,
                        camRM.get(0, 0)[0], camRM.get(0, 1)[0], camRM.get(0, 2)[0], worldTV.get(0, 0)[0],
                        camRM.get(1, 0)[0], camRM.get(1, 1)[0], camRM.get(1, 2)[0], worldTV.get(1, 0)[0],
                        camRM.get(2, 0)[0], camRM.get(2, 1)[0], camRM.get(2, 2)[0], worldTV.get(2, 0)[0],
                        0, 0, 0, 1);

                System.out.println("targetCameraRt");
                System.out.println(targetCameraRt.dump());

                // Camera projection matrices are offset but not rotated
                // Mat RtLeft = Mat.zeros(3, 4, CvType.CV_32F);
                // RtLeft.put(0, 0,
                // 1, 0, 0, base/2,
                // 0, 1, 0, 0,
                // 0, 0, 1, 0);
                // Mat Pleft = new Mat();
                // Core.gemm(kMat, RtLeft, 1.0, new Mat(), 0.0, Pleft);

                // Mat Pleft = Mat.zeros(3, 4, CvType.CV_32F);
                // Pleft.put(0, 0,
                // 1, 0, 0, base / 2,
                // 0, 1, 0, 0,
                // 0, 0, 1, 0);

                Mat Pleft = Mat.zeros(3, 4, CvType.CV_32F);
                Pleft.put(0, 0,
                        f, 0, width / 2, base * f / 2,
                        0, f, height / 2, 0,
                        0, 0, 1, 0);

                System.out.println("Pleft");
                System.out.println(Pleft.dump());

                Mat Pright = Mat.zeros(3, 4, CvType.CV_32F);
                Pright.put(0, 0,
                        f, 0, width / 2, -base * f / 2,
                        0, f, height / 2, 0,
                        0, 0, 1, 0);

                // Mat Pright = Mat.zeros(3, 4, CvType.CV_32F);
                // Pright.put(0, 0,
                // 1, 0, 0, -base / 2,
                // 0, 1, 0, 0,
                // 0, 0, 1, 0);

                Mat homogeneousTarget = new Mat();
                Calib3d.convertPointsToHomogeneous(targetGeometryMeters, homogeneousTarget);
                System.out.println("homogeneousTarget");
                System.out.println(homogeneousTarget.reshape(1).dump());

                Mat targetInCamera = new Mat();
                Core.gemm(targetCameraRt, homogeneousTarget.reshape(1).t(), 1.0, new Mat(), 0.0, targetInCamera);
                System.out.println("targetInCamera");
                System.out.println(targetInCamera.t().dump());

                // this is meters i guess
                Mat projectedTargetLeft = new Mat();
                // Core.gemm(Pleft, homogeneousTarget.reshape(1).t(), 1.0, new Mat(), 0.0,
                // projectedTargetLeft);
                Core.gemm(Pleft, targetInCamera, 1.0, new Mat(), 0.0, projectedTargetLeft);
                System.out.println("projectedTargetLeft");
                System.out.println(projectedTargetLeft.t().dump());

                Mat projectedTargetLeftNormal = new Mat();
                Calib3d.convertPointsFromHomogeneous(projectedTargetLeft.t(), projectedTargetLeftNormal);
                System.out.println("projectedTargetLeftNormal");
                System.out.println(projectedTargetLeftNormal.dump());

                Mat projectedTargetRight = new Mat();
                // Core.gemm(Pright, homogeneousTarget.reshape(1).t(), 1.0, new Mat(), 0.0,
                // projectedTargetRight);
                Core.gemm(Pright, targetInCamera, 1.0, new Mat(), 0.0, projectedTargetRight);
                System.out.println("projectedTargetRight");
                System.out.println(projectedTargetRight.t().dump());

                // points in the left camera's rectified coords, one channel
                // pts are pixels
                // P base is in meters
                // so this is all wrong
                //
                Mat predictedHomogeneous = new Mat();
                Calib3d.triangulatePoints(Pleft, Pright, leftPts, rightPts, predictedHomogeneous);
                System.out.println("predictedHomogeneous == what triangulatepoints says");
                System.out.println(predictedHomogeneous.t().dump());
                Mat fixBase = Mat.zeros(4, 4, CvType.CV_32F);

                fixBase.put(0, 0,
                        1, 0, 0, 0,
                        0, 1, 0, 0,
                        0, 0, 1, 0,
                        0, 0, 0, 1);
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

                Mat fullyFixed = new Mat();
                Calib3d.convertPointsFromHomogeneous(fixedT, fullyFixed);
                Calib3d.convertPointsToHomogeneous(fullyFixed, fixedT);
                System.out.println("fixedT == camera view from base center, homogeneous, normalized?");
                System.out.println(fixedT.reshape(1).size());
                System.out.println(fixedT.reshape(1).dump());

                // try dropping Y, since the robot is on the floor and the target height
                // is fixed.
                Mat dropY = Mat.zeros(3, 4, CvType.CV_32F);
                dropY.put(0, 0,
                        1, 0, 0, 0,
                        0, 0, 1, 0,
                        0, 0, 0, 1);
                Mat fixedT2d = new Mat();
                Core.gemm(dropY, fixedT.reshape(1).t(), 1.0, new Mat(), 0.0, fixedT2d);
                System.out.println("fixedT2d");
                System.out.println(fixedT2d.t().type());
                System.out.println(fixedT2d.t().dump());

                Mat homogeneousTarget2d = new Mat();
                Core.gemm(dropY, homogeneousTarget.reshape(1).t(), 1.0, new Mat(), 0.0, homogeneousTarget2d);
                System.out.println("homogeneousTarget2d");
                System.out.println(homogeneousTarget2d.t().type());
                System.out.println(homogeneousTarget2d.t().dump());

                // solves Ax=b. A * camera triangulation = world coords.
                Mat A = new Mat();
                // Core.solve(fixedT.reshape(1), homogeneousTarget.reshape(1), A,
                // Core.DECOMP_SVD);
                Core.solve(fixedT2d.t(), homogeneousTarget2d.t(), A, Core.DECOMP_SVD);

                Mat Atp = A.t(); // why transpose? because it seems to produce the right answer? yuck.
                System.out.println("Atp");
                System.out.println(Atp.dump());

                Mat reproject = new Mat();
                // Core.gemm(Atp, fixedT.reshape(1).t(), 1.0, new Mat(), 0.0, reproject);
                Core.gemm(Atp, fixedT2d, 1.0, new Mat(), 0.0, reproject);
                System.out.println("reproject");
                System.out.println(reproject.t().dump());

                Mat reprojectionError = new Mat();
                Core.subtract(homogeneousTarget2d.t(), reproject.t(), reprojectionError);
                System.out.println("reprojectionError");
                System.out.println(reprojectionError.dump());

                // double scale = Atp.get(3, 3)[0];
                // Mat tvec = Mat.zeros(3, 1, CvType.CV_32F);
                // tvec.put(0, 0,
                // -Atp.get(0, 3)[0] / scale, -Atp.get(1, 3)[0] / scale, -Atp.get(2, 3)[0] /
                // scale);

                double scale = Atp.get(2, 2)[0];

                // i think this is "camera in world"
                // note addition of yPos since we know what it is
                Mat tvec = Mat.zeros(3, 1, CvType.CV_32F);
                tvec.put(0, 0,
                        Atp.get(0, 2)[0] / scale, yPos, Atp.get(1, 2)[0] / scale);
                System.out.println("tvec == translation of camera in world?");
                System.out.println(tvec.dump());

                // Mat rmat = Mat.zeros(3, 3, CvType.CV_32F);
                // rmat.put(0, 0,
                // Atp.get(0, 0)[0] / scale, Atp.get(0, 1)[0] / scale, Atp.get(0, 2)[0] / scale,
                // Atp.get(1, 0)[0] / scale, Atp.get(1, 1)[0] / scale, Atp.get(1, 2)[0] / scale,
                // Atp.get(2, 0)[0] / scale, Atp.get(2, 1)[0] / scale, Atp.get(2, 2)[0] /
                // scale);

                // note addition of y axis here, y axis never moves
                Mat rmat = Mat.zeros(3, 3, CvType.CV_32F);
                rmat.put(0, 0,
                        Atp.get(0, 0)[0] / scale, 0.0, Atp.get(0, 1)[0] / scale,
                        0.0, 1.0, 0.0,
                        Atp.get(1, 0)[0] / scale, 0.0, Atp.get(1, 1)[0] / scale);
                System.out.println("rmat = rotation of camera in world?");
                System.out.println(rmat.dump());

                Mat rvec = new Mat();
                Mat jacobian = new Mat();
                Calib3d.Rodrigues(rmat, rvec, jacobian);
                System.out.println("rvec");
                System.out.println(rvec.dump());
                Mat euler = VisionUtil.rotm2euler(rmat);
                System.out.println("euler");
                System.out.println(euler.dump());

                // double euler = VisionUtil.rotm2euler2d(rmat);
                // System.out.println("euler");
                // System.out.println(euler);

                System.out.printf("%d, %5.3f, %5.3f, %5.3f, %5.3f, %5.3f,"
                        + " %5.3f, %5.3f, %5.3f, %5.3f, %5.3f\n",
                        idx, xPos, yPos, zPos, tilt, pan,
                        tvec.get(0, 0)[0], tvec.get(1, 0)[0], tvec.get(2, 0)[0],
                        euler.get(0, 0)[0], euler.get(1, 0)[0]);
            }
        }

    }

}
