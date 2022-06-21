//import java.util.List;

import static org.junit.Assert.assertEquals;

import org.junit.Test;
import org.opencv.calib3d.Calib3d;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfDouble;
import org.opencv.core.MatOfPoint2f;
import org.opencv.core.MatOfPoint3f;
import org.opencv.core.Point;
import org.opencv.core.Point3;
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
    public final double DELTA = 0.001;

    public TestBinocular() {
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
    }

    // @Test
    public void testSimple() {
        final int height = 540;
        final int width = 960;
        Size dsize = new Size(width, height);
        final double f = 256.0;
        Mat kMat = VisionUtil.makeIntrinsicMatrix(f, dsize);
        System.out.println("kMat");
        System.out.println(kMat.dump());
        MatOfDouble dMat = new MatOfDouble(Mat.zeros(4, 1, CvType.CV_64F));

        // target is 0.4m wide, 0.1m high
        final double targetWidth = 0.4;
        final double targetHeight = 0.1;
        MatOfPoint3f targetGeometryMeters = VisionUtil.makeTargetGeometry3f2(targetWidth,
                targetHeight);
        System.out.println("targetGeometryMeters");
        System.out.println(targetGeometryMeters.dump());

        // camera base at 0,0,-4, straight ahead, in meters, world coords
        // double xPos = 0.0;
        // double yPos = 1.0; // camera is below the target
        double yPos = 0.0;
        // double zPos = -0.02;
        // in radians, camera coords, positive = right
        double tilt = 0.0;
        double pan = 0.785398; // 45 degrees to the right
        // double pan = 2 * 0.785398; // 45 degrees to the right

        // double pan = 0.0;

        final double base = 0.4; // camera separation (wide!)
        Scalar green = new Scalar(0, 255, 0);
        int idx = 0;
        // Rect viewport = new Rect(10, 10, width - 20, height - 20);
        System.out.println("idx, xPos, yPos, zPos, tilt, pan, px, py, pz, ptilt, ppan ");
        for (double zPos = -1; zPos <= -1; zPos += 2.0) {
            // negative xpos means camera to the left of the target i.e. world coords
            // for camera, whicih means target should be in positive x territory
            // point: for (double xPos = -4; xPos <= -4; xPos += 2.0) {
            point: for (double xPos = -1; xPos <= -1; xPos += 2.0) {
                ++idx;

                // for now just put it at the center
                // target is 0.4 wide, so try:
                // point at the center from -1,-1 and -0.5,-0.5 etc.
                // point at one end from -1.2, -1 etc.
                // ah, this part is wrong. the x value is wrong; the base needs to *rotate*,
                // not just move over on the (world) x axis.
                // START HERE
                MatOfPoint2f leftPts = VisionUtil.getImagePoints(-0.3 - base / 2, 0, -0.3, tilt, 0.7854, kMat, dMat,
                        targetGeometryMeters);
                // MatOfPoint2f leftPts = VisionUtil.getImagePoints(xPos - base / 2, yPos, zPos,
                // tilt, pan, kMat, dMat,
                // targetGeometryMeters);
                System.out.println("leftPts");
                System.out.println(leftPts.dump());

                Mat leftImage = Mat.zeros(height, width, CvType.CV_32FC3);
                for (Point pt : leftPts.toList()) {
                    // if (!viewport.contains(pt))
                    // break point;
                    Imgproc.circle(leftImage, pt, 6, green, 1);
                }
                Imgcodecs.imwrite(String.format("C:\\Users\\joelt\\Desktop\\pics\\stereo-%d-left.png", idx),
                        leftImage);

                if (xPos > -20)
                    break point;

                MatOfPoint2f rightPts = VisionUtil.getImagePoints(xPos + base / 2, yPos, zPos, tilt, pan, kMat, dMat,
                        targetGeometryMeters);
                System.out.println("rightPts");
                System.out.println(rightPts.dump());
                Mat rightImage = Mat.zeros(height, width, CvType.CV_32FC3);
                for (Point pt : rightPts.toList()) {
                    // if (!viewport.contains(pt))
                    // break point;
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
                        f, 0, width / 2, -base * f / 2,
                        0, f, height / 2, 0,
                        0, 0, 1, 0);

                System.out.println("Pleft");
                System.out.println(Pleft.dump());

                Mat Pright = Mat.zeros(3, 4, CvType.CV_32F);
                Pright.put(0, 0,
                        f, 0, width / 2, base * f / 2,
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

    /**
     * get the projections to right and left correct, so the test above will work.
     * the new way
     * is to move the target points so they are relative to the camera center, and
     * then
     * tell the "project points" thing to just do the base translation for each eye,
     * without
     * rotation etc.
     */
    // @Test
    public void testProjection() {
        final int height = 540;
        final int width = 960;
        Size dsize = new Size(width, height);
        final double f = 256.0;
        Mat kMat = VisionUtil.makeIntrinsicMatrix(f, dsize);
        System.out.println("kMat");
        System.out.println(kMat.dump());
        MatOfDouble dMat = new MatOfDouble(Mat.zeros(4, 1, CvType.CV_64F));
        System.out.println("dMat");
        System.out.println(dMat.dump());

        // this is camera->world transformation
        // this is location of the camera in world coordinates
        // i.e. applying to a point in camera frame yields the point in world frame.
        // e.g. for t=(0,0,-2) applied to p=(0,0,1) = (0,0,-1)
        // i.e. z is negative, camera "behind"
        double xPos = 0;
        double yPos = 0;
        double zPos = -3;
        Mat worldTVec = Mat.zeros(3, 1, CvType.CV_32F);
        worldTVec.put(0, 0, xPos, yPos, zPos);
        System.out.println("worldTVec");
        System.out.println(worldTVec.dump());

        // this is camera->world transformation
        // this is rotation of the camera in world coordinates
        // i.e. applying to a point in camera frame yields the point in world frame
        // e.g. for r=45 around y, p=(0,0,1) yields (sqrt2/2,0,sqrt2/2)
        // no tilt for now.
        // previously i had used negative pan because i had the world and cam rotations
        // backwards.
        double pan = 0.7854;
        Mat worldRV = Mat.zeros(3, 1, CvType.CV_32F);
        worldRV.put(0, 0, 0.0, pan, 0.0);
        System.out.println("worldRV");
        System.out.println(worldRV.dump());

        Mat worldRMat = new Mat();
        Calib3d.Rodrigues(worldRV, worldRMat);

        Mat camRMat = worldRMat.t();
        Mat camRV = new Mat();
        Calib3d.Rodrigues(camRMat, camRV);
        System.out.println("camRV");
        System.out.println(camRV.dump());

        // this is inverse(worldT*worldR)
        // inverse of multiplication is order-reversed multipication of inverses, so
        // which is worldR.t * -worldT or camR*-worldT
        Mat camTVec = new Mat();
        Core.gemm(camRMat, worldTVec, -1.0, new Mat(), 0, camTVec);
        System.out.println("camTVec");
        System.out.println(camTVec.dump());

        // so this is how the origin moves
        // worldTVec = (0,0,-3)
        // worldRVec = (0, pi/4, 0) = (0, 0.7854, 0)
        // camRVec = (0, -pi/4, 0) = (0, -0.7854, 0)
        // camTVec = (-3sqrt2/2, 0, 3sqrt2/2) = (-2.12, 0, 2.12)

        // so for the point (1,0,0) in world frame, it should end up at

        // so the final (homogeneous) transform from world to camera
        Mat camTransform = Mat.zeros(4, 4, CvType.CV_32F);
        camTransform.put(0, 0,
                camRMat.get(0, 0)[0], camRMat.get(0, 1)[0], camRMat.get(0, 2)[0], camTVec.get(0, 0)[0],
                camRMat.get(1, 0)[0], camRMat.get(1, 1)[0], camRMat.get(1, 2)[0], camTVec.get(1, 0)[0],
                camRMat.get(2, 0)[0], camRMat.get(2, 1)[0], camRMat.get(2, 2)[0], camTVec.get(2, 0)[0],
                0, 0, 0, 1);
        System.out.println("camTransform");
        System.out.println(camTransform.dump());

        // example: (0,0,0) -> (-3sqrt(2)/2, 0, 3sqrt(2)/2)
        {
            Mat worldPoint = Mat.zeros(4, 1, CvType.CV_32F);
            worldPoint.put(0, 0,
                    0, 0, 0, 1);
            System.out.println("point in world frame");
            System.out.println(worldPoint.dump());
            Mat transformedPoint = new Mat();
            Core.gemm(camTransform, worldPoint, 1.0, new Mat(), 0, transformedPoint);
            System.out.println("point in camera frame");
            System.out.println(transformedPoint.dump());
            assertEquals(-3 * Math.sqrt(2) / 2, transformedPoint.get(0, 0)[0], DELTA);
            assertEquals(0, transformedPoint.get(1, 0)[0], DELTA);
            assertEquals(3 * Math.sqrt(2) / 2, transformedPoint.get(2, 0)[0], DELTA);
        }
        // example: (1,0,0) -> (-sqrt(2), 0, 2sqrt(2))
        {
            Mat worldPoint = Mat.zeros(4, 1, CvType.CV_32F);
            worldPoint.put(0, 0,
                    1, 0, 0, 1);
            System.out.println("point in world frame");
            System.out.println(worldPoint.dump());
            Mat transformedPoint = new Mat();
            Core.gemm(camTransform, worldPoint, 1.0, new Mat(), 0, transformedPoint);
            System.out.println("point in camera frame");
            System.out.println(transformedPoint.dump());
            assertEquals(-Math.sqrt(2), transformedPoint.get(0, 0)[0], DELTA);
            assertEquals(0, transformedPoint.get(1, 0)[0], DELTA);
            assertEquals(2 * Math.sqrt(2), transformedPoint.get(2, 0)[0], DELTA);
        }

    }

    // @Test
    public void testProjection2() {
        final int height = 540; // c=270
        final int width = 960; // c=480
        Size dsize = new Size(width, height);
        final double f = 256.0;
        Mat kMat = VisionUtil.makeIntrinsicMatrix(f, dsize);
        System.out.println("kMat");
        System.out.println(kMat.dump());
        MatOfDouble dMat = new MatOfDouble(Mat.zeros(4, 1, CvType.CV_64F));
        System.out.println("dMat");
        System.out.println(dMat.dump());

        MatOfPoint3f targetGeometryMeters = new MatOfPoint3f(
                new Point3(0.0, 0.0, 0.0),
                new Point3(1.0, 0.0, 0.0),
                new Point3(-1.0, 0.0, 0.0),
                new Point3(2.0, 2.0, 0.0),
                new Point3(-2.0, 2.0, 0.0),
                new Point3(2.0, -2.0, 0.0),
                new Point3(-2.0, -2.0, 0.0));

        // this is camera->world transformation
        // this is location of the camera in world coordinates
        double xPos = -3;
        double yPos = 0;
        double zPos = -3;
        Mat worldTVec = Mat.zeros(3, 1, CvType.CV_32F);
        worldTVec.put(0, 0, xPos, yPos, zPos);
        System.out.println("worldTVec");
        System.out.println(worldTVec.dump());

        // this is camera->world transformation
        // this is rotation of the camera in world coordinates
        double pan = 0.7854;
        Mat worldRV = Mat.zeros(3, 1, CvType.CV_32F);
        worldRV.put(0, 0, 0.0, pan, 0.0);
        System.out.println("worldRV");
        System.out.println(worldRV.dump());

        Mat worldRMat = new Mat();
        Calib3d.Rodrigues(worldRV, worldRMat);

        Mat camRMat = worldRMat.t();
        Mat camRV = new Mat();
        Calib3d.Rodrigues(camRMat, camRV);
        System.out.println("camRV");
        System.out.println(camRV.dump());

        // this is inverse(worldT*worldR)
        // inverse of multiplication is order-reversed multipication of inverses, so
        // which is worldR.t * -worldT or camR*-worldT
        Mat camTVec = new Mat();
        Core.gemm(camRMat, worldTVec, -1.0, new Mat(), 0, camTVec);
        System.out.println("camTVec");
        System.out.println(camTVec.dump());

        // so the final (homogeneous) transform from world to camera
        Mat camTransform = Mat.zeros(4, 4, CvType.CV_32F);
        camTransform.put(0, 0,
                camRMat.get(0, 0)[0], camRMat.get(0, 1)[0], camRMat.get(0, 2)[0], camTVec.get(0, 0)[0],
                camRMat.get(1, 0)[0], camRMat.get(1, 1)[0], camRMat.get(1, 2)[0], camTVec.get(1, 0)[0],
                camRMat.get(2, 0)[0], camRMat.get(2, 1)[0], camRMat.get(2, 2)[0], camTVec.get(2, 0)[0],
                0, 0, 0, 1);
        System.out.println("camTransform");
        System.out.println(camTransform.dump());

        // and we can transform points this way
        for (Point3 p : targetGeometryMeters.toList()) {
            Mat pp = Mat.zeros(4, 1, CvType.CV_32F);
            pp.put(0, 0,
                    p.x, p.y, p.z, 1);
            System.out.println("point in world frame");
            System.out.println(pp.dump());
            Mat transformedPoint = new Mat();
            Core.gemm(camTransform, pp, 1.0, new Mat(), 0, transformedPoint);
            System.out.println("point in camera frame");
            System.out.println(transformedPoint.dump());
        }

        MatOfPoint2f skewedImagePts2f = new MatOfPoint2f();
        Mat jacobian = new Mat();
        // this wants world->camera transformation
        Calib3d.projectPoints(targetGeometryMeters, camRV, camTVec, kMat, dMat,
                skewedImagePts2f, jacobian);
        System.out.println("skewedImagePts2f");
        System.out.println(skewedImagePts2f.dump());

        Scalar green = new Scalar(0, 255, 0);
        Mat img = Mat.zeros(height, width, CvType.CV_32FC3);
        for (Point pt : skewedImagePts2f.toList()) {
            Imgproc.circle(img, pt, 6, green, 1);
        }
        Imgcodecs.imwrite("C:\\Users\\joelt\\Desktop\\pics\\projection.png", img);

    }

    /**
     * now that the camera-center transform is worked out, how do we make the one
     * for the left eye and right eye?
     * the left eye is a simple translation from the camera frame, so maybe we just
     * add a step to the world->camera translation so it's world->camera->eye
     * 
     */
    // @Test
    public void testEyes() {
        final int height = 540; // c=270
        final int width = 960; // c=480
        Size dsize = new Size(width, height);
        final double f = 256.0;
        Mat kMat = VisionUtil.makeIntrinsicMatrix(f, dsize);
        System.out.println("kMat");
        System.out.println(kMat.dump());
        MatOfDouble dMat = new MatOfDouble(Mat.zeros(4, 1, CvType.CV_64F));
        System.out.println("dMat");
        System.out.println(dMat.dump());

        // this is camera->world transformation
        // this is location of the camera in world coordinates
        double xPos = 0;
        double yPos = 0;
        double zPos = -3;
        Mat worldTVec = Mat.zeros(3, 1, CvType.CV_32F);
        worldTVec.put(0, 0, xPos, yPos, zPos);
        System.out.println("worldTVec");
        System.out.println(worldTVec.dump());

        // this is camera->world transformation
        // this is rotation of the camera in world coordinates
        double pan = 0.7854;
        Mat worldRV = Mat.zeros(3, 1, CvType.CV_32F);
        worldRV.put(0, 0, 0.0, pan, 0.0);
        System.out.println("worldRV");
        System.out.println(worldRV.dump());

        Mat worldRMat = new Mat();
        Calib3d.Rodrigues(worldRV, worldRMat);

        Mat camRMat = worldRMat.t();
        Mat camRV = new Mat();
        Calib3d.Rodrigues(camRMat, camRV);
        System.out.println("camRV");
        System.out.println(camRV.dump());

        // this is inverse(worldT*worldR)
        // inverse of multiplication is order-reversed multipication of inverses, so
        // which is worldR.t * -worldT or camR*-worldT
        Mat camTVec = new Mat();
        Core.gemm(camRMat, worldTVec, -1.0, new Mat(), 0, camTVec);
        System.out.println("camTVec");
        System.out.println(camTVec.dump());

        // so the final (homogeneous) transform from world to camera
        Mat worldToCamera = Mat.zeros(4, 4, CvType.CV_32F);
        worldToCamera.put(0, 0,
                camRMat.get(0, 0)[0], camRMat.get(0, 1)[0], camRMat.get(0, 2)[0], camTVec.get(0, 0)[0],
                camRMat.get(1, 0)[0], camRMat.get(1, 1)[0], camRMat.get(1, 2)[0], camTVec.get(1, 0)[0],
                camRMat.get(2, 0)[0], camRMat.get(2, 1)[0], camRMat.get(2, 2)[0], camTVec.get(2, 0)[0],
                0, 0, 0, 1);
        System.out.println("worldToCamera");
        System.out.println(worldToCamera.dump());

        final double base = 0.4; // 50cm camera separation (wide!)
        // transform from camera center to left eye
        Mat baseToLeftEye = Mat.zeros(4, 4, CvType.CV_32F);
        baseToLeftEye.put(0, 0,
                1, 0, 0, base / 2,
                0, 1, 0, 0,
                0, 0, 1, 0,
                0, 0, 0, 1);
        System.out.println("baseToLeftEye");
        System.out.println(baseToLeftEye.dump());

        Mat worldToLeftEye = new Mat();
        Core.gemm(baseToLeftEye, worldToCamera, 1.0, new Mat(), 0.0, worldToLeftEye);
        System.out.println("worldToLeftEye");
        System.out.println(worldToLeftEye.dump());

        // example: (0,0,0) -> (-3sqrt(2)/2, 0, 3sqrt(2)/2)
        {
            Mat worldPoint = Mat.zeros(4, 1, CvType.CV_32F);
            worldPoint.put(0, 0,
                    0, 0, 0, 1);
            System.out.println("point in world frame");
            System.out.println(worldPoint.dump());
            Mat transformedPoint = new Mat();
            Core.gemm(worldToLeftEye, worldPoint, 1.0, new Mat(), 0, transformedPoint);
            System.out.println("point in camera frame");
            System.out.println(transformedPoint.dump());
            assertEquals(base / 2 - 3 * Math.sqrt(2) / 2, transformedPoint.get(0, 0)[0], DELTA);
            assertEquals(0, transformedPoint.get(1, 0)[0], DELTA);
            assertEquals(3 * Math.sqrt(2) / 2, transformedPoint.get(2, 0)[0], DELTA);
        }
        // example: (1,0,0) -> (-sqrt(2), 0, 2sqrt(2))
        {
            Mat worldPoint = Mat.zeros(4, 1, CvType.CV_32F);
            worldPoint.put(0, 0,
                    1, 0, 0, 1);
            System.out.println("point in world frame");
            System.out.println(worldPoint.dump());
            Mat transformedPoint = new Mat();
            Core.gemm(worldToLeftEye, worldPoint, 1.0, new Mat(), 0, transformedPoint);
            System.out.println("point in camera frame");
            System.out.println(transformedPoint.dump());
            assertEquals(base / 2 - Math.sqrt(2), transformedPoint.get(0, 0)[0], DELTA);
            assertEquals(0, transformedPoint.get(1, 0)[0], DELTA);
            assertEquals(2 * Math.sqrt(2), transformedPoint.get(2, 0)[0], DELTA);
        }

        Mat baseToRightEye = Mat.zeros(4, 4, CvType.CV_32F);
        baseToRightEye.put(0, 0,
                1, 0, 0, -base / 2,
                0, 1, 0, 0,
                0, 0, 1, 0,
                0, 0, 0, 1);
        System.out.println("baseToRightEye");
        System.out.println(baseToRightEye.dump());

        Mat worldToRightEye = new Mat();
        Core.gemm(baseToRightEye, worldToCamera, 1.0, new Mat(), 0.0, worldToRightEye);
        System.out.println("worldToRightEye");
        System.out.println(worldToRightEye.dump());

        // example: (0,0,0) -> (-3sqrt(2)/2, 0, 3sqrt(2)/2)
        {
            Mat worldPoint = Mat.zeros(4, 1, CvType.CV_32F);
            worldPoint.put(0, 0,
                    0, 0, 0, 1);
            System.out.println("point in world frame");
            System.out.println(worldPoint.dump());
            Mat transformedPoint = new Mat();
            Core.gemm(worldToRightEye, worldPoint, 1.0, new Mat(), 0, transformedPoint);
            System.out.println("point in camera frame");
            System.out.println(transformedPoint.dump());
            assertEquals(-base / 2 - 3 * Math.sqrt(2) / 2, transformedPoint.get(0, 0)[0], DELTA);
            assertEquals(0, transformedPoint.get(1, 0)[0], DELTA);
            assertEquals(3 * Math.sqrt(2) / 2, transformedPoint.get(2, 0)[0], DELTA);
        }
        // example: (1,0,0) -> (-sqrt(2), 0, 2sqrt(2))
        {
            Mat worldPoint = Mat.zeros(4, 1, CvType.CV_32F);
            worldPoint.put(0, 0,
                    1, 0, 0, 1);
            System.out.println("point in world frame");
            System.out.println(worldPoint.dump());
            Mat transformedPoint = new Mat();
            Core.gemm(worldToRightEye, worldPoint, 1.0, new Mat(), 0, transformedPoint);
            System.out.println("point in camera frame");
            System.out.println(transformedPoint.dump());
            assertEquals(-base / 2 - Math.sqrt(2), transformedPoint.get(0, 0)[0], DELTA);
            assertEquals(0, transformedPoint.get(1, 0)[0], DELTA);
            assertEquals(2 * Math.sqrt(2), transformedPoint.get(2, 0)[0], DELTA);
        }
    }

    /**
     * now extract R and t for each eye and make some pictures. pics seem good.
     */
    // @Test
    public void testEyeImages() {
        final int height = 540; // c=270
        final int width = 960; // c=480
        Size dsize = new Size(width, height);
        final double f = 256.0;
        Mat kMat = VisionUtil.makeIntrinsicMatrix(f, dsize);
        System.out.println("kMat");
        System.out.println(kMat.dump());
        MatOfDouble dMat = new MatOfDouble(Mat.zeros(4, 1, CvType.CV_64F));
        System.out.println("dMat");
        System.out.println(dMat.dump());

        // this is camera->world transformation
        // this is location of the camera in world coordinates
        double xPos = -3;
        double yPos = 0;
        double zPos = -3;
        Mat worldTVec = Mat.zeros(3, 1, CvType.CV_32F);
        worldTVec.put(0, 0, xPos, yPos, zPos);
        System.out.println("worldTVec");
        System.out.println(worldTVec.dump());

        // this is camera->world transformation
        // this is rotation of the camera in world coordinates
        double pan = 0.785398;
        Mat worldRV = Mat.zeros(3, 1, CvType.CV_32F);
        worldRV.put(0, 0, 0.0, pan, 0.0);
        System.out.println("worldRV");
        System.out.println(worldRV.dump());

        Mat worldRMat = new Mat();
        Calib3d.Rodrigues(worldRV, worldRMat);

        Mat camRMat = worldRMat.t();
        Mat camRV = new Mat();
        Calib3d.Rodrigues(camRMat, camRV);
        System.out.println("camRV");
        System.out.println(camRV.dump());

        // this is inverse(worldT*worldR)
        // inverse of multiplication is order-reversed multipication of inverses, so
        // which is worldR.t * -worldT or camR*-worldT
        Mat camTVec = new Mat();
        Core.gemm(camRMat, worldTVec, -1.0, new Mat(), 0, camTVec);
        System.out.println("camTVec");
        System.out.println(camTVec.dump());

        // so the final (homogeneous) transform from world to camera
        Mat worldToCamera = Mat.zeros(4, 4, CvType.CV_32F);
        worldToCamera.put(0, 0,
                camRMat.get(0, 0)[0], camRMat.get(0, 1)[0], camRMat.get(0, 2)[0], camTVec.get(0, 0)[0],
                camRMat.get(1, 0)[0], camRMat.get(1, 1)[0], camRMat.get(1, 2)[0], camTVec.get(1, 0)[0],
                camRMat.get(2, 0)[0], camRMat.get(2, 1)[0], camRMat.get(2, 2)[0], camTVec.get(2, 0)[0],
                0, 0, 0, 1);
        System.out.println("worldToCamera");
        System.out.println(worldToCamera.dump());

        final double base = 0.4; // 50cm camera separation (wide!)
        // transform from camera center to left eye
        Mat baseToLeftEye = Mat.zeros(4, 4, CvType.CV_32F);
        baseToLeftEye.put(0, 0,
                1, 0, 0, base / 2,
                0, 1, 0, 0,
                0, 0, 1, 0,
                0, 0, 0, 1);
        System.out.println("baseToLeftEye");
        System.out.println(baseToLeftEye.dump());

        Mat worldToLeftEye = new Mat();
        Core.gemm(baseToLeftEye, worldToCamera, 1.0, new Mat(), 0.0, worldToLeftEye);
        System.out.println("worldToLeftEye");
        System.out.println(worldToLeftEye.dump());

        Mat baseToRightEye = Mat.zeros(4, 4, CvType.CV_32F);
        baseToRightEye.put(0, 0,
                1, 0, 0, -base / 2,
                0, 1, 0, 0,
                0, 0, 1, 0,
                0, 0, 0, 1);
        System.out.println("baseToRightEye");
        System.out.println(baseToRightEye.dump());

        Mat worldToRightEye = new Mat();
        Core.gemm(baseToRightEye, worldToCamera, 1.0, new Mat(), 0.0, worldToRightEye);
        System.out.println("worldToRightEye");
        System.out.println(worldToRightEye.dump());

        // start with one point at the origin
        MatOfPoint3f targetGeometryMeters = new MatOfPoint3f(
                new Point3(0.0, 0.0, 0.0),
                new Point3(1.0, 0.0, 0.0),
                new Point3(-1.0, 0.0, 0.0),
                new Point3(2.0, 2.0, 0.0),
                new Point3(-2.0, 2.0, 0.0),
                new Point3(2.0, -2.0, 0.0),
                new Point3(-2.0, -2.0, 0.0));

        {
            Mat leftCamRV = Mat.zeros(3, 1, CvType.CV_32F);
            Calib3d.Rodrigues(worldToLeftEye.rowRange(0, 3).colRange(0, 3), leftCamRV);
            System.out.println("leftCamRV");
            System.out.println(leftCamRV.dump());
            Mat leftCamTVec = worldToLeftEye.colRange(3, 4).rowRange(0, 3);
            System.out.println("leftCamTVec");
            System.out.println(leftCamTVec.dump());

            MatOfPoint2f leftPts = new MatOfPoint2f();
            Mat jacobian = new Mat();
            // this wants world->camera transformation
            Calib3d.projectPoints(targetGeometryMeters, leftCamRV, leftCamTVec, kMat, dMat,
                    leftPts, jacobian);
            System.out.println("leftPts");
            System.out.println(leftPts.dump());

            Scalar green = new Scalar(0, 255, 0);
            Mat imgLeft = Mat.zeros(height, width, CvType.CV_32FC3);
            for (Point pt : leftPts.toList()) {
                Imgproc.circle(imgLeft, pt, 6, green, 1);
            }
            Imgcodecs.imwrite("C:\\Users\\joelt\\Desktop\\pics\\projectionLeft.png", imgLeft);
        }
        {
            Mat rightCamRV = Mat.zeros(3, 1, CvType.CV_32F);
            Calib3d.Rodrigues(worldToRightEye.rowRange(0, 3).colRange(0, 3), rightCamRV);
            System.out.println("rightCamRV");
            System.out.println(rightCamRV.dump());
            Mat rightCamTVec = worldToRightEye.colRange(3, 4).rowRange(0, 3);
            System.out.println("rightCamTVec");
            System.out.println(rightCamTVec.dump());

            MatOfPoint2f rightPts = new MatOfPoint2f();
            Mat jacobian = new Mat();
            // this wants world->camera transformation
            Calib3d.projectPoints(targetGeometryMeters, rightCamRV, rightCamTVec, kMat, dMat,
                    rightPts, jacobian);
            System.out.println("rightPts");
            System.out.println(rightPts.dump());

            Scalar green = new Scalar(0, 255, 0);
            Mat imgRight = Mat.zeros(height, width, CvType.CV_32FC3);
            for (Point pt : rightPts.toList()) {
                Imgproc.circle(imgRight, pt, 6, green, 1);
            }
            Imgcodecs.imwrite("C:\\Users\\joelt\\Desktop\\pics\\projectionRight.png", imgRight);
        }

    }

    /**
     * now using the eye points, triangulate
     */
    // @Test
    public void testEyeTriangulate() {
        final int height = 540; // c=270
        final int width = 960; // c=480
        Size dsize = new Size(width, height);
        final double f = 256.0;
        Mat kMat = VisionUtil.makeIntrinsicMatrix(f, dsize);
        System.out.println("kMat");
        System.out.println(kMat.dump());
        MatOfDouble dMat = new MatOfDouble(Mat.zeros(4, 1, CvType.CV_64F));
        System.out.println("dMat");
        System.out.println(dMat.dump());

        // this is camera->world transformation
        // this is location of the camera in world coordinates
        double xPos = -3;
        double yPos = 0;
        double zPos = -3;
        Mat worldTVec = Mat.zeros(3, 1, CvType.CV_32F);
        worldTVec.put(0, 0, xPos, yPos, zPos);
        System.out.println("worldTVec");
        System.out.println(worldTVec.dump());

        // this is camera->world transformation
        // this is rotation of the camera in world coordinates
        double pan = 0.785398;
        Mat worldRV = Mat.zeros(3, 1, CvType.CV_32F);
        worldRV.put(0, 0, 0.0, pan, 0.0);
        System.out.println("worldRV");
        System.out.println(worldRV.dump());

        Mat worldRMat = new Mat();
        Calib3d.Rodrigues(worldRV, worldRMat);

        Mat camRMat = worldRMat.t();
        Mat camRV = new Mat();
        Calib3d.Rodrigues(camRMat, camRV);
        System.out.println("camRV");
        System.out.println(camRV.dump());

        // now the whole camera->world
        Mat cameraToWorld = Mat.zeros(4, 4, CvType.CV_32F);
        cameraToWorld.put(0, 0,
                worldRMat.get(0, 0)[0], worldRMat.get(0, 1)[0], worldRMat.get(0, 2)[0], worldTVec.get(0, 0)[0],
                worldRMat.get(1, 0)[0], worldRMat.get(1, 1)[0], worldRMat.get(1, 2)[0], worldTVec.get(1, 0)[0],
                worldRMat.get(2, 0)[0], worldRMat.get(2, 1)[0], worldRMat.get(2, 2)[0], worldTVec.get(2, 0)[0],
                0, 0, 0, 1);

        System.out.println("cameraToWorld");
        System.out.println(cameraToWorld.dump());

        // this is inverse(worldT*worldR)
        // inverse of multiplication is order-reversed multipication of inverses, so
        // which is worldR.t * -worldT or camR*-worldT
        Mat camTVec = new Mat();
        Core.gemm(camRMat, worldTVec, -1.0, new Mat(), 0, camTVec);
        System.out.println("camTVec");
        System.out.println(camTVec.dump());

        // so the final (homogeneous) transform from world to camera
        Mat worldToCamera = Mat.zeros(4, 4, CvType.CV_32F);
        worldToCamera.put(0, 0,
                camRMat.get(0, 0)[0], camRMat.get(0, 1)[0], camRMat.get(0, 2)[0], camTVec.get(0, 0)[0],
                camRMat.get(1, 0)[0], camRMat.get(1, 1)[0], camRMat.get(1, 2)[0], camTVec.get(1, 0)[0],
                camRMat.get(2, 0)[0], camRMat.get(2, 1)[0], camRMat.get(2, 2)[0], camTVec.get(2, 0)[0],
                0, 0, 0, 1);
        System.out.println("worldToCamera");
        System.out.println(worldToCamera.dump());

        final double base = 0.4;

        // transform from camera center to left eye
        Mat baseToLeftEye = Mat.zeros(4, 4, CvType.CV_32F);
        baseToLeftEye.put(0, 0,
                1, 0, 0, base / 2,
                0, 1, 0, 0,
                0, 0, 1, 0,
                0, 0, 0, 1);
        System.out.println("baseToLeftEye");
        System.out.println(baseToLeftEye.dump());

        Mat worldToLeftEye = new Mat();
        Core.gemm(baseToLeftEye, worldToCamera, 1.0, new Mat(), 0.0, worldToLeftEye);
        System.out.println("worldToLeftEye");
        System.out.println(worldToLeftEye.dump());

        Mat baseToRightEye = Mat.zeros(4, 4, CvType.CV_32F);
        baseToRightEye.put(0, 0,
                1, 0, 0, -base / 2,
                0, 1, 0, 0,
                0, 0, 1, 0,
                0, 0, 0, 1);
        System.out.println("baseToRightEye");
        System.out.println(baseToRightEye.dump());

        Mat worldToRightEye = new Mat();
        Core.gemm(baseToRightEye, worldToCamera, 1.0, new Mat(), 0.0, worldToRightEye);
        System.out.println("worldToRightEye");
        System.out.println(worldToRightEye.dump());

        MatOfPoint3f targetGeometryMeters = new MatOfPoint3f(
                new Point3(0.0, 0.0, 0.01), // solver needs this little bit of non-planarity
                new Point3(1.0, 0.0, 0.0),
                new Point3(-1.0, 0.0, 0.0),
                new Point3(2.0, 2.0, 0.0),
                new Point3(-2.0, 2.0, 0.0),
                new Point3(2.0, -2.0, 0.0),
                new Point3(-2.0, -2.0, 0.0));

        Mat homogeneousTarget = new Mat();
        Calib3d.convertPointsToHomogeneous(targetGeometryMeters, homogeneousTarget);
        homogeneousTarget = homogeneousTarget.reshape(1);
        System.out.println("homogeneousTarget");
        System.out.println(homogeneousTarget.dump());

        Mat leftCamRV = Mat.zeros(3, 1, CvType.CV_32F);
        Calib3d.Rodrigues(worldToLeftEye.rowRange(0, 3).colRange(0, 3), leftCamRV);
        System.out.println("leftCamRV");
        System.out.println(leftCamRV.dump());
        Mat leftCamTVec = worldToLeftEye.colRange(3, 4).rowRange(0, 3);
        System.out.println("leftCamTVec");
        System.out.println(leftCamTVec.dump());

        MatOfPoint2f leftPts = new MatOfPoint2f();
        Mat jacobian = new Mat();
        // this wants world->camera transformation
        Calib3d.projectPoints(targetGeometryMeters, leftCamRV, leftCamTVec, kMat, dMat,
                leftPts, jacobian);
        System.out.println("leftPts");
        System.out.println(leftPts.dump());

        Scalar green = new Scalar(0, 255, 0);
        Mat imgLeft = Mat.zeros(height, width, CvType.CV_32FC3);
        for (Point pt : leftPts.toList()) {
            Imgproc.circle(imgLeft, pt, 6, green, 1);
        }
        Imgcodecs.imwrite("C:\\Users\\joelt\\Desktop\\pics\\projection2Left.png", imgLeft);

        Mat rightCamRV = Mat.zeros(3, 1, CvType.CV_32F);
        Calib3d.Rodrigues(worldToRightEye.rowRange(0, 3).colRange(0, 3), rightCamRV);
        System.out.println("rightCamRV");
        System.out.println(rightCamRV.dump());
        Mat rightCamTVec = worldToRightEye.colRange(3, 4).rowRange(0, 3);
        System.out.println("rightCamTVec");
        System.out.println(rightCamTVec.dump());

        MatOfPoint2f rightPts = new MatOfPoint2f();

        // this wants world->camera transformation
        Calib3d.projectPoints(targetGeometryMeters, rightCamRV, rightCamTVec, kMat, dMat,
                rightPts, jacobian);
        System.out.println("rightPts");
        System.out.println(rightPts.dump());

        Mat imgRight = Mat.zeros(height, width, CvType.CV_32FC3);
        for (Point pt : rightPts.toList()) {
            Imgproc.circle(imgRight, pt, 6, green, 1);
        }
        Imgcodecs.imwrite("C:\\Users\\joelt\\Desktop\\pics\\projection2Right.png", imgRight);

        // i really don't know what the "P" matrices should be; the triangulatePoints
        // thing says they should transform *world* frame into camera frames but
        // nobody does that; instead they use triangulatepoints to find points in the
        // *camera* frame, since the whole point is that you don't know where the camera
        // is.
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

        System.out.println("Pright");
        System.out.println(Pright.dump());

        Mat predictedHomogeneous = new Mat();
        Calib3d.triangulatePoints(Pleft, Pright, leftPts, rightPts, predictedHomogeneous);
        System.out.println("predictedHomogeneous");
        System.out.println(predictedHomogeneous.t().dump());

        Mat predictedNormal = new Mat();
        Calib3d.convertPointsFromHomogeneous(predictedHomogeneous.t(), predictedNormal);
        System.out.println("predictedNormal");
        System.out.println(predictedNormal.channels());
        System.out.println(predictedNormal.dump());

        Mat predictedHomogeneousNormalized = new Mat();
        Calib3d.convertPointsToHomogeneous(predictedNormal, predictedHomogeneousNormalized);
        System.out.println("predictedHomogeneousNormalized");
        predictedHomogeneousNormalized = predictedHomogeneousNormalized.reshape(1);
        System.out.println(predictedHomogeneousNormalized.channels());
        System.out.println(predictedHomogeneousNormalized.dump());

        // these are in camera frame
        // now transform back into world
        // predictedWorld is exactly the target geometry, and predictedhomogeneous
        // is exactly the triangulation, so cameraToWorld is what we want to solve for.
        Mat predictedWorld = new Mat();
        Core.gemm(cameraToWorld, predictedHomogeneous, 1.0, new Mat(), 0.0, predictedWorld);
        System.out.println("predictedWorld");
        System.out.println(predictedWorld.t().dump());

        Mat predictedWorldNormal = new Mat();
        Calib3d.convertPointsFromHomogeneous(predictedWorld.t(), predictedWorldNormal);
        System.out.println("predictedWorldNormal");
        System.out.println(predictedWorldNormal.dump());

        System.out.println("targetGeometryMeters for comparison");
        System.out.println(targetGeometryMeters.dump());

        // error is tiny, -1e6
        Mat triangulationError = new Mat();
        Core.subtract(targetGeometryMeters, predictedWorldNormal, triangulationError);
        System.out.println("triangulationError");
        System.out.println(triangulationError.dump());

        // so what we want is to derive cameratoworld, so solve for it.

        System.out.println("this is x");
        System.out.println("predictedHomogeneousNormalized");
        System.out.println(predictedHomogeneousNormalized.t().dump());
        System.out.println("this is b");
        System.out.println("homogeneousTarget");
        System.out.println(homogeneousTarget.t().dump());

        // solves Ax=b. A * camera triangulation = world coords.
        System.out.println("solve Ax=b");
        Mat A = new Mat();
        Core.solve(predictedHomogeneousNormalized, homogeneousTarget, A, Core.DECOMP_SVD);

        System.out.println("AT");
        System.out.println(A.t().dump());

        Mat predictedRV = Mat.zeros(3, 1, CvType.CV_32F);
        Calib3d.Rodrigues(A.t().rowRange(0, 3).colRange(0, 3), predictedRV);
        System.out.println("predictedRV");
        System.out.println(predictedRV.dump());

        Mat predictedTV = A.t().colRange(3, 4).rowRange(0, 3);
        System.out.println("predictedTV");
        System.out.println(predictedTV.dump());

    }

    /**
     * ok now the grid works for all three dimensions (x, z, theta).
     */
    @Test
    public void testStereoGrid() {

        // global invariants

        final int height = 540; // c=270
        final int width = 960; // c=480
        final Size dsize = new Size(width, height);
        Rect viewport = new Rect(10, 10, width - 20, height - 20);
        final double f = 256.0;
        final Mat kMat = VisionUtil.makeIntrinsicMatrix(f, dsize);
        System.out.println("kMat");
        System.out.println(kMat.dump());
        final MatOfDouble dMat = new MatOfDouble(Mat.zeros(4, 1, CvType.CV_64F));
        System.out.println("dMat");
        System.out.println(dMat.dump());

        final double base = 0.4;

        final MatOfPoint3f targetGeometryMeters = new MatOfPoint3f(
                new Point3(0.0, 0.0, 0.01), // solver needs this little bit of non-planarity
                new Point3(1.0, 0.0, 0.0),
                new Point3(-1.0, 0.0, 0.0),
                new Point3(2.0, 2.0, 0.0),
                new Point3(-2.0, 2.0, 0.0),
                new Point3(2.0, -2.0, 0.0),
                new Point3(-2.0, -2.0, 0.0));

        // i really don't know what the "P" matrices should be; the triangulatePoints
        // thing says they should transform *world* frame into camera frames but
        // nobody does that; instead they use triangulatepoints to find points in the
        // *camera* frame, since the whole point is that you don't know where the camera
        // is.
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

        System.out.println("Pright");
        System.out.println(Pright.dump());

        // fixed coordinates

        final double yPos = 0;

        // grid

        int idx = 0;
        System.out.println("idx, pan, xpos, zpos, ppan, pxpos, pzpos");
        for (double pan = -Math.PI / 2; pan <= Math.PI / 2; pan += Math.PI / 8) {
            for (double zPos = -10.0; zPos <= -1.0; zPos += 1.0) {
                point: for (double xPos = -4.0; xPos <= 4.0; xPos += 1.0) {
                    idx += 1;

                    // camera-to-world transforms

                    Mat worldTVec = Mat.zeros(3, 1, CvType.CV_32F);
                    worldTVec.put(0, 0, xPos, yPos, zPos);
                    // System.out.println("worldTVec");
                    // System.out.println(worldTVec.dump());

                    Mat worldRV = Mat.zeros(3, 1, CvType.CV_32F);
                    worldRV.put(0, 0, 0.0, pan, 0.0);
                    // System.out.println("worldRV");
                    // System.out.println(worldRV.dump());

                    Mat worldRMat = new Mat();
                    Calib3d.Rodrigues(worldRV, worldRMat);

                    Mat camRMat = worldRMat.t();
                    Mat camRV = new Mat();
                    Calib3d.Rodrigues(camRMat, camRV);
                    // System.out.println("camRV");
                    // System.out.println(camRV.dump());

                    // now the whole camera->world
                    Mat cameraToWorld = Mat.zeros(4, 4, CvType.CV_32F);
                    cameraToWorld.put(0, 0,
                            worldRMat.get(0, 0)[0], worldRMat.get(0, 1)[0], worldRMat.get(0, 2)[0],
                            worldTVec.get(0, 0)[0],
                            worldRMat.get(1, 0)[0], worldRMat.get(1, 1)[0], worldRMat.get(1, 2)[0],
                            worldTVec.get(1, 0)[0],
                            worldRMat.get(2, 0)[0], worldRMat.get(2, 1)[0], worldRMat.get(2, 2)[0],
                            worldTVec.get(2, 0)[0],
                            0, 0, 0, 1);

                    // System.out.println("cameraToWorld");
                    // System.out.println(cameraToWorld.dump());

                    // this is inverse(worldT*worldR)
                    // inverse of multiplication is order-reversed multipication of inverses, so
                    // which is worldR.t * -worldT or camR*-worldT
                    Mat camTVec = new Mat();
                    Core.gemm(camRMat, worldTVec, -1.0, new Mat(), 0, camTVec);
                    // System.out.println("camTVec");
                    // System.out.println(camTVec.dump());

                    // so the final (homogeneous) transform from world to camera
                    Mat worldToCamera = Mat.zeros(4, 4, CvType.CV_32F);
                    worldToCamera.put(0, 0,
                            camRMat.get(0, 0)[0], camRMat.get(0, 1)[0], camRMat.get(0, 2)[0], camTVec.get(0, 0)[0],
                            camRMat.get(1, 0)[0], camRMat.get(1, 1)[0], camRMat.get(1, 2)[0], camTVec.get(1, 0)[0],
                            camRMat.get(2, 0)[0], camRMat.get(2, 1)[0], camRMat.get(2, 2)[0], camTVec.get(2, 0)[0],
                            0, 0, 0, 1);
                    // System.out.println("worldToCamera");
                    // System.out.println(worldToCamera.dump());

                    // transform from camera center to left eye
                    Mat baseToLeftEye = Mat.zeros(4, 4, CvType.CV_32F);
                    baseToLeftEye.put(0, 0,
                            1, 0, 0, base / 2,
                            0, 1, 0, 0,
                            0, 0, 1, 0,
                            0, 0, 0, 1);
                    // System.out.println("baseToLeftEye");
                    // System.out.println(baseToLeftEye.dump());

                    Mat worldToLeftEye = new Mat();
                    Core.gemm(baseToLeftEye, worldToCamera, 1.0, new Mat(), 0.0, worldToLeftEye);
                    // System.out.println("worldToLeftEye");
                    // System.out.println(worldToLeftEye.dump());

                    Mat baseToRightEye = Mat.zeros(4, 4, CvType.CV_32F);
                    baseToRightEye.put(0, 0,
                            1, 0, 0, -base / 2,
                            0, 1, 0, 0,
                            0, 0, 1, 0,
                            0, 0, 0, 1);
                    // System.out.println("baseToRightEye");
                    // System.out.println(baseToRightEye.dump());

                    Mat worldToRightEye = new Mat();
                    Core.gemm(baseToRightEye, worldToCamera, 1.0, new Mat(), 0.0, worldToRightEye);
                    // System.out.println("worldToRightEye");
                    // System.out.println(worldToRightEye.dump());

                    // make images

                    Mat homogeneousTarget = new Mat();
                    Calib3d.convertPointsToHomogeneous(targetGeometryMeters, homogeneousTarget);
                    homogeneousTarget = homogeneousTarget.reshape(1);
                    // System.out.println("homogeneousTarget");
                    // System.out.println(homogeneousTarget.dump());

                    Mat leftCamRV = Mat.zeros(3, 1, CvType.CV_32F);
                    Calib3d.Rodrigues(worldToLeftEye.rowRange(0, 3).colRange(0, 3), leftCamRV);
                    // System.out.println("leftCamRV");
                    // System.out.println(leftCamRV.dump());
                    Mat leftCamTVec = worldToLeftEye.colRange(3, 4).rowRange(0, 3);
                    // System.out.println("leftCamTVec");
                    // System.out.println(leftCamTVec.dump());

                    MatOfPoint2f leftPts = new MatOfPoint2f();
                    Mat jacobian = new Mat();
                    // this wants world->camera transformation
                    Calib3d.projectPoints(targetGeometryMeters, leftCamRV, leftCamTVec, kMat, dMat,
                            leftPts, jacobian);
                    // System.out.println("leftPts");
                    // System.out.println(leftPts.dump());

                    Scalar green = new Scalar(0, 255, 0);
                    Mat imgLeft = Mat.zeros(height, width, CvType.CV_32FC3);
                    for (Point pt : leftPts.toList()) {
                        if (!viewport.contains(pt))
                            continue point;
                        Imgproc.circle(imgLeft, pt, 6, green, 1);
                    }
                    Imgcodecs.imwrite(String.format("C:\\Users\\joelt\\Desktop\\pics\\projection-%d-left.png",
                            idx), imgLeft);

                    Mat rightCamRV = Mat.zeros(3, 1, CvType.CV_32F);
                    Calib3d.Rodrigues(worldToRightEye.rowRange(0, 3).colRange(0, 3), rightCamRV);
                    // System.out.println("rightCamRV");
                    // System.out.println(rightCamRV.dump());
                    Mat rightCamTVec = worldToRightEye.colRange(3, 4).rowRange(0, 3);
                    // System.out.println("rightCamTVec");
                    // System.out.println(rightCamTVec.dump());

                    MatOfPoint2f rightPts = new MatOfPoint2f();

                    // this wants world->camera transformation
                    Calib3d.projectPoints(targetGeometryMeters, rightCamRV, rightCamTVec, kMat, dMat,
                            rightPts, jacobian);
                    // System.out.println("rightPts");
                    // System.out.println(rightPts.dump());

                    Mat imgRight = Mat.zeros(height, width, CvType.CV_32FC3);
                    for (Point pt : rightPts.toList()) {
                        if (!viewport.contains(pt))
                            continue point;
                        Imgproc.circle(imgRight, pt, 6, green, 1);
                    }
                    Imgcodecs.imwrite(String.format("C:\\Users\\joelt\\Desktop\\pics\\projection-%d-right.png",
                            idx), imgRight);

                    // triangulate

                    Mat predictedHomogeneous = new Mat();
                    Calib3d.triangulatePoints(Pleft, Pright, leftPts, rightPts, predictedHomogeneous);
                    // System.out.println("predictedHomogeneous");
                    // System.out.println(predictedHomogeneous.t().dump());

                    Mat predictedNormal = new Mat();
                    Calib3d.convertPointsFromHomogeneous(predictedHomogeneous.t(), predictedNormal);
                    // System.out.println("predictedNormal");
                    // System.out.println(predictedNormal.channels());
                    // System.out.println(predictedNormal.dump());

                    Mat predictedHomogeneousNormalized = new Mat();
                    Calib3d.convertPointsToHomogeneous(predictedNormal, predictedHomogeneousNormalized);
                    // System.out.println("predictedHomogeneousNormalized");
                    predictedHomogeneousNormalized = predictedHomogeneousNormalized.reshape(1);
                    // System.out.println(predictedHomogeneousNormalized.channels());
                    // System.out.println(predictedHomogeneousNormalized.dump());

                    // these are in camera frame
                    // now transform back into world
                    // predictedWorld is exactly the target geometry, and predictedhomogeneous
                    // is exactly the triangulation, so cameraToWorld is what we want to solve for.
                    Mat predictedWorld = new Mat();
                    Core.gemm(cameraToWorld, predictedHomogeneous, 1.0, new Mat(), 0.0, predictedWorld);
                    // System.out.println("predictedWorld");
                    // System.out.println(predictedWorld.t().dump());

                    Mat predictedWorldNormal = new Mat();
                    Calib3d.convertPointsFromHomogeneous(predictedWorld.t(), predictedWorldNormal);
                    // System.out.println("predictedWorldNormal");
                    // System.out.println(predictedWorldNormal.dump());

                    // System.out.println("targetGeometryMeters for comparison");
                    // System.out.println(targetGeometryMeters.dump());

                    // error is tiny, -1e6
                    Mat triangulationError = new Mat();
                    Core.subtract(targetGeometryMeters, predictedWorldNormal, triangulationError);
                    // System.out.println("triangulationError");
                    // System.out.println(triangulationError.dump());

                    // so what we want is to derive cameratoworld, so solve for it.

                    // System.out.println("this is x");
                    // System.out.println("predictedHomogeneousNormalized");
                    // System.out.println(predictedHomogeneousNormalized.t().dump());
                    // System.out.println("this is b");
                    // System.out.println("homogeneousTarget");
                    // System.out.println(homogeneousTarget.t().dump());

                    // solves Ax=b. A * camera triangulation = world coords.
                    // System.out.println("solve Ax=b");
                    Mat A = new Mat();
                    Core.solve(predictedHomogeneousNormalized, homogeneousTarget, A, Core.DECOMP_SVD);

                    // System.out.println("AT");
                    // System.out.println(A.t().dump());

                    Mat predictedRV = Mat.zeros(3, 1, CvType.CV_32F);
                    Calib3d.Rodrigues(A.t().rowRange(0, 3).colRange(0, 3), predictedRV);
                    // System.out.println("predictedRV");
                    // System.out.println(predictedRV.dump());

                    Mat predictedTV = A.t().colRange(3, 4).rowRange(0, 3);
                    // System.out.println("predictedTV");
                    // System.out.println(predictedTV.dump());

                    double pxPos = predictedTV.get(0, 0)[0];
                    double pzPos = predictedTV.get(2, 0)[0];
                    double ppan = predictedRV.get(1, 0)[0];

                    System.out.printf("%d, %5.2f, %5.2f, %5.2f, %5.2f, %5.2f, %5.2f\n",
                            idx, pan, xPos, zPos, ppan, pxPos, pzPos);

                }
            }
        }

    }

}
