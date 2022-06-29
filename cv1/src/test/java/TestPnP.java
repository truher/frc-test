import static org.junit.Assert.assertEquals;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.junit.Test;
//import org.junit.Test;
import org.opencv.calib3d.Calib3d;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfDouble;
import org.opencv.core.MatOfInt;
import org.opencv.core.MatOfPoint;
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
 * This uses {@link Calib3d#solvePnP()} to estimate poses using a single camera.
 */
public class TestPnP {
    public static final double DELTA = 0.00001;
    public static final boolean DEBUG = false;

    public TestPnP() {
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
    }

    /**
     * Try using {@link Calib3d#solvePnP()}. This takes some 3d points and projects
     * them into a single camera, then reverses to get the pose of the camera.
     */
    @Test
    public void testSolvePnP() {
        // known "world" geometry
        MatOfPoint3f objectPts3f = new MatOfPoint3f(
                new Point3(-20, -10, 0.0),
                new Point3(-20, 10, 0.0),
                new Point3(20, 10, 0.0),
                new Point3(20, -10, 0.0));

        // rotate 1 radian
        Mat rVec = Mat.zeros(3, 1, CvType.CV_64F);
        rVec.put(0, 0, 0.0, 1.0, 0.0);

        // translate to the right, up, far
        Mat tVec = Mat.zeros(3, 1, CvType.CV_64F);
        tVec.put(0, 0, 10.0, -20.0, 70.0);

        Mat kMat = Mat.zeros(3, 3, CvType.CV_64F);
        kMat.put(0, 0,
                400.0, 0.0, 256.0,
                0.0, 400.0, 256.0,
                0.0, 0.0, 1.0);

        MatOfDouble dMat = new MatOfDouble(Mat.zeros(4, 1, CvType.CV_64F));

        // project the world into the camera plane
        MatOfPoint2f imagePts2f = new MatOfPoint2f();
        Calib3d.projectPoints(objectPts3f, rVec, tVec, kMat, dMat, imagePts2f);

        debug("object in world coordinates", objectPts3f);
        debug("projection in camera", imagePts2f);

        Mat newRVec = new Mat();
        Mat newTVec = new Mat();
        // reverse the projection
        Calib3d.solvePnP(objectPts3f, imagePts2f, kMat, dMat, newRVec, newTVec);

        // show solvePnP actually reverses the projection.
        // these describe the world from the camera point of view:
        // first translate the world origin, then rotate the world.
        // rotation is correct to about 1e-6
        debug("actual camera rotation", rVec);
        debug("derived camera rotation", newRVec);
        // translation is correct to about 1e-6
        debug("actual camera translation", tVec);
        debug("derived camera translation", newTVec);

        Mat rotM = new Mat();
        Calib3d.Rodrigues(newRVec, rotM);
        debug("rotation matrix", rotM);

        // what is the camera pose from the world perspective?
        // (this would be given to the pose estimator)
        // world origin is at (4,-2,10) in camera coords
        // the world coord orientation is the same as the camera, so
        // camera rotation is (0, -1, 0) (just the opposite)
        Mat camRot = new Mat();
        Calib3d.Rodrigues(rotM.t(), camRot);
        debug("camera rotation", camRot);

        // camera origin is at roughly (6.5, 2, -8)
        Mat inv = new Mat();
        Core.gemm(rotM.t(), newTVec, -1.0, new Mat(), 0.0, inv);
        debug("camera position", inv);
    }

    /**
     * Generate an image representing the target, extract contours from it,
     * and then use {@link Calib3d#solvePnP()} to find the camera pose.
     */
    @Test
    public void testSolvePnPFromContours() {

        // these numbers are big because if they're 1 then the warped image is all
        // blurry.
        MatOfPoint3f objectPts3f = new MatOfPoint3f(
                new Point3(-20, -10, 0.0),
                new Point3(-20, 10, 0.0),
                new Point3(20, 10, 0.0),
                new Point3(20, -10, 0.0));
        debug("objectPts3f", objectPts3f);

        // rotate one radian
        Mat rVec = Mat.zeros(3, 1, CvType.CV_64F);
        rVec.put(0, 0, 0.0, 1.0, 0.0);

        // translate to the right, up, far
        Mat tVec = Mat.zeros(3, 1, CvType.CV_64F);
        tVec.put(0, 0, 10.0, -20.0, 70.0);

        Mat kMat = Mat.zeros(3, 3, CvType.CV_64F);
        kMat.put(0, 0,
                400.0, 0.0, 256.0,
                0.0, 400.0, 256.0,
                0.0, 0.0, 1.0);

        MatOfDouble dMat = new MatOfDouble(Mat.zeros(4, 1, CvType.CV_64F));

        // project the world into the camera plane
        MatOfPoint2f imagePts2f = new MatOfPoint2f();
        Calib3d.projectPoints(objectPts3f, rVec, tVec, kMat, dMat, imagePts2f);
        debug("imagePts2f", imagePts2f);

        // now find the warp transform from the pairs
        // i don't think getPerspectiveTransform understands negative numbers.
        MatOfPoint2f object2d = new MatOfPoint2f(
                new Point(0, 0),
                new Point(0, 20),
                new Point(40, 20),
                new Point(40, 0));

        Mat transformMat = Imgproc.getPerspectiveTransform(object2d, imagePts2f);

        // make an actual image of the target
        Size dsize = new Size(512, 512);
        Mat visionTarget = Mat.zeros(dsize, CvType.CV_8U);
        Imgproc.rectangle(visionTarget,
                new Point(0, 0),
                new Point(40, 20),
                new Scalar(255, 255, 255),
                Imgproc.FILLED);

        // apply the transform
        // this maps the corner of the the target to the center.

        Mat cameraView = Mat.zeros(dsize, CvType.CV_8U);
        Imgproc.warpPerspective(visionTarget, cameraView, transformMat, dsize);
        Imgcodecs.imwrite("C:\\Users\\joelt\\Desktop\\pics\\foo5.jpg", cameraView);

        // now find the vertices in the image
        List<MatOfPoint> contours = new ArrayList<>();
        Mat hierarchy = new Mat();
        Imgproc.findContours(cameraView,
                contours,
                hierarchy,
                Imgproc.RETR_LIST,
                Imgproc.CHAIN_APPROX_SIMPLE);
        // there's just one contour
        assertEquals(1, contours.size());

        Imgproc.drawContours(cameraView, contours, 0, new Scalar(255, 0, 0));
        Imgcodecs.imwrite("C:\\Users\\joelt\\Desktop\\pics\\foo6.jpg", cameraView);

        MatOfPoint2f curve = new MatOfPoint2f(contours.get(0).toArray());
        MatOfPoint2f approxCurve = new MatOfPoint2f();
        Imgproc.approxPolyDP(curve, approxCurve, 3, true);
        // contour is pretty close.
        debug("approx curve", approxCurve);
        // there are four points
        assertEquals(4, approxCurve.toList().size());

        // these points are not necessarily the same order as the object points.
        // how to associate the correct object point with the contour point?
        // start upper left, counterclockwise. since the rotation axis is actually
        // always Y, and the camera is always in front of the target, the left
        // point is always actualy on the left, or most negative, side, and the
        // upper one is above the lower one. isn't there some more clever
        // way to make use of these constraints? oh actually the rotation axis
        // is not always Y since the camera may be inclined.

        MatOfInt hull = new MatOfInt();
        // clockwise for convexhull is actually counterclockwise due to reversed axes
        // so this is the order
        Imgproc.convexHull(new MatOfPoint(approxCurve.toArray()), hull, true);
        debug("hull", hull);
        // need to find the first element (index)
        // upper left has min(x+y)
        Point upperLeftPoint = new Point(Double.MAX_VALUE, Double.MAX_VALUE);
        int idx = 0;
        List<Point> approxCurveList = approxCurve.toList();
        for (int i = 0; i < approxCurveList.size(); ++i) {
            Point p = approxCurveList.get(i);
            if (p.x + p.y < upperLeftPoint.x + upperLeftPoint.y) {
                upperLeftPoint = p;
                idx = i;
            }
        }
        debug("idx", idx);

        // put the idx'th element at zero
        Collections.rotate(approxCurveList, -idx);

        // ... aaand what's our position?
        Mat newRVec = new Mat(); // rVec.clone();
        Mat newTVec = new Mat(); // tVec.clone();
        MatOfPoint2f imagePoints = new MatOfPoint2f(approxCurveList.toArray(new Point[0]));
        debug("imagePoints", imagePoints);

        Calib3d.solvePnPRansac(objectPts3f, imagePoints, kMat, dMat,
                newRVec, newTVec, false,
                Calib3d.SOLVEPNP_SQPNP);
        // rotation is totally wrong, mostly pointing in z, just about pi/2.
        // maybe x and y are switched somehow.
        debug("original rvec", rVec);
        debug("new rvec", newRVec);
        // translation is pretty good
        debug("original tvec", tVec);
        debug("new tvec", newTVec);

        Mat rotM = new Mat();
        Calib3d.Rodrigues(newRVec, rotM);
        debug("rotation matrix", rotM);

        // camera rotation is just the reverse i.e. 1 radian the other way
        Mat camRot = new Mat();
        Calib3d.Rodrigues(rotM.t(), camRot);
        debug("camera rotation in world coords", camRot);

        // camera origin is at roughly (6.5, 2, -8)
        Mat inv = new Mat();
        Core.gemm(rotM.t(), newTVec, -1.0, new Mat(), 0.0, inv);
        debug("camera position in world coords", inv);

    }

    /**
     * Start with world coordinates, generate images, then generate poses.
     * seems like it works within ~4 in world units.
     */
    @Test
    public void testPoseFromImageFromWorldCoords() {
        // camera is at 2,0,-4, pointing 45 degrees to the left (which means negative
        // rotation)
        Mat worldRVec = Mat.zeros(3, 1, CvType.CV_64F);
        // worldRVec.put(0, 0, 0.0, -0.785398, 0.0);
        debug("worldRVec", worldRVec);

        Mat worldTVec = Mat.zeros(3, 1, CvType.CV_64F);
        worldTVec.put(0, 0, 20.0, 0.0, -40.0);
        debug("worldTVec", worldTVec);

        // derive camera transformations
        Mat worldRMat = new Mat();
        Calib3d.Rodrigues(worldRVec, worldRMat);

        // camera rotation is positive i.e. clockwise in these coords
        Mat camRVec = new Mat();
        Calib3d.Rodrigues(worldRMat.t(), camRVec);
        debug("camRVec", camRVec);

        Mat camTVec = new Mat();
        Core.gemm(worldRMat.t(), worldTVec, -1.0, new Mat(), 0.0, camTVec);
        debug("camTVec", camTVec);

        // now do the same thing as above
        MatOfPoint3f objectPts3f = new MatOfPoint3f(
                new Point3(-10, -10, 0.0),
                new Point3(-10, 10, 0.0),
                new Point3(10, 10, 0.0),
                new Point3(10, -10, 0.0));

        Mat kMat = Mat.zeros(3, 3, CvType.CV_64F);
        kMat.put(0, 0,
                400.0, 0.0, 256.0,
                0.0, 400.0, 256.0,
                0.0, 0.0, 1.0);

        MatOfDouble dMat = new MatOfDouble(Mat.zeros(4, 1, CvType.CV_64F));
        MatOfPoint2f imagePts2f = new MatOfPoint2f();
        Calib3d.projectPoints(objectPts3f, camRVec, camTVec, kMat, dMat, imagePts2f);

        // maybe clipping
        debug("imagePts2f", imagePts2f);

        // now find the warp transform from the pairs
        // i don't think getPerspectiveTransform understands negative numbers.
        MatOfPoint2f object2d = new MatOfPoint2f(
                new Point(0, 0),
                new Point(0, 20),
                new Point(20, 20),
                new Point(20, 0));

        Mat transformMat = Imgproc.getPerspectiveTransform(object2d, imagePts2f);
        // make an actual image of the target
        Size dsize = new Size(512, 512);
        Mat visionTarget = Mat.zeros(dsize, CvType.CV_8U);
        Imgproc.rectangle(visionTarget,
                new Point(0, 0),
                new Point(20, 20),
                new Scalar(255, 255, 255),
                Imgproc.FILLED);

        Mat cameraView = Mat.zeros(dsize, CvType.CV_8U);
        Imgproc.warpPerspective(visionTarget, cameraView, transformMat, dsize);
        Imgcodecs.imwrite("C:\\Users\\joelt\\Desktop\\pics\\foo7.jpg", cameraView);

        // now find the vertices in the image
        List<MatOfPoint> contours = new ArrayList<>();
        Mat hierarchy = new Mat();
        Imgproc.findContours(cameraView,
                contours,
                hierarchy,
                Imgproc.RETR_LIST,
                Imgproc.CHAIN_APPROX_SIMPLE);
        // there's just one contour
        assertEquals(1, contours.size());

        Imgproc.drawContours(cameraView, contours, 0, new Scalar(255, 0, 0));
        Imgcodecs.imwrite("C:\\Users\\joelt\\Desktop\\pics\\foo7.jpg", cameraView);

        MatOfPoint2f curve = new MatOfPoint2f(contours.get(0).toArray());
        MatOfPoint2f approxCurve = new MatOfPoint2f();
        Imgproc.approxPolyDP(curve, approxCurve, 3, true);
        // contour is pretty close.
        debug("approx curve", approxCurve);
        // there are four points
        assertEquals(4, approxCurve.toList().size());

        // these points are not necessarily the same order as the object points.
        // how to associate the correct object point with the contour point?
        // start upper left, counterclockwise. since the rotation axis is actually
        // always Y, and the camera is always in front of the target, the left
        // point is always actualy on the left, or most negative, side, and the
        // upper one is above the lower one. isn't there some more clever
        // way to make use of these constraints? oh actually the rotation axis
        // is not always Y since the camera may be inclined.

        MatOfInt hull = new MatOfInt();
        // clockwise for convexhull is actually counterclockwise due to reversed axes
        // so this is the order
        Imgproc.convexHull(new MatOfPoint(approxCurve.toArray()), hull, true);
        debug("hull", hull);
        // need to find the first element (index)
        // upper left has min(x+y)
        Point upperLeftPoint = new Point(Double.MAX_VALUE, Double.MAX_VALUE);
        int idx = 0;
        List<Point> approxCurveList = approxCurve.toList();
        for (int i = 0; i < approxCurveList.size(); ++i) {
            Point p = approxCurveList.get(i);
            if (p.x + p.y < upperLeftPoint.x + upperLeftPoint.y) {
                upperLeftPoint = p;
                idx = i;
            }
        }
        debug("idx", idx);

        // put the idx'th element at zero
        Collections.rotate(approxCurveList, -idx);

        // ... aaand what's our position?
        Mat newRVec = new Mat(); // rVec.clone();
        Mat newTVec = new Mat(); // tVec.clone();
        MatOfPoint2f imagePoints = new MatOfPoint2f(approxCurveList.toArray(new Point[0]));
        debug("imagePoints", imagePoints);

        Calib3d.solvePnPRansac(objectPts3f, imagePoints, kMat, dMat,
                newRVec, newTVec, false,
                Calib3d.SOLVEPNP_SQPNP);
        // rotation is totally wrong, mostly pointing in z, just about pi/2.
        // maybe x and y are switched somehow.
        debug("original rvec", camRVec);
        debug("new rvec", newRVec);
        // translation is pretty good
        debug("original tvec", camTVec);
        debug("new tvec", newTVec);

        Mat rotM = new Mat();
        Calib3d.Rodrigues(newRVec, rotM);

        // camera rotation is just the reverse i.e. 1 radian the other way
        Mat camRot = new Mat();
        Calib3d.Rodrigues(rotM.t(), camRot);
        debug("actual camera rotation in world coords", worldRVec);
        debug("camera rotation in world coords", camRot);

        // camera origin is at roughly (6.5, 2, -8)
        Mat inv = new Mat();
        Core.gemm(rotM.t(), newTVec, -1.0, new Mat(), 0.0, inv);
        debug("actual camera position in world coords", worldTVec);
        debug("camera position in world coords", inv);

        double norm = Core.norm(worldTVec, inv);
        debug("translation norm", norm);

        norm = Core.norm(worldRVec, camRot);
        debug("rotation norm", norm);
    }

    /**
     * Try many world locations; generate an image, extract pose from it.
     */
    @Test
    public void testSolvePnPGrid() {
        Size dsize = new Size(1920, 1080);
        double f = 600.0;
        Mat kMat = VisionUtil.makeIntrinsicMatrix(f, dsize);

        // don't forget to measure distortion in a real camera
        // Note: distortion confuses pnpransac, use normal pnp instead
        // a bit of barrel
        // MatOfDouble dMat = new MatOfDouble(Mat.zeros(4, 1, CvType.CV_64F));
        // dMat.put(0, 0, -0.05, 0.0, 0.0, 0.0);
        // this is from github.com/SharadRawat/pi_sensor_setup
        // MatOfDouble dMat = new MatOfDouble(Mat.zeros(5, 1, CvType.CV_64F));
        // dMat.put(0, 0, 0.159, -0.0661, -0.00570, 0.0117, -0.503);
        // try a less agro version of that
        MatOfDouble dMat = new MatOfDouble(Mat.zeros(5, 1, CvType.CV_64F));
        //
        //
        // // is distortion broken?
        //
        dMat.put(0, 0, -0.1, 0, 0, 0, 0);
        //
        //
        //
        // target is 0.4m wide, 0.1m high .
        double height = 0.1;
        double width = 0.4;
        MatOfPoint3f targetGeometryMeters = VisionUtil.makeTargetGeometry3f(width, height);

        // final double maxAbsErr = 0.5;
        final double dyWorld = 1.0; // say the camera is 1m below (+y) relative to the target
        final double tilt = 0.45; // camera tilts up
        final double pan = 0.0; // for now, straight ahead
        int idx = 0;
        System.out.println(
                "idx, dxWorld, dyWorld, dzWorld, dxCam, "
                        + "dyCam, dzCam, pan, tilt, 0.0, "
                        + "pdxworld, pdyworld, pdzworld, ppanCam, ptiltCam, "
                        + "pscrewCam, pdxCam, pdyCam, pdzCam, pdxCamDp, "
                        + "pdyCamDp, pdzCamDp, pdxWorldDp, pdyWorldDp, pdzWorldDp, "
                        + "ppanWorld, ptiltWorld, pscrewWorld");
        // FRC field is 8x16m, let's try for half-length and full-width i.e. 8x8
        for (double dzWorld = -10; dzWorld <= -1; dzWorld += 1.0) { // meters, start far, move closer
            for (double dxWorld = -5; dxWorld <= 5; dxWorld += 1.0) { // meters, start left, move right

                // for (double dz = -1; dz <= -1; dz += 1.0) {
                // for (double dx = 1; dx <= 1; dx += 1.0) {
                // right
                idx += 1;
                Mat cameraView = VisionUtil.makeImage(dxWorld, dyWorld, dzWorld, tilt, pan, kMat, dMat,
                        targetGeometryMeters, dsize);
                if (cameraView == null) {
                    debugmsg("no camera view");
                    continue;
                }
                Imgcodecs.imwrite(String.format("C:\\Users\\joelt\\Desktop\\pics\\target-%d-distorted.png", idx),
                        cameraView);

                //
                // manually undistort the camera view.
                Mat undistortedCameraView = new Mat();
                Calib3d.undistort(cameraView, undistortedCameraView, kMat, dMat);
                Imgcodecs.imwrite(String.format("C:\\Users\\joelt\\Desktop\\pics\\target-%d-undistorted.png", idx),
                        undistortedCameraView);

                // try removing the camera tilt and using the camera y to make fake points.

                // Mat homogeneousKMat = Mat.zeros(3, 4, CvType.CV_64F);
                // homogeneousKMat.put(0, 0,
                // 512.0, 0.0, dsize.width / 2,
                // 0.0, 512.0, dsize.height / 2, 0,
                // 0.0, 0.0, 1.0, 0.0);
                // Mat homogenousInvKMat = Mat.zeros(4, 3, CvType.CV_64F);
                // homogenousInvKMat.put(0, 0,
                // 1.0 / 512.0, 0.0, -dsize.width / (2 * 512.0),
                // 0.0, 1.0 / 512.0, -dsize.height / (2 * 512.0),
                // 0.0, 0.0, 0.0,
                // 0.0, 0.0, 1.0);
                // Mat homogeneousUntilt = Mat.zeros(4, 4, CvType.CV_64F);
                // homogeneousUntilt.put(0, 0,
                // 1.0, 0.0, 0.0, 0.0,
                // 0.0, Math.cos(-tilt), -Math.sin(-tilt), 0.0,
                // 0.0, Math.sin(-tilt), Math.cos(-tilt), 0.0,
                // 0.0, 0.0, 0.0, 1.0);

                Mat invKMat = kMat.inv();
                Mat unTiltV = Mat.zeros(3, 1, CvType.CV_64F);
                unTiltV.put(0, 0, tilt, 0.0, 0.0);
                Mat unTiltM = new Mat();
                Calib3d.Rodrigues(unTiltV, unTiltM);

                Mat result = new Mat();
                // Core.gemm(homogeneousUntilt, homogenousInvKMat, 1.0, new Mat(), 0.0, result);
                // Core.gemm(homogeneousKMat, result, 1.0, new Mat(), 0.0, result);
                Core.gemm(unTiltM, invKMat, 1.0, new Mat(), 0.0, result);
                // this doesn't work because translation is range-dependent
                // Mat translation = Mat.zeros(3, 3, CvType.CV_64F);
                // translation.put(0, 0,
                // 1.0, 0.0, 0.0,
                // 0.0, 1.0, dy,
                // 0.0, 0.0, 1.0);
                // Core.gemm(translation, result, 1.0, new Mat(), 0.0, result);

                // make a tall camera
                //
                Size tallSize = new Size(1920, 2160);
                Mat tallKMat = VisionUtil.makeIntrinsicMatrix(f, tallSize);
                //
                //

                // Core.gemm(kMat, result, 1.0, new Mat(), 0.0, result);
                Core.gemm(tallKMat, result, 1.0, new Mat(), 0.0, result);
                debug("result", result);

                // Mat untiltedCameraView = Mat.zeros(dsize, CvType.CV_8UC3);
                Mat untiltedCameraView = Mat.zeros(tallSize, CvType.CV_8UC3);

                // Imgproc.warpPerspective(undistortedCameraView, untiltedCameraView, result,
                // dsize);
                Imgproc.warpPerspective(undistortedCameraView, untiltedCameraView, result, tallSize);

                Imgcodecs.imwrite(String.format("C:\\Users\\joelt\\Desktop\\pics\\target-%d-raw.png", idx),
                        untiltedCameraView);

                MatOfPoint2f imagePoints = VisionUtil.getImagePoints(idx, untiltedCameraView);
                if (imagePoints == null) {
                    debugmsg("no image points");
                    continue;
                }

                debug("imagePoints", imagePoints);

                //
                // // try homography.
                // // ok the homography approach isn't any better
                // // there's still no way to constrain it
                MatOfPoint2f targetImageGeometry = VisionUtil.makeTargetImageGeometryPixels(targetGeometryMeters, 1000);

                Mat H = Calib3d.findHomography(targetImageGeometry, imagePoints);
                debug("H", H);
                List<Mat> rotations = new ArrayList<Mat>();
                List<Mat> translations = new ArrayList<Mat>();
                List<Mat> normals = new ArrayList<Mat>();
                Calib3d.decomposeHomographyMat(H, kMat, rotations, translations, normals);
                for (Mat m : translations) {
                    debug("m", m);
                }

                // targetGeometryMeters is four points
                // add two more below, at the y of the camera, which is the horizon
                // in the untilted view.

                List<Point3> point3List = new ArrayList<Point3>(targetGeometryMeters.toList());
                List<Point> pointList = new ArrayList<Point>(imagePoints.toList());

                Point upperLeft = imagePoints.toList().get(0);
                Point lowerLeft = imagePoints.toList().get(1);
                Point lowerRight = imagePoints.toList().get(2);
                Point upperRight = imagePoints.toList().get(3);
                // make the rectangle just a projection of the top edge.
                Rect imageRect = new Rect(upperLeft, new Size(upperRight.x - upperLeft.x, lowerRight.y - upperRight.y));
                // this isn't exactly in the same place as the top edge
                // Rect imageRect = Imgproc.boundingRect(imagePoints);

                //
                // mirror the whole thing below the horizon. note the ordering
                point3List.add(new Point3(-width / 2, height / 2 + 2 * dyWorld, 0.0));
                point3List.add(new Point3(-width / 2, -height / 2 + 2 * dyWorld, 0.0));
                point3List.add(new Point3(width / 2, -height / 2 + 2 * dyWorld, 0.0));
                point3List.add(new Point3(width / 2, height / 2 + 2 * dyWorld, 0.0));
                pointList.add(new Point(upperLeft.x, tallSize.height - upperLeft.y));
                pointList.add(new Point(lowerLeft.x, tallSize.height - lowerLeft.y));
                pointList.add(new Point(lowerRight.x, tallSize.height - lowerRight.y));
                pointList.add(new Point(upperRight.x, tallSize.height - upperRight.y));

                //
                //
                if (imageRect.x - imageRect.width > 0) {
                    // wider == better yaw signal
                    point3List.add(new Point3(-3 * width / 2, dyWorld, 0.0));
                    pointList.add(new Point(imageRect.x - imageRect.width, tallSize.height / 2));
                }
                if (imageRect.x - 2 * imageRect.width > 0) {
                    // wider == better yaw signal
                    point3List.add(new Point3(-5 * width / 2, dyWorld, 0.0));
                    pointList.add(new Point(imageRect.x - 2 * imageRect.width, tallSize.height / 2));
                }

                // also extend the upper.
                // points are upper-left first and then clockwise
                // so...

                double upperLeftX = upperLeft.x;
                double upperLeftY = upperLeft.y;

                double upperRightX = upperRight.x;
                double upperRightY = upperRight.y;
                double xDel = upperRightX - upperLeftX;
                double yDel = upperRightY - upperLeftY;
                if (upperLeftX - xDel > 0 && upperLeftY - yDel > 0) {
                    point3List.add(new Point3(-3 * width / 2, -height / 2, 0.0));
                    pointList.add(new Point(upperLeftX - xDel, upperLeftY - yDel));
                }
                if (upperRightX + xDel < tallSize.width && upperRightY + yDel < tallSize.height) {
                    point3List.add(new Point3(3 * width / 2, -height / 2, 0.0));
                    pointList.add(new Point(upperRightX + xDel, upperRightY + yDel));
                }
                if (upperLeftX - 2 * xDel > 0 && upperLeftY - 2 * yDel > 0) {
                    point3List.add(new Point3(-5 * width / 2, -height / 2, 0.0));
                    pointList.add(new Point(upperLeftX - 2 * xDel, upperLeftY - 2 * yDel));
                }
                if (upperRightX + 2 * xDel < tallSize.width && upperRightY + 2 * yDel < tallSize.height) {
                    point3List.add(new Point3(5 * width / 2, -height / 2, 0.0));
                    pointList.add(new Point(upperRightX + 2 * xDel, upperRightY + 2 * yDel));
                }

                // these are guaranteed to be in frame
                point3List.add(new Point3(-width / 2, dyWorld, 0.0));
                pointList.add(new Point(imageRect.x, tallSize.height / 2));
                point3List.add(new Point3(width / 2, dyWorld, 0.0));
                pointList.add(new Point(imageRect.br().x, tallSize.height / 2));

                if (imageRect.br().x + imageRect.width < tallSize.width) {
                    // wider == better yaw signal
                    point3List.add(new Point3(3 * width / 2, dyWorld, 0.0));
                    pointList.add(new Point(imageRect.br().x + imageRect.width, tallSize.height / 2));
                }
                if (imageRect.br().x + 2 * imageRect.width < tallSize.width) {
                    // wider == better yaw signal
                    point3List.add(new Point3(5 * width / 2, dyWorld, 0.0));
                    pointList.add(new Point(imageRect.br().x + 2 * imageRect.width, tallSize.height / 2));
                }
                // more points at the horizon make pnp pay more attention to it
                point3List.add(new Point3(0, dyWorld, 0.0));
                pointList.add(new Point(imageRect.x + imageRect.width / 2, tallSize.height / 2));

                MatOfPoint3f expandedTargetGeometryMeters = new MatOfPoint3f(point3List.toArray(new Point3[0]));
                debug("expandedTargetGeometryMeters", expandedTargetGeometryMeters);
                MatOfPoint2f expandedImagePoints = new MatOfPoint2f(pointList.toArray(new Point[0]));

                debug("expandedImagePoints", expandedImagePoints);
                //
                // find the pose using solvepnp. this sucks because the target is small relative
                // to the distances.
                //
                Mat newCamRVec = new Mat();
                Mat newCamTVec = new Mat();

                Calib3d.solvePnP(expandedTargetGeometryMeters, expandedImagePoints, tallKMat,
                        new MatOfDouble(), newCamRVec, newCamTVec, false,
                        Calib3d.SOLVEPNP_ITERATIVE);
                // large reprojection error == there are no outliers
                // Calib3d.solvePnPRansac(expandedTargetGeometryMeters, expandedImagePoints,
                // tallKMat,
                // new MatOfDouble(), newCamRVec, newCamTVec, false, 2000, 100);

                // Calib3d.solvePnPRefineLM(expandedTargetGeometryMeters, expandedImagePoints,
                // tallKMat, new MatOfDouble(),
                // newCamRVec, newCamTVec, new TermCriteria(TermCriteria.EPS, 10000, 0.001));
                // Calib3d.solvePnPRefineVVS(expandedTargetGeometryMeters, expandedImagePoints,
                // tallKMat,
                // new MatOfDouble(), newCamRVec, newCamTVec, new TermCriteria(TermCriteria.EPS,
                // 1000, 0.001),
                // 1);

                // now derive world coords
                // for distant orthogonal targets the R is quite uncertain.
                Mat newCamRMat = new Mat();
                Calib3d.Rodrigues(newCamRVec, newCamRMat);
                Mat newWorldRMat = newCamRMat.t();

                //
                // draw the target points on the camera view to see where we think they are
                //

                MatOfPoint2f skewedImagePts2f = new MatOfPoint2f();
                Mat jacobian = new Mat();
                Calib3d.projectPoints(expandedTargetGeometryMeters, newCamRVec,
                        newCamTVec, tallKMat, new MatOfDouble(), skewedImagePts2f, jacobian);
                // Mat dpdrot = jacobian.colRange(0, 3);
                Mat dpdt = jacobian.colRange(3, 6);
                // calculate the stdev dtdp for each dimension:
                double pdxCamDp = 0;
                double pdyCamDp = 0;
                double pdzCamDp = 0;
                double pdxWorldDp = 0;
                double pdyWorldDp = 0;
                double pdzWorldDp = 0;
                // guess stdev in pixels :-)
                final Mat dp = Mat.zeros(2, 1, CvType.CV_64F);
                dp.put(0, 0, 3, 3);
                debug("dp", dp);
                for (int i = 0; i < dpdt.rows(); i += 2) {
                    Mat pointDpdt = dpdt.rowRange(i, i + 2);
                    debug("dpdt", pointDpdt);
                    Mat dtdp = new Mat();
                    Core.invert(pointDpdt, dtdp, Core.DECOMP_SVD);

                    debug("dtdp", dtdp);
                    Mat dt = new Mat();
                    Core.gemm(dtdp, dp, 1.0, new Mat(), 0.0, dt);
                    pdxCamDp += (dt.get(0, 0)[0] * dt.get(0, 0)[0]);
                    pdyCamDp += (dt.get(1, 0)[0] * dt.get(1, 0)[0]);
                    pdzCamDp += (dt.get(2, 0)[0] * dt.get(2, 0)[0]);
                    // ok now find the world-transformed dt.
                    // this is the jacobian of the transform (which is just the
                    // transform itself), evaluated at the predicted camt.
                    // ... this should be a 3x3 not a 3x1, grrr
                    Mat Jworld = new Mat();
                    Core.gemm(newWorldRMat, newCamTVec, -1.0, new Mat(), 0.0, Jworld);
                    debug("Jworld", Jworld);
                    Mat dtWorld = new Mat();
                    Core.gemm(Jworld.t(), dt, -1.0, new Mat(), 0.0, dtWorld);
                    debug("dtWorld", dtWorld);

                    // pdxWorldDp += (dtWorld.get(0, 0)[0] * dtWorld.get(0, 0)[0]);
                    // pdyWorldDp += (dtWorld.get(1, 0)[0] * dtWorld.get(1, 0)[0]);
                    // pdzWorldDp += (dtWorld.get(2, 0)[0] * dtWorld.get(2, 0)[0]);
                }
                pdxCamDp /= dpdt.rows() / 2;
                pdyCamDp /= dpdt.rows() / 2;
                pdzCamDp /= dpdt.rows() / 2;
                pdxCamDp = Math.sqrt(pdxCamDp);
                pdyCamDp = Math.sqrt(pdyCamDp);
                pdzCamDp = Math.sqrt(pdzCamDp);
                pdxWorldDp /= dpdt.rows() / 2;
                pdyWorldDp /= dpdt.rows() / 2;
                pdzWorldDp /= dpdt.rows() / 2;
                pdxWorldDp = Math.sqrt(pdxWorldDp);
                pdyWorldDp = Math.sqrt(pdyWorldDp);
                pdzWorldDp = Math.sqrt(pdzWorldDp);

                // points projected from pnp
                for (Point pt : skewedImagePts2f.toList()) {
                    Imgproc.circle(untiltedCameraView,
                            new Point(pt.x, pt.y),
                            2,
                            new Scalar(0, 0, 255),
                            Imgproc.FILLED);
                }
                // points found in the image
                for (Point pt : expandedImagePoints.toList()) {
                    Imgproc.circle(untiltedCameraView,
                            new Point(pt.x, pt.y),
                            6,
                            new Scalar(0, 255, 0),
                            1);
                }

                // report on the predictions
                // first derive cam coords, because cam coords errors are easier to understand

                double pdxCam = newCamTVec.get(0, 0)[0];
                double pdyCam = newCamTVec.get(1, 0)[0];
                double pdzCam = newCamTVec.get(2, 0)[0];

                // are world rotations interesting?
                Mat eulerWorld = VisionUtil.rotm2euler(newCamRMat);
                double ptiltWorld = eulerWorld.get(0, 0)[0];
                double ppanWorld = eulerWorld.get(1, 0)[0];
                double pscrewWorld = eulerWorld.get(2, 0)[0];

                // what are the actual camera translations
                // note this ignores camera tilt, because we magically detilt above
                Mat dWorld = Mat.zeros(3, 1, CvType.CV_64F);
                dWorld.put(0, 0, dxWorld, dyWorld, dzWorld);

                Mat rCam = Mat.zeros(3, 1, CvType.CV_64F);
                rCam.put(0, 0, 0.0, pan, 0.0);
                Mat rCamM = new Mat();
                Calib3d.Rodrigues(rCam, rCamM);
                Mat dCam = new Mat();
                Core.gemm(rCamM, dWorld, -1.0, new Mat(), 0.0, dCam);

                double dxCam = dCam.get(0, 0)[0];
                double dyCam = dCam.get(1, 0)[0];
                double dzCam = dCam.get(2, 0)[0];

                Mat newWorldTVec = new Mat();
                Core.gemm(newWorldRMat, newCamTVec, -1.0, new Mat(), 0.0, newWorldTVec);
                // predictions in world coords
                // NOTE: error in R becomes error in X and Y.pnp
                double pdxworld = newWorldTVec.get(0, 0)[0];
                double pdyworld = newWorldTVec.get(1, 0)[0];
                double pdzworld = newWorldTVec.get(2, 0)[0];
                // the interesting aspect of rotation is the *camera* rotation
                Mat eulerCam = VisionUtil.rotm2euler(newCamRMat);
                double ptiltCam = eulerCam.get(0, 0)[0];
                double ppanCam = eulerCam.get(1, 0)[0];
                double pscrewCam = eulerCam.get(2, 0)[0];
                // double xAbsErr = Math.abs(dxWorld - pdxworld);
                // double yAbsErr = Math.abs(dyWorld - pdyworld);
                // double zAbsErr = Math.abs(dzWorld - pdzworld);
                // if (xAbsErr < maxAbsErr && yAbsErr < maxAbsErr && zAbsErr < maxAbsErr)
                // continue;
                // this is a bad case, so store it

                Imgcodecs.imwrite(String.format("C:\\Users\\joelt\\Desktop\\pics\\target-%d-annotated.png", idx),
                        untiltedCameraView);
                System.out.printf(
                        "%d, %f, %f, %f, %f, "
                                + " %f, %f, %f, %f, %f, "
                                + " %f, %f, %f, %f, %f, "
                                + " %f, %f, %f, %f, %f, "
                                + " %f, %f, %f, %f, %f, "
                                + " %f, %f, %f\n",
                        idx, dxWorld, dyWorld, dzWorld, dxCam,
                        dyCam, dzCam, pan, tilt, 0.0,
                        pdxworld, pdyworld, pdzworld, ppanCam, ptiltCam,
                        pscrewCam, pdxCam, pdyCam, pdzCam, pdxCamDp,
                        pdyCamDp, pdzCamDp, pdxWorldDp, pdyWorldDp, pdzWorldDp,
                        ppanWorld, ptiltWorld, pscrewWorld);
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