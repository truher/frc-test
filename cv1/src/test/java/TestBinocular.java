import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

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

import vision.MyCalib3d;
import vision.VisionUtil;

/**
 * Binocular vision to try to improve distant pose estimation.
 */
public class TestBinocular {
    public static final double DELTA = 0.001;
    public static final boolean DEBUG = false;

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
        debug("kMat", kMat);
        MatOfDouble dMat = new MatOfDouble(Mat.zeros(4, 1, CvType.CV_64F));

        // target is 0.4m wide, 0.1m high
        final double targetWidth = 0.4;
        final double targetHeight = 0.1;
        MatOfPoint3f targetGeometryMeters = VisionUtil.makeTargetGeometry3f2(targetWidth,
                targetHeight);
        debug("targetGeometryMeters", targetGeometryMeters);

        // camera base at 0,0,-4, straight ahead, in meters, world coords
        // double xPos = 0.0;
        // double yPos = 1.0; // camera is below the target
        double yPos = 0.5;
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
                debug("leftPts", leftPts);

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
                debug("rightPts", rightPts);
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
                Mat worldTV = new Mat();
                Core.gemm(camRM.t(), camTV, -1.0, new Mat(), 0.0, worldTV);
                Mat targetCameraRt = Mat.zeros(4, 4, CvType.CV_32F);
                targetCameraRt.put(0, 0,
                        camRM.get(0, 0)[0], camRM.get(0, 1)[0], camRM.get(0, 2)[0], worldTV.get(0, 0)[0],
                        camRM.get(1, 0)[0], camRM.get(1, 1)[0], camRM.get(1, 2)[0], worldTV.get(1, 0)[0],
                        camRM.get(2, 0)[0], camRM.get(2, 1)[0], camRM.get(2, 2)[0], worldTV.get(2, 0)[0],
                        0, 0, 0, 1);

                debug("targetCameraRt", targetCameraRt);

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

                debug("Pleft", Pleft);

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
                debug("homogeneousTarget", homogeneousTarget.reshape(1));

                Mat targetInCamera = new Mat();
                Core.gemm(targetCameraRt, homogeneousTarget.reshape(1).t(), 1.0, new Mat(), 0.0, targetInCamera);
                debug("targetInCamera", targetInCamera.t());

                // this is meters i guess
                Mat projectedTargetLeft = new Mat();
                // Core.gemm(Pleft, homogeneousTarget.reshape(1).t(), 1.0, new Mat(), 0.0,
                // projectedTargetLeft);
                Core.gemm(Pleft, targetInCamera, 1.0, new Mat(), 0.0, projectedTargetLeft);
                debug("projectedTargetLeft", projectedTargetLeft.t());

                Mat projectedTargetLeftNormal = new Mat();
                Calib3d.convertPointsFromHomogeneous(projectedTargetLeft.t(), projectedTargetLeftNormal);
                debug("projectedTargetLeftNormal", projectedTargetLeftNormal);

                Mat projectedTargetRight = new Mat();
                // Core.gemm(Pright, homogeneousTarget.reshape(1).t(), 1.0, new Mat(), 0.0,
                // projectedTargetRight);
                Core.gemm(Pright, targetInCamera, 1.0, new Mat(), 0.0, projectedTargetRight);
                debug("projectedTargetRight", projectedTargetRight.t());

                // points in the left camera's rectified coords, one channel
                // pts are pixels
                // P base is in meters
                // so this is all wrong
                //
                Mat predictedHomogeneous = new Mat();
                Calib3d.triangulatePoints(Pleft, Pright, leftPts, rightPts, predictedHomogeneous);
                debug("predictedHomogeneous == what triangulatepoints says", predictedHomogeneous.t());
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
                debug("fixedT == camera view from base center, homogeneous", fixedT);
                //
                Mat fixedTNormal = new Mat();
                Calib3d.convertPointsFromHomogeneous(fixedT, fixedTNormal);
                debug("fixedTNormal == target points in camera coords, just to see", fixedTNormal);

                Mat fullyFixed = new Mat();
                Calib3d.convertPointsFromHomogeneous(fixedT, fullyFixed);
                Calib3d.convertPointsToHomogeneous(fullyFixed, fixedT);
                debug("fixedT == camera view from base center, homogeneous, normalized?", fixedT.reshape(1));

                // try dropping Y, since the robot is on the floor and the target height
                // is fixed.
                Mat dropY = Mat.zeros(3, 4, CvType.CV_32F);
                dropY.put(0, 0,
                        1, 0, 0, 0,
                        0, 0, 1, 0,
                        0, 0, 0, 1);
                Mat fixedT2d = new Mat();
                Core.gemm(dropY, fixedT.reshape(1).t(), 1.0, new Mat(), 0.0, fixedT2d);
                debug("fixedT2d", fixedT2d.t());

                Mat homogeneousTarget2d = new Mat();
                Core.gemm(dropY, homogeneousTarget.reshape(1).t(), 1.0, new Mat(), 0.0, homogeneousTarget2d);
                debug("homogeneousTarget2d", homogeneousTarget2d.t());

                // solves Ax=b. A * camera triangulation = world coords.
                Mat A = new Mat();
                // Core.solve(fixedT.reshape(1), homogeneousTarget.reshape(1), A,
                // Core.DECOMP_SVD);
                Core.solve(fixedT2d.t(), homogeneousTarget2d.t(), A, Core.DECOMP_SVD);

                Mat Atp = A.t(); // why transpose? because it seems to produce the right answer? yuck.
                debug("Atp", Atp);

                Mat reproject = new Mat();
                // Core.gemm(Atp, fixedT.reshape(1).t(), 1.0, new Mat(), 0.0, reproject);
                Core.gemm(Atp, fixedT2d, 1.0, new Mat(), 0.0, reproject);
                debug("reproject", reproject.t());

                Mat reprojectionError = new Mat();
                Core.subtract(homogeneousTarget2d.t(), reproject.t(), reprojectionError);
                debug("reprojectionError", reprojectionError);

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
                debug("tvec == translation of camera in world?", tvec);

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
                debug("rmat = rotation of camera in world?", rmat);

                Mat rvec = new Mat();
                Mat jacobian = new Mat();
                Calib3d.Rodrigues(rmat, rvec, jacobian);
                debug("rvec", rvec);
                Mat euler = VisionUtil.rotm2euler(rmat);
                debug("euler", euler);

                // double euler = VisionUtil.rotm2euler2d(rmat);
                debug("euler", euler);

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
        debug("kMat", kMat);
        MatOfDouble dMat = new MatOfDouble(Mat.zeros(4, 1, CvType.CV_64F));
        debug("dMat", dMat);

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
        debug("worldTVec", worldTVec);

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
        debug("worldRV", worldRV);

        Mat worldRMat = new Mat();
        Calib3d.Rodrigues(worldRV, worldRMat);

        Mat camRMat = worldRMat.t();
        Mat camRV = new Mat();
        Calib3d.Rodrigues(camRMat, camRV);
        debug("camRV", camRV);

        // this is inverse(worldT*worldR)
        // inverse of multiplication is order-reversed multipication of inverses, so
        // which is worldR.t * -worldT or camR*-worldT
        Mat camTVec = new Mat();
        Core.gemm(camRMat, worldTVec, -1.0, new Mat(), 0, camTVec);
        debug("camTVec", camTVec);

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
        debug("camTransform", camTransform);

        // example: (0,0,0) -> (-3sqrt(2)/2, 0, 3sqrt(2)/2)
        {
            Mat worldPoint = Mat.zeros(4, 1, CvType.CV_32F);
            worldPoint.put(0, 0,
                    0, 0, 0, 1);
            debug("point in world frame", worldPoint);
            Mat transformedPoint = new Mat();
            Core.gemm(camTransform, worldPoint, 1.0, new Mat(), 0, transformedPoint);
            debug("point in camera frame", transformedPoint);
            assertEquals(-3 * Math.sqrt(2) / 2, transformedPoint.get(0, 0)[0], DELTA);
            assertEquals(0, transformedPoint.get(1, 0)[0], DELTA);
            assertEquals(3 * Math.sqrt(2) / 2, transformedPoint.get(2, 0)[0], DELTA);
        }
        // example: (1,0,0) -> (-sqrt(2), 0, 2sqrt(2))
        {
            Mat worldPoint = Mat.zeros(4, 1, CvType.CV_32F);
            worldPoint.put(0, 0,
                    1, 0, 0, 1);
            debug("point in world frame", worldPoint);
            Mat transformedPoint = new Mat();
            Core.gemm(camTransform, worldPoint, 1.0, new Mat(), 0, transformedPoint);
            debug("point in camera frame", transformedPoint);
            assertEquals(-Math.sqrt(2), transformedPoint.get(0, 0)[0], DELTA);
            assertEquals(0, transformedPoint.get(1, 0)[0], DELTA);
            assertEquals(2 * Math.sqrt(2), transformedPoint.get(2, 0)[0], DELTA);
        }

    }

  //  @Test
    public void testProjection2() {
        final int height = 540; // c=270
        final int width = 960; // c=480
        Size dsize = new Size(width, height);
        final double f = 256.0;
        Mat kMat = VisionUtil.makeIntrinsicMatrix(f, dsize);
        debug("kMat", kMat);
        MatOfDouble dMat = new MatOfDouble(Mat.zeros(4, 1, CvType.CV_64F));
        debug("dMat", dMat);

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
        debug("worldTVec", worldTVec);

        // this is camera->world transformation
        // this is rotation of the camera in world coordinates
        double pan = 0.7854;
        Mat worldRV = Mat.zeros(3, 1, CvType.CV_32F);
        worldRV.put(0, 0, 0.0, pan, 0.0);
        debug("worldRV", worldRV);

        Mat worldRMat = new Mat();
        Calib3d.Rodrigues(worldRV, worldRMat);

        Mat camRMat = worldRMat.t();
        Mat camRV = new Mat();
        Calib3d.Rodrigues(camRMat, camRV);
        debug("camRV", camRV);

        // this is inverse(worldT*worldR)
        // inverse of multiplication is order-reversed multipication of inverses, so
        // which is worldR.t * -worldT or camR*-worldT
        Mat camTVec = new Mat();
        Core.gemm(camRMat, worldTVec, -1.0, new Mat(), 0, camTVec);
        debug("camTVec", camTVec);

        // so the final (homogeneous) transform from world to camera
        Mat camTransform = Mat.zeros(4, 4, CvType.CV_32F);
        camTransform.put(0, 0,
                camRMat.get(0, 0)[0], camRMat.get(0, 1)[0], camRMat.get(0, 2)[0], camTVec.get(0, 0)[0],
                camRMat.get(1, 0)[0], camRMat.get(1, 1)[0], camRMat.get(1, 2)[0], camTVec.get(1, 0)[0],
                camRMat.get(2, 0)[0], camRMat.get(2, 1)[0], camRMat.get(2, 2)[0], camTVec.get(2, 0)[0],
                0, 0, 0, 1);
        debug("camTransform", camTransform);

        // and we can transform points this way
        for (Point3 p : targetGeometryMeters.toList()) {
            Mat pp = Mat.zeros(4, 1, CvType.CV_32F);
            pp.put(0, 0,
                    p.x, p.y, p.z, 1);
            debug("point in world frame", pp);
            Mat transformedPoint = new Mat();
            Core.gemm(camTransform, pp, 1.0, new Mat(), 0, transformedPoint);
            debug("point in camera frame", transformedPoint);
        }

        MatOfPoint2f skewedImagePts2f = new MatOfPoint2f();
        Mat jacobian = new Mat();
        // this wants world->camera transformation
        Calib3d.projectPoints(targetGeometryMeters, camRV, camTVec, kMat, dMat,
                skewedImagePts2f, jacobian);
        debug("skewedImagePts2f", skewedImagePts2f);

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
  //  @Test
    public void testEyes() {
        final int height = 540; // c=270
        final int width = 960; // c=480
        Size dsize = new Size(width, height);
        final double f = 256.0;
        Mat kMat = VisionUtil.makeIntrinsicMatrix(f, dsize);
        debug("kMat", kMat);
        MatOfDouble dMat = new MatOfDouble(Mat.zeros(4, 1, CvType.CV_64F));
        debug("dMat", dMat);

        // this is camera->world transformation
        // this is location of the camera in world coordinates
        double xPos = 0;
        double yPos = 0;
        double zPos = -3;
        Mat worldTVec = Mat.zeros(3, 1, CvType.CV_32F);
        worldTVec.put(0, 0, xPos, yPos, zPos);
        debug("worldTVec", worldTVec);

        // this is camera->world transformation
        // this is rotation of the camera in world coordinates
        double pan = 0.7854;
        Mat worldRV = Mat.zeros(3, 1, CvType.CV_32F);
        worldRV.put(0, 0, 0.0, pan, 0.0);
        debug("worldRV", worldRV);

        Mat worldRMat = new Mat();
        Calib3d.Rodrigues(worldRV, worldRMat);

        Mat camRMat = worldRMat.t();
        Mat camRV = new Mat();
        Calib3d.Rodrigues(camRMat, camRV);
        debug("camRV", camRV);

        // this is inverse(worldT*worldR)
        // inverse of multiplication is order-reversed multipication of inverses, so
        // which is worldR.t * -worldT or camR*-worldT
        Mat camTVec = new Mat();
        Core.gemm(camRMat, worldTVec, -1.0, new Mat(), 0, camTVec);
        debug("camTVec", camTVec);

        // so the final (homogeneous) transform from world to camera
        Mat worldToCamera = Mat.zeros(4, 4, CvType.CV_32F);
        worldToCamera.put(0, 0,
                camRMat.get(0, 0)[0], camRMat.get(0, 1)[0], camRMat.get(0, 2)[0], camTVec.get(0, 0)[0],
                camRMat.get(1, 0)[0], camRMat.get(1, 1)[0], camRMat.get(1, 2)[0], camTVec.get(1, 0)[0],
                camRMat.get(2, 0)[0], camRMat.get(2, 1)[0], camRMat.get(2, 2)[0], camTVec.get(2, 0)[0],
                0, 0, 0, 1);
        debug("worldToCamera", worldToCamera);

        final double base = 0.4; // 50cm camera separation (wide!)
        // transform from camera center to left eye
        Mat baseToLeftEye = Mat.zeros(4, 4, CvType.CV_32F);
        baseToLeftEye.put(0, 0,
                1, 0, 0, base / 2,
                0, 1, 0, 0,
                0, 0, 1, 0,
                0, 0, 0, 1);
        debug("baseToLeftEye", baseToLeftEye);

        Mat worldToLeftEye = new Mat();
        Core.gemm(baseToLeftEye, worldToCamera, 1.0, new Mat(), 0.0, worldToLeftEye);
        debug("worldToLeftEye", worldToLeftEye);

        // example: (0,0,0) -> (-3sqrt(2)/2, 0, 3sqrt(2)/2)
        {
            Mat worldPoint = Mat.zeros(4, 1, CvType.CV_32F);
            worldPoint.put(0, 0,
                    0, 0, 0, 1);
            debug("point in world frame", worldPoint);
            Mat transformedPoint = new Mat();
            Core.gemm(worldToLeftEye, worldPoint, 1.0, new Mat(), 0, transformedPoint);
            debug("point in camera frame", transformedPoint);
            assertEquals(base / 2 - 3 * Math.sqrt(2) / 2, transformedPoint.get(0, 0)[0], DELTA);
            assertEquals(0, transformedPoint.get(1, 0)[0], DELTA);
            assertEquals(3 * Math.sqrt(2) / 2, transformedPoint.get(2, 0)[0], DELTA);
        }
        // example: (1,0,0) -> (-sqrt(2), 0, 2sqrt(2))
        {
            Mat worldPoint = Mat.zeros(4, 1, CvType.CV_32F);
            worldPoint.put(0, 0,
                    1, 0, 0, 1);
            debug("point in world frame", worldPoint);
            Mat transformedPoint = new Mat();
            Core.gemm(worldToLeftEye, worldPoint, 1.0, new Mat(), 0, transformedPoint);
            debug("point in camera frame", transformedPoint);
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
        debug("baseToRightEye", baseToRightEye);

        Mat worldToRightEye = new Mat();
        Core.gemm(baseToRightEye, worldToCamera, 1.0, new Mat(), 0.0, worldToRightEye);
        debug("worldToRightEye", worldToRightEye);

        // example: (0,0,0) -> (-3sqrt(2)/2, 0, 3sqrt(2)/2)
        {
            Mat worldPoint = Mat.zeros(4, 1, CvType.CV_32F);
            worldPoint.put(0, 0,
                    0, 0, 0, 1);
            debug("point in world frame", worldPoint);
            Mat transformedPoint = new Mat();
            Core.gemm(worldToRightEye, worldPoint, 1.0, new Mat(), 0, transformedPoint);
            debug("point in camera frame", transformedPoint);
            assertEquals(-base / 2 - 3 * Math.sqrt(2) / 2, transformedPoint.get(0, 0)[0], DELTA);
            assertEquals(0, transformedPoint.get(1, 0)[0], DELTA);
            assertEquals(3 * Math.sqrt(2) / 2, transformedPoint.get(2, 0)[0], DELTA);
        }
        // example: (1,0,0) -> (-sqrt(2), 0, 2sqrt(2))
        {
            Mat worldPoint = Mat.zeros(4, 1, CvType.CV_32F);
            worldPoint.put(0, 0,
                    1, 0, 0, 1);
            debug("point in world frame", worldPoint);
            Mat transformedPoint = new Mat();
            Core.gemm(worldToRightEye, worldPoint, 1.0, new Mat(), 0, transformedPoint);
            debug("point in camera frame", transformedPoint);
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
        debug("kMat", kMat);
        MatOfDouble dMat = new MatOfDouble(Mat.zeros(4, 1, CvType.CV_64F));
        debug("dMat", dMat);

        // this is camera->world transformation
        // this is location of the camera in world coordinates
        double xPos = -3;
        double yPos = 0;
        double zPos = -3;
        Mat worldTVec = Mat.zeros(3, 1, CvType.CV_32F);
        worldTVec.put(0, 0, xPos, yPos, zPos);
        debug("worldTVec", worldTVec);

        // this is camera->world transformation
        // this is rotation of the camera in world coordinates
        double pan = 0.785398;
        Mat worldRV = Mat.zeros(3, 1, CvType.CV_32F);
        worldRV.put(0, 0, 0.0, pan, 0.0);
        debug("worldRV", worldRV);

        Mat worldRMat = new Mat();
        Calib3d.Rodrigues(worldRV, worldRMat);

        Mat camRMat = worldRMat.t();
        Mat camRV = new Mat();
        Calib3d.Rodrigues(camRMat, camRV);
        debug("camRV", camRV);

        // this is inverse(worldT*worldR)
        // inverse of multiplication is order-reversed multipication of inverses, so
        // which is worldR.t * -worldT or camR*-worldT
        Mat camTVec = new Mat();
        Core.gemm(camRMat, worldTVec, -1.0, new Mat(), 0, camTVec);
        debug("camTVec", camTVec);

        // so the final (homogeneous) transform from world to camera
        Mat worldToCamera = Mat.zeros(4, 4, CvType.CV_32F);
        worldToCamera.put(0, 0,
                camRMat.get(0, 0)[0], camRMat.get(0, 1)[0], camRMat.get(0, 2)[0], camTVec.get(0, 0)[0],
                camRMat.get(1, 0)[0], camRMat.get(1, 1)[0], camRMat.get(1, 2)[0], camTVec.get(1, 0)[0],
                camRMat.get(2, 0)[0], camRMat.get(2, 1)[0], camRMat.get(2, 2)[0], camTVec.get(2, 0)[0],
                0, 0, 0, 1);
        debug("worldToCamera", worldToCamera);

        final double base = 0.4; // 50cm camera separation (wide!)
        // transform from camera center to left eye
        Mat baseToLeftEye = Mat.zeros(4, 4, CvType.CV_32F);
        baseToLeftEye.put(0, 0,
                1, 0, 0, base / 2,
                0, 1, 0, 0,
                0, 0, 1, 0,
                0, 0, 0, 1);
        debug("baseToLeftEye", baseToLeftEye);

        Mat worldToLeftEye = new Mat();
        Core.gemm(baseToLeftEye, worldToCamera, 1.0, new Mat(), 0.0, worldToLeftEye);
        debug("worldToLeftEye", worldToLeftEye);

        Mat baseToRightEye = Mat.zeros(4, 4, CvType.CV_32F);
        baseToRightEye.put(0, 0,
                1, 0, 0, -base / 2,
                0, 1, 0, 0,
                0, 0, 1, 0,
                0, 0, 0, 1);
        debug("baseToRightEye", baseToRightEye);

        Mat worldToRightEye = new Mat();
        Core.gemm(baseToRightEye, worldToCamera, 1.0, new Mat(), 0.0, worldToRightEye);
        debug("worldToRightEye", worldToRightEye);

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
            debug("leftCamRV",
                    leftCamRV);
            Mat leftCamTVec = worldToLeftEye.colRange(3, 4).rowRange(0, 3);
            debug("leftCamTVec",
                    leftCamTVec);

            MatOfPoint2f leftPts = new MatOfPoint2f();
            Mat jacobian = new Mat();
            // this wants world->camera transformation
            Calib3d.projectPoints(targetGeometryMeters, leftCamRV, leftCamTVec, kMat, dMat,
                    leftPts, jacobian);
            debug("leftPts", leftPts);

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
            debug("rightCamRV", rightCamRV);
            Mat rightCamTVec = worldToRightEye.colRange(3, 4).rowRange(0, 3);
            debug("rightCamTVec", rightCamTVec);

            MatOfPoint2f rightPts = new MatOfPoint2f();
            Mat jacobian = new Mat();
            // this wants world->camera transformation
            Calib3d.projectPoints(targetGeometryMeters, rightCamRV, rightCamTVec, kMat, dMat,
                    rightPts, jacobian);
            debug("rightPts",
                    rightPts);

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
  //  @Test
    public void testEyeTriangulate() {
        final int height = 540; // c=270
        final int width = 960; // c=480
        Size dsize = new Size(width, height);
        final double f = 256.0;
        Mat kMat = VisionUtil.makeIntrinsicMatrix(f, dsize);
        debug("kMat", kMat);
        MatOfDouble dMat = new MatOfDouble(Mat.zeros(4, 1, CvType.CV_64F));
        debug("dMat", dMat);

        // this is camera->world transformation
        // this is location of the camera in world coordinates
        double xPos = -3;
        double yPos = 0;
        double zPos = -3;
        Mat worldTVec = Mat.zeros(3, 1, CvType.CV_32F);
        worldTVec.put(0, 0, xPos, yPos, zPos);
        debug("worldTVec", worldTVec);

        // this is camera->world transformation
        // this is rotation of the camera in world coordinates
        double pan = 0.785398;
        Mat worldRV = Mat.zeros(3, 1, CvType.CV_32F);
        worldRV.put(0, 0, 0.0, pan, 0.0);
        debug("worldRV",
                worldRV);

        Mat worldRMat = new Mat();
        Calib3d.Rodrigues(worldRV, worldRMat);

        Mat camRMat = worldRMat.t();
        Mat camRV = new Mat();
        Calib3d.Rodrigues(camRMat, camRV);
        debug("camRV", camRV);

        // now the whole camera->world
        Mat cameraToWorld = Mat.zeros(4, 4, CvType.CV_32F);
        cameraToWorld.put(0, 0,
                worldRMat.get(0, 0)[0], worldRMat.get(0, 1)[0], worldRMat.get(0, 2)[0], worldTVec.get(0, 0)[0],
                worldRMat.get(1, 0)[0], worldRMat.get(1, 1)[0], worldRMat.get(1, 2)[0], worldTVec.get(1, 0)[0],
                worldRMat.get(2, 0)[0], worldRMat.get(2, 1)[0], worldRMat.get(2, 2)[0], worldTVec.get(2, 0)[0],
                0, 0, 0, 1);

        debug("cameraToWorld", cameraToWorld);

        // this is inverse(worldT*worldR)
        // inverse of multiplication is order-reversed multipication of inverses, so
        // which is worldR.t * -worldT or camR*-worldT
        Mat camTVec = new Mat();
        Core.gemm(camRMat, worldTVec, -1.0, new Mat(), 0, camTVec);
        debug("camTVec", camTVec);

        // so the final (homogeneous) transform from world to camera
        Mat worldToCamera = Mat.zeros(4, 4, CvType.CV_32F);
        worldToCamera.put(0, 0,
                camRMat.get(0, 0)[0], camRMat.get(0, 1)[0], camRMat.get(0, 2)[0], camTVec.get(0, 0)[0],
                camRMat.get(1, 0)[0], camRMat.get(1, 1)[0], camRMat.get(1, 2)[0], camTVec.get(1, 0)[0],
                camRMat.get(2, 0)[0], camRMat.get(2, 1)[0], camRMat.get(2, 2)[0], camTVec.get(2, 0)[0],
                0, 0, 0, 1);
        debug("worldToCamera", worldToCamera);

        final double base = 0.4;

        // transform from camera center to left eye
        Mat baseToLeftEye = Mat.zeros(4, 4, CvType.CV_32F);
        baseToLeftEye.put(0, 0,
                1, 0, 0, base / 2,
                0, 1, 0, 0,
                0, 0, 1, 0,
                0, 0, 0, 1);
        debug("baseToLeftEye", baseToLeftEye);

        Mat worldToLeftEye = new Mat();
        Core.gemm(baseToLeftEye, worldToCamera, 1.0, new Mat(), 0.0, worldToLeftEye);
        debug("worldToLeftEye", worldToLeftEye);

        Mat baseToRightEye = Mat.zeros(4, 4, CvType.CV_32F);
        baseToRightEye.put(0, 0,
                1, 0, 0, -base / 2,
                0, 1, 0, 0,
                0, 0, 1, 0,
                0, 0, 0, 1);
        debug("baseToRightEye", baseToRightEye);

        Mat worldToRightEye = new Mat();
        Core.gemm(baseToRightEye, worldToCamera, 1.0, new Mat(), 0.0, worldToRightEye);
        debug("worldToRightEye", worldToRightEye);

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
        debug("homogeneousTarget", homogeneousTarget);

        Mat leftCamRV = Mat.zeros(3, 1, CvType.CV_32F);
        Calib3d.Rodrigues(worldToLeftEye.rowRange(0, 3).colRange(0, 3), leftCamRV);
        debug("leftCamRV", leftCamRV);
        Mat leftCamTVec = worldToLeftEye.colRange(3, 4).rowRange(0, 3);
        debug("leftCamTVec", leftCamTVec);

        MatOfPoint2f leftPts = new MatOfPoint2f();
        Mat jacobian = new Mat();
        // this wants world->camera transformation
        Calib3d.projectPoints(targetGeometryMeters, leftCamRV, leftCamTVec, kMat, dMat,
                leftPts, jacobian);
        debug("leftPts", leftPts);

        Scalar green = new Scalar(0, 255, 0);
        Mat imgLeft = Mat.zeros(height, width, CvType.CV_32FC3);
        for (Point pt : leftPts.toList()) {
            Imgproc.circle(imgLeft, pt, 6, green, 1);
        }
        Imgcodecs.imwrite("C:\\Users\\joelt\\Desktop\\pics\\projection2Left.png", imgLeft);

        Mat rightCamRV = Mat.zeros(3, 1, CvType.CV_32F);
        Calib3d.Rodrigues(worldToRightEye.rowRange(0, 3).colRange(0, 3), rightCamRV);
        debug("rightCamRV", rightCamRV);
        Mat rightCamTVec = worldToRightEye.colRange(3, 4).rowRange(0, 3);
        debug("rightCamTVec", rightCamTVec);

        MatOfPoint2f rightPts = new MatOfPoint2f();

        // this wants world->camera transformation
        Calib3d.projectPoints(targetGeometryMeters, rightCamRV, rightCamTVec, kMat, dMat,
                rightPts, jacobian);
        debug("rightPts", rightPts);

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

        debug("Pleft", Pleft);

        Mat Pright = Mat.zeros(3, 4, CvType.CV_32F);
        Pright.put(0, 0,
                f, 0, width / 2, -base * f / 2,
                0, f, height / 2, 0,
                0, 0, 1, 0);

        debug("Pright", Pright);

        Mat predictedHomogeneous = new Mat();
        Calib3d.triangulatePoints(Pleft, Pright, leftPts, rightPts, predictedHomogeneous);
        debug("predictedHomogeneous", predictedHomogeneous.t());

        Mat predictedNormal = new Mat();
        Calib3d.convertPointsFromHomogeneous(predictedHomogeneous.t(), predictedNormal);
        debug("predictedNormal", predictedNormal);

        Mat predictedHomogeneousNormalized = new Mat();
        Calib3d.convertPointsToHomogeneous(predictedNormal, predictedHomogeneousNormalized);

        predictedHomogeneousNormalized = predictedHomogeneousNormalized.reshape(1);
        debug("predictedHomogeneousNormalized", predictedHomogeneousNormalized);

        // these are in camera frame
        // now transform back into world
        // predictedWorld is exactly the target geometry, and predictedhomogeneous
        // is exactly the triangulation, so cameraToWorld is what we want to solve for.
        Mat predictedWorld = new Mat();
        Core.gemm(cameraToWorld, predictedHomogeneous, 1.0, new Mat(), 0.0, predictedWorld);
        debug("predictedWorld", predictedWorld.t());

        Mat predictedWorldNormal = new Mat();
        Calib3d.convertPointsFromHomogeneous(predictedWorld.t(), predictedWorldNormal);
        debug("predictedWorldNormal", predictedWorldNormal);

        debug("targetGeometryMeters for comparison", targetGeometryMeters);

        // error is tiny, -1e6
        Mat triangulationError = new Mat();
        Core.subtract(targetGeometryMeters, predictedWorldNormal, triangulationError);
        debug("triangulationError", triangulationError);

        // so what we want is to derive cameratoworld, so solve for it.

        debugmsg("this is x");
        debug("predictedHomogeneousNormalized", predictedHomogeneousNormalized.t());
        debugmsg("this is b");
        debug("homogeneousTarget", homogeneousTarget.t());

        // solves Ax=b. A * camera triangulation = world coords.
        debugmsg("solve Ax=b");
        Mat A = new Mat();
        Core.solve(predictedHomogeneousNormalized, homogeneousTarget, A, Core.DECOMP_SVD);

        debug("AT", A.t());

        Mat predictedRV = Mat.zeros(3, 1, CvType.CV_32F);
        Calib3d.Rodrigues(A.t().rowRange(0, 3).colRange(0, 3), predictedRV);
        debug("predictedRV", predictedRV);

        Mat predictedTV = A.t().colRange(3, 4).rowRange(0, 3);
        debug("predictedTV", predictedTV);

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
        debug("kMat", kMat);
        final MatOfDouble dMat = new MatOfDouble(Mat.zeros(4, 1, CvType.CV_64F));
        debug("dMat", dMat);

        final double base = 0.4;

        final MatOfPoint3f targetGeometryMeters = new MatOfPoint3f(
                new Point3(0.0, 0.0, 0.0),
                new Point3(1.0, 1.0, 0.0),
                new Point3(1.0, -1.0, 0.0),
                new Point3(-1.0, -1.0, 0.0),
                new Point3(-1.0, 1.0, 0.0));

        // final MatOfPoint3f targetGeometryMeters = new MatOfPoint3f(
        // new Point3(0.0, 0.0, 0.01), // solver needs this little bit of non-planarity
        // new Point3(1.0, 0.0, 0.0),
        // new Point3(-1.0, 0.0, 0.0),
        // new Point3(2.0, 2.0, 0.0),
        // new Point3(-2.0, 2.0, 0.0),
        // new Point3(2.0, -2.0, 0.0),
        // new Point3(-2.0, -2.0, 0.0));

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

        debug("Pleft", Pleft);

        Mat Pright = Mat.zeros(3, 4, CvType.CV_32F);
        Pright.put(0, 0,
                f, 0, width / 2, -base * f / 2,
                0, f, height / 2, 0,
                0, 0, 1, 0);

        debug("Pright", Pright);

        //
        //
        //
        Random rand = new Random(42);
        double noisePixels = 2;
        int pointMultiplier = 3;
        MatOfPoint3f targetPointsMultiplied = new MatOfPoint3f();
        List<Point3> targetpointlist = new ArrayList<Point3>();
        for (int reps = 0; reps < pointMultiplier; reps++) {
            for (Point3 p : targetGeometryMeters.toList()) {
                targetpointlist.add(p);
            }
        }
        targetPointsMultiplied = new MatOfPoint3f(targetpointlist.toArray(new Point3[0]));
        //
        //
        //

        // fixed coordinates

        final double yPos = 0;

        // grid

        int idx = 0;
        System.out.println("idx, pan, xpos, ypos, zpos, ppan, pxpos, pypos, pzpos");
        for (double pan = -Math.PI / 2; pan <= Math.PI / 2; pan += Math.PI / 8) {
            for (double zPos = -10.0; zPos <= -1.0; zPos += 1.0) {
                point: for (double xPos = -4.0; xPos <= 4.0; xPos += 1.0) {
                    // for (double pan = 0; pan <= 0; pan += Math.PI / 8) {
                    // for (double zPos = -5.0; zPos <= -5.0; zPos += 1.0) {
                    // point: for (double xPos = -0.0; xPos <= 0.0; xPos += 1.0) {
                    idx += 1;

                    // camera-to-world transforms

                    Mat worldTVec = Mat.zeros(3, 1, CvType.CV_32F);
                    worldTVec.put(0, 0, xPos, yPos, zPos);
                    debug("worldTVec", worldTVec);

                    Mat worldRV = Mat.zeros(3, 1, CvType.CV_32F);
                    worldRV.put(0, 0, 0.0, pan, 0.0);
                    debug("worldRV", worldRV);

                    Mat worldRMat = new Mat();
                    Calib3d.Rodrigues(worldRV, worldRMat);

                    Mat camRMat = worldRMat.t();
                    Mat camRV = new Mat();
                    Calib3d.Rodrigues(camRMat, camRV);
                    debug("camRV", camRV);

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

                    debug("cameraToWorld", cameraToWorld);

                    // this is inverse(worldT*worldR)
                    // inverse of multiplication is order-reversed multipication of inverses, so
                    // which is worldR.t * -worldT or camR*-worldT
                    Mat camTVec = new Mat();
                    Core.gemm(camRMat, worldTVec, -1.0, new Mat(), 0, camTVec);
                    debug("camTVec", camTVec);

                    // so the final (homogeneous) transform from world to camera
                    Mat worldToCamera = Mat.zeros(4, 4, CvType.CV_32F);
                    worldToCamera.put(0, 0,
                            camRMat.get(0, 0)[0], camRMat.get(0, 1)[0], camRMat.get(0, 2)[0], camTVec.get(0, 0)[0],
                            camRMat.get(1, 0)[0], camRMat.get(1, 1)[0], camRMat.get(1, 2)[0], camTVec.get(1, 0)[0],
                            camRMat.get(2, 0)[0], camRMat.get(2, 1)[0], camRMat.get(2, 2)[0], camTVec.get(2, 0)[0],
                            0, 0, 0, 1);
                    debug("worldToCamera", worldToCamera);

                    // transform from camera center to left eye
                    Mat baseToLeftEye = Mat.zeros(4, 4, CvType.CV_32F);
                    baseToLeftEye.put(0, 0,
                            1, 0, 0, base / 2,
                            0, 1, 0, 0,
                            0, 0, 1, 0,
                            0, 0, 0, 1);
                    debug("baseToLeftEye", baseToLeftEye);

                    Mat worldToLeftEye = new Mat();
                    Core.gemm(baseToLeftEye, worldToCamera, 1.0, new Mat(), 0.0, worldToLeftEye);
                    debug("worldToLeftEye", worldToLeftEye);

                    Mat baseToRightEye = Mat.zeros(4, 4, CvType.CV_32F);
                    baseToRightEye.put(0, 0,
                            1, 0, 0, -base / 2,
                            0, 1, 0, 0,
                            0, 0, 1, 0,
                            0, 0, 0, 1);
                    debug("baseToRightEye", baseToRightEye);

                    Mat worldToRightEye = new Mat();
                    Core.gemm(baseToRightEye, worldToCamera, 1.0, new Mat(), 0.0, worldToRightEye);
                    debug("worldToRightEye", worldToRightEye);

                    // make images

                    Mat homogeneousTarget = new Mat();
                    Calib3d.convertPointsToHomogeneous(targetGeometryMeters, homogeneousTarget);
                    homogeneousTarget = homogeneousTarget.reshape(1);
                    debug("homogeneousTarget", homogeneousTarget);

                    Mat leftCamRV = Mat.zeros(3, 1, CvType.CV_32F);
                    Calib3d.Rodrigues(worldToLeftEye.rowRange(0, 3).colRange(0, 3), leftCamRV);
                    debug("leftCamRV", leftCamRV);
                    Mat leftCamTVec = worldToLeftEye.colRange(3, 4).rowRange(0, 3);
                    debug("leftCamTVec", leftCamTVec);

                    MatOfPoint2f leftPts = new MatOfPoint2f();
                    Mat jacobian = new Mat();
                    // this wants world->camera transformation
                    Calib3d.projectPoints(targetGeometryMeters, leftCamRV, leftCamTVec, kMat, dMat,
                            leftPts, jacobian);
                    debug("leftPts", leftPts);

                    // perturb points
                    List<Point> leftPtsList = new ArrayList<Point>();
                    for (int reps = 0; reps < pointMultiplier; reps++) {
                        for (Point p : leftPts.toList()) {
                            p.x = p.x + rand.nextGaussian() * noisePixels;
                            p.y = p.y + rand.nextGaussian() * noisePixels;
                            leftPtsList.add(p);
                        }
                    }
                    leftPts = new MatOfPoint2f(leftPtsList.toArray(new Point[0]));

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
                    debug("rightCamRV", rightCamRV);
                    Mat rightCamTVec = worldToRightEye.colRange(3, 4).rowRange(0, 3);
                    debug("rightCamTVec", rightCamTVec);

                    MatOfPoint2f rightPts = new MatOfPoint2f();

                    // this wants world->camera transformation
                    Calib3d.projectPoints(targetGeometryMeters, rightCamRV, rightCamTVec, kMat, dMat,
                            rightPts, jacobian);
                    debug("rightPts", rightPts);

                    // perturb points
                    List<Point> rightPtsList = new ArrayList<Point>();
                    for (int reps = 0; reps < pointMultiplier; reps++) {
                        for (Point p : rightPts.toList()) {
                            p.x = p.x + rand.nextGaussian() * noisePixels;
                            p.y = p.y + rand.nextGaussian() * noisePixels;
                            rightPtsList.add(p);
                        }
                    }
                    rightPts = new MatOfPoint2f(rightPtsList.toArray(new Point[0]));

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
                    debug("predictedHomogeneous", predictedHomogeneous.t());

                    Mat predictedNormal = new Mat();
                    Calib3d.convertPointsFromHomogeneous(predictedHomogeneous.t(), predictedNormal);
                    debug("predictedNormal", predictedNormal);
                    //
                    //
                    //
                    // if (idx > 64) break;
                    //
                    //
                    //
                    Mat predictedHomogeneousNormalized = new Mat();
                    Calib3d.convertPointsToHomogeneous(predictedNormal, predictedHomogeneousNormalized);

                    predictedHomogeneousNormalized = predictedHomogeneousNormalized.reshape(1);
                    debug("predictedHomogeneousNormalized", predictedHomogeneousNormalized);

                    // these are in camera frame
                    // now transform back into world
                    // predictedWorld is exactly the target geometry, and predictedhomogeneous
                    // is exactly the triangulation, so cameraToWorld is what we want to solve for.
                    Mat predictedWorld = new Mat();
                    Core.gemm(cameraToWorld, predictedHomogeneous, 1.0, new Mat(), 0.0, predictedWorld);
                    debug("predictedWorld", predictedWorld.t());

                    Mat predictedWorldNormal = new Mat();
                    Calib3d.convertPointsFromHomogeneous(predictedWorld.t(), predictedWorldNormal);
                    debug("predictedWorldNormal", predictedWorldNormal);

                    debug("targetGeometryMeters for comparison", targetGeometryMeters);
                     

                    // error is tiny, -1e6
                    Mat triangulationError = new Mat();
                    Core.subtract(targetPointsMultiplied, predictedWorldNormal,
                            triangulationError);
                    debug("triangulationError", triangulationError);

                    // so what we want is to derive cameratoworld, so solve for it.

                    debugmsg("this is x");
                    debug("predictedHomogeneousNormalized", predictedHomogeneousNormalized);
                    debugmsg("this is b");
                    debug("homogeneousTarget", homogeneousTarget);
                    // if (idx > 0)
                    // continue;

                    // solves Ax=b. A * camera triangulation = world coords.
                    debugmsg("solve Ax=b");
                    Mat A = new Mat();
                    // Core.solve(predictedHomogeneousNormalized, homogeneousTarget, A,
                    // Core.DECOMP_SVD);
                    Core.solve(predictedHomogeneousNormalized, homogeneousTarget, A, Core.DECOMP_SVD);

                    debug("AT", A.t());

                    //
                    //
                    //

                    // so Core.solve seems to really suck at this, and it's more general than I
                    // need. this is a 3d affine transform? actually affine includes
                    // scaling which i don't want.
                    // i could treat it as a 2d problem (just x and z) so i could
                    // use estimateAffine2D.

                    // but first try estimateAffine3D, it has a parameter to force scale (!)
                    // ... but only in 4.6, and we use 4.5.2. copy the 4.6 one.
                    // ... but it's in c++. try the one we do have first.

                    {

                        debugmsg("try affine transform");
                        debug("predictedNormal", predictedNormal);
                        debug("targetPointsMultiplied", targetPointsMultiplied);
                        Mat inliers = new Mat();
                        Mat affineTransform1 = new Mat();
                        Calib3d.estimateAffine3D(predictedNormal, targetPointsMultiplied,
                                affineTransform1,
                                inliers, 0.95);
                        debug("affineTransform", affineTransform1);
                        debug("inliers", inliers);
                    }

                    debugmsg("try Umeyama affine transform");
                    debug("predictedNormal", predictedNormal);
                    debug("targetPointsMultiplied", targetPointsMultiplied);
                    Mat affineTransform = MyCalib3d.estimateAffine3D(predictedNormal, targetPointsMultiplied, null,
                            true);
                    debug("affineTransform", affineTransform);

                    //
                    //
                    //
                    // ok the 3d one basically doesn't work.
                    // before porting the 4.6 version try the 2d version.
                    //
                    //
                    //
                    Mat prediction2d = Mat.zeros(predictedNormal.rows(), 2, CvType.CV_32F);
                    for (int i = 0; i < predictedNormal.rows(); ++i) {
                        prediction2d.put(i, 0, predictedNormal.reshape(1).get(i, 0)[0],
                                predictedNormal.reshape(1).get(i, 2)[0]);
                    }
                    debug("prediction2d", prediction2d);

                    Mat target2d = Mat.zeros(targetPointsMultiplied.rows(), 2, CvType.CV_32F);
                    for (int i = 0; i < targetPointsMultiplied.rows(); ++i) {
                        target2d.put(i, 0, targetPointsMultiplied.reshape(1).get(i, 0)[0],
                                targetPointsMultiplied.reshape(1).get(i, 2)[0]);
                    }
                    debug("target2d", target2d);
                    //
                    //
                    //
                    {
                        Mat inliers = new Mat();
                        Mat affine2d = Calib3d.estimateAffinePartial2D(prediction2d, target2d,
                                inliers, Calib3d.LMEDS,
                                0.5,
                                1000, 0.99, 1000);
                        debug("affine2d", affine2d);
                    }
                    //
                    //
                    //
                    Mat predictedRV = Mat.zeros(3, 1, CvType.CV_32F);
                    // Calib3d.Rodrigues(A.t().rowRange(0, 3).colRange(0, 3), predictedRV);
                    Calib3d.Rodrigues(affineTransform.rowRange(0, 3).colRange(0, 3), predictedRV);
                    debug("predictedRV", predictedRV);

                    // Mat predictedTV = A.t().colRange(3, 4).rowRange(0, 3);
                    Mat predictedTV = affineTransform.colRange(3, 4).rowRange(0, 3);
                    debug("predictedTV", predictedTV);

                    double pxPos = predictedTV.get(0, 0)[0];
                    double pyPos = predictedTV.get(1, 0)[0];
                    double pzPos = predictedTV.get(2, 0)[0];
                    double ppan = predictedRV.get(1, 0)[0];

                    System.out.printf("%d, %5.2f, %5.2f, %5.2f, %5.2f, %5.2f, %5.2f, %5.2f, %5.2f\n",
                            idx, pan, xPos, yPos, zPos, ppan, pxPos, pyPos, pzPos);

                }
            }
        }

    }

    public static void debugmsg(String msg) {
        if (!DEBUG)
            return;
        System.out.println(msg);
    }

    public static void debug(String msg, Mat m) {
        if (!DEBUG)
            return;
        System.out.println(msg);
        System.out.println(m.dump());
    }

    public static void debug(String msg, double d) {
        if (!DEBUG)
            return;
        System.out.println(msg);
        System.out.println(d);
    }

}
