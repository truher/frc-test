import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.junit.Test;
import org.opencv.calib3d.Calib3d;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfByte;
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

public class TestCV {
    public static final double DELTA = 0.00001;

    public TestCV() {
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
    }

    @Test
    public void testNothing() {
        assertTrue(true);
    }

    // @Test
    public void testCombiningRotations() {
        Mat pan = Mat.zeros(3, 1, CvType.CV_64F);
        pan.put(0, 0, 0.0, -0.5, 0.0); // pan to right, world to left, so negative
        Mat tilt = Mat.zeros(3, 1, CvType.CV_64F);
        tilt.put(0, 0, -0.5, 0.0, 0.0); // tilt up, world down, so negative
        // System.out.println("pan vector");
        // System.out.println(pan.dump());
        // System.out.println("tilt vector");
        // System.out.println(tilt.dump());
        // pan first to keep horizon horizontal
        Mat productV = VisionUtil.combineRotations(pan, tilt);
        // System.out.println("product vector");
        // System.out.println(productV.dump());
        assertEquals(-0.48945, productV.get(0, 0)[0], DELTA);
        assertEquals(-0.48945, productV.get(1, 0)[0], DELTA);
        assertEquals(0.12498, productV.get(2, 0)[0], DELTA);
    }

    // @Test
    public void testRotm2euler() {
        Mat pan = Mat.zeros(3, 1, CvType.CV_64F);
        pan.put(0, 0, 0.0, -0.7854, 0.0); // pan 45deg to right, world to left, so
        // negative
        Mat tilt = Mat.zeros(3, 1, CvType.CV_64F);
        tilt.put(0, 0, -0.7854, 0.0, 0.0); // tilt 45deg up, world down, so negative
        // System.out.println("pan vector");
        // System.out.println(pan.dump());
        // System.out.println("tilt vector");
        // System.out.println(tilt.dump());
        // pan first to keep horizon horizontal
        Mat productV = VisionUtil.combineRotations(pan, tilt);
        Mat productM = new Mat();
        Calib3d.Rodrigues(productV, productM);

        Mat r = productM.t();

        // System.out.println("result");
        // System.out.println(r.dump());

        Mat euler = VisionUtil.rotm2euler(r);

        // System.out.println("euler radians");
        // System.out.println(euler.dump());

        assertEquals(0.7854, euler.get(0, 0)[0], DELTA); // upward tilt
        assertEquals(0.7854, euler.get(1, 0)[0], DELTA); // rightward pan
        assertEquals(0, euler.get(2, 0)[0], DELTA); // no rotation around the camera axis
    }

    /**
     * figure out how to read a file.
     */
    // @Test
    public void testFile() throws Exception {

        String foo = new String(getClass().getClassLoader().getResourceAsStream("readme.md").readAllBytes());
        assertEquals("hello", foo);
    }

    /**
     * figure out how to read an image file and find contours in it
     */
    // @Test
    public void testImage() throws Exception {

        // (100,100), (200,200)
        // (300,300), (400,400)
        byte[] imgbytes = getClass().getClassLoader().getResourceAsStream("two_squares.png").readAllBytes();
        Mat matrix = Imgcodecs.imdecode(new MatOfByte(imgbytes), Imgcodecs.IMREAD_UNCHANGED);
        assertEquals(512, matrix.rows());
        assertEquals(512, matrix.cols());

        List<MatOfPoint> contours = new ArrayList<>();
        Mat hierarchy = new Mat();
        // Imgproc.RETR_CCOMP == 2 level hierarchy
        // Imgproc.RETR_EXTERNAL == outer contours only
        // Imgproc.RETR_LIST == flat list
        Imgproc.findContours(matrix, // input
                contours, // output contours
                hierarchy, // output topology
                Imgproc.RETR_TREE, // return full hierarchy.
                Imgproc.CHAIN_APPROX_SIMPLE); // rectangular
        // contour == rectangle with all four corners.

        assertEquals(2, contours.size());
        assertEquals(4, contours.get(0).rows());
        assertEquals(1, contours.get(0).cols());
        assertEquals(4, contours.get(0).toArray().length, DELTA);
        assertEquals(300, contours.get(0).toArray()[0].x, DELTA);
        assertEquals(300, contours.get(0).toArray()[0].y, DELTA);
        assertEquals(300, contours.get(0).toArray()[1].x, DELTA);
        assertEquals(400, contours.get(0).toArray()[1].y, DELTA);
        assertEquals(400, contours.get(0).toArray()[2].x, DELTA);
        assertEquals(400, contours.get(0).toArray()[2].y, DELTA);
        assertEquals(400, contours.get(0).toArray()[3].x, DELTA);
        assertEquals(300, contours.get(0).toArray()[3].y, DELTA);
        assertEquals(4, contours.get(1).rows());
        assertEquals(1, contours.get(1).cols());
        assertEquals(4, contours.get(1).toArray().length, DELTA);
        assertEquals(100, contours.get(1).toArray()[0].x, DELTA);
        assertEquals(100, contours.get(1).toArray()[0].y, DELTA);
        assertEquals(100, contours.get(1).toArray()[1].x, DELTA);
        assertEquals(200, contours.get(1).toArray()[1].y, DELTA);
        assertEquals(200, contours.get(1).toArray()[2].x, DELTA);
        assertEquals(200, contours.get(1).toArray()[2].y, DELTA);
        assertEquals(200, contours.get(1).toArray()[3].x, DELTA);
        assertEquals(100, contours.get(1).toArray()[3].y, DELTA);
    }

    // @Test
    public void testGeneratedImage() throws Exception {
        Mat matrix = Mat.zeros(512, 512, CvType.CV_8U);
        Imgproc.rectangle(matrix,
                new Point(100, 200),
                new Point(300, 400),
                new Scalar(255, 255, 255),
                Imgproc.FILLED);
        // look at it. Imgcodecs.imwrite("C:\\Users\\joelt\\Desktop\\pics\\foo.jpg",
        // matrix);
        List<MatOfPoint> contours = new ArrayList<>();
        Mat hierarchy = new Mat();
        Imgproc.findContours(matrix, contours, hierarchy, Imgproc.RETR_LIST, Imgproc.CHAIN_APPROX_SIMPLE);
        assertEquals(1, contours.size()); // just the one rectangle
        assertEquals(4, contours.get(0).rows());
        assertEquals(1, contours.get(0).cols());
        assertEquals(4, contours.get(0).toArray().length, DELTA);
        assertEquals(100, contours.get(0).toArray()[0].x, DELTA);
        assertEquals(200, contours.get(0).toArray()[0].y, DELTA);
        assertEquals(100, contours.get(0).toArray()[1].x, DELTA);
        assertEquals(400, contours.get(0).toArray()[1].y, DELTA);
        assertEquals(300, contours.get(0).toArray()[2].x, DELTA);
        assertEquals(400, contours.get(0).toArray()[2].y, DELTA);
        assertEquals(300, contours.get(0).toArray()[3].x, DELTA);
        assertEquals(200, contours.get(0).toArray()[3].y, DELTA);
    }

    // @Test
    public void testGeometry() {
        // this is an example that illustrates a bug, apparently fixed.
        // three points in a line
        MatOfPoint3f objectPts3f = new MatOfPoint3f(
                new Point3(0.0, 1.0, 1.0),
                new Point3(0.0, 1.0, 5.0),
                new Point3(0.0, 1.0, 10.0));
        MatOfPoint2f imagePts2f = new MatOfPoint2f();

        // no rotation, no translation, means camera is at the world origin
        // pointing along z
        Mat rVec = Mat.zeros(3, 1, CvType.CV_64F);
        Mat tVec = Mat.zeros(3, 1, CvType.CV_64F);

        // "camera matrix" is about the lens:
        // "focal length" is 100
        // "optical center" is (0,0).
        Mat kMat = Mat.zeros(3, 3, CvType.CV_64F);
        kMat.put(0, 0,
                100.0, 0.0, 0.0,
                0.0, 100.0, 0.0,
                0.0, 0.0, 1.0);
        // no distortion
        MatOfDouble dMat = new MatOfDouble(Mat.zeros(4, 1, CvType.CV_64F));
        Calib3d.projectPoints(objectPts3f, rVec, tVec, kMat, dMat, imagePts2f);
        assertEquals(0, imagePts2f.toList().get(0).x, DELTA);
        assertEquals(100, imagePts2f.toList().get(0).y, DELTA);
        assertEquals(0, imagePts2f.toList().get(1).x, DELTA);
        assertEquals(20, imagePts2f.toList().get(1).y, DELTA);
        assertEquals(0, imagePts2f.toList().get(2).x, DELTA);
        assertEquals(10, imagePts2f.toList().get(2).y, DELTA);

        System.out.println(objectPts3f.toList());
        System.out.println(imagePts2f.toList());
    }

    // @Test
    public void testGeneratedGeometry() {
        MatOfPoint3f objectPts3f = new MatOfPoint3f(
                new Point3(0.0, 1.0, 1.0),
                new Point3(0.0, 1.0, 5.0),
                new Point3(0.0, 1.0, 10.0));
        MatOfPoint2f imagePts2f = new MatOfPoint2f();

        // no rotation, no translation, means camera is at the world origin
        // pointing along z
        Mat rVec = Mat.zeros(3, 1, CvType.CV_64F);
        Mat tVec = Mat.zeros(3, 1, CvType.CV_64F);
        tVec.put(0, 0,
                0.0, 0.0, 0.0);

        // "camera matrix" is about the lens:
        // "focal length" is 100
        // "optical center" is (0,0).
        Mat kMat = Mat.zeros(3, 3, CvType.CV_64F);
        kMat.put(0, 0,
                100.0, 0.0, 0.0,
                0.0, 100.0, 0.0,
                0.0, 0.0, 1.0);
        // no distortion
        MatOfDouble dMat = new MatOfDouble(Mat.zeros(4, 1, CvType.CV_64F));
        Calib3d.projectPoints(objectPts3f, rVec, tVec, kMat, dMat, imagePts2f);
        assertEquals(0, imagePts2f.toList().get(0).x, DELTA);
        assertEquals(100, imagePts2f.toList().get(0).y, DELTA);
        assertEquals(0, imagePts2f.toList().get(1).x, DELTA);
        assertEquals(20, imagePts2f.toList().get(1).y, DELTA);
        assertEquals(0, imagePts2f.toList().get(2).x, DELTA);
        assertEquals(10, imagePts2f.toList().get(2).y, DELTA);

        System.out.println(objectPts3f.toList());
        System.out.println(imagePts2f.toList());

        int rows = 512;
        int cols = 512;
        Mat matrix = Mat.zeros(rows, cols, CvType.CV_8U);
        for (Point pt : imagePts2f.toList()) {
            Imgproc.circle(matrix,
                    new Point(pt.x + rows / 2, pt.y + cols / 2),
                    10,
                    new Scalar(255, 255, 255),
                    Imgproc.FILLED);
        }

        Imgcodecs.imwrite("C:\\Users\\joelt\\Desktop\\pics\\foo.jpg", matrix);

    }

    // @Test
    public void testGeneratedGeometry2() {
        MatOfPoint3f objectPts3f = new MatOfPoint3f(
                // outer
                new Point3(1.1, 1.1, 10.0),
                new Point3(1.1, -1.1, 10.0),
                new Point3(-1.1, -1.1, 10.0),
                new Point3(-1.1, 1.1, 10.0),
                // inner
                new Point3(1.0, 1.0, 10.0),
                new Point3(1.0, -1.0, 10.0),
                new Point3(-1.0, -1.0, 10.0),
                new Point3(-1.0, 1.0, 10.0));
        MatOfPoint2f imagePts2f = new MatOfPoint2f();

        // no rotation, no translation, means camera is at the world origin
        // pointing along z
        // rotation vector is along the rotation axis,
        // magnitude is rotation in radians
        // ok i was wrong before, this is rotating the world at the camera center,
        // not the translated world center.
        // Mat rVec = Mat.zeros(3, 1, CvType.CV_64F);
        Mat rVec = Mat.zeros(3, 1, CvType.CV_64F);
        rVec.put(0, 0,
                0.0, 0.78, 0.0);
        // 0.0, 0.1, 0.0);
        Mat tVec = Mat.zeros(3, 1, CvType.CV_64F);
        tVec.put(0, 0,
                4.0, -2.0, 10.0);
        // 4.0, -2.0, 10.0);

        // "camera matrix" is about the lens:
        // "focal length" is 100
        // "optical center" is (0,0).
        Mat kMat = Mat.zeros(3, 3, CvType.CV_64F);
        // camera matrix includes where the camera center is (cx, cy)
        // in *image coordinates* (i.e. *pixels*)
        // and focal length (fx, fy)
        kMat.put(0, 0,
                200.0, 0.0, 256.0,
                0.0, 200.0, 256.0,
                0.0, 0.0, 1.0);
        // no distortion
        MatOfDouble dMat = new MatOfDouble(Mat.zeros(4, 1, CvType.CV_64F));
        Calib3d.projectPoints(objectPts3f, rVec, tVec, kMat, dMat, imagePts2f);
        // assertEquals(2, imagePts2f.toList().size());
        // assertEquals(40, imagePts2f.toList().get(0).x, DELTA);
        // assertEquals(0, imagePts2f.toList().get(0).y, DELTA);
        // assertEquals(-40, imagePts2f.toList().get(1).x, DELTA);
        // assertEquals(0, imagePts2f.toList().get(1).y, DELTA);

        System.out.println(objectPts3f.toList());
        System.out.println(imagePts2f.toList());

        // blank image
        int rows = 512;
        int cols = 512;
        Mat matrix = Mat.zeros(rows, cols, CvType.CV_8U);
        // this is wrong, lines are straight.
        Imgproc.fillPoly(matrix,
                List.of(new MatOfPoint(imagePts2f.toList().subList(0, 4).toArray(new Point[0]))),
                new Scalar(255, 255, 255),
                Imgproc.LINE_8, 0,
                // new Point(rows / 2, cols / 2));
                new Point());
        Imgproc.fillPoly(matrix,
                List.of(new MatOfPoint(imagePts2f.toList().subList(4, 8).toArray(new Point[0]))),
                new Scalar(0, 0, 0),
                Imgproc.LINE_8, 0,
                // new Point(rows / 2, cols / 2));
                new Point());

        Imgcodecs.imwrite("C:\\Users\\joelt\\Desktop\\pics\\foo2.jpg", matrix);

    }

    /**
     * this takes some 3d points and projects them into a camera, then reverses
     * to get the pose of the camera.
     */
    // @Test
    public void testGeneratedGeometry3() {
        // known "world" geometry
        // MatOfPoint3f objectPts3f = new MatOfPoint3f(
        // // outer
        // new Point3(1.1, 1.1, 0.0),
        // new Point3(1.1, -1.1, 0.0),
        // new Point3(-1.1, -1.1, 0.0),
        // new Point3(-1.1, 1.1, 0.0),
        // // inner
        // new Point3(1.0, 1.0, 0.0),
        // new Point3(1.0, -1.0, 0.0),
        // new Point3(-1.0, -1.0, 0.0),
        // new Point3(-1.0, 1.0, 0.0));
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

        System.out.println("object in world coordinates");
        System.out.println(objectPts3f.toList());
        System.out.println("projection in camera");
        System.out.println(imagePts2f.toList());

        Mat newRVec = new Mat();
        Mat newTVec = new Mat();
        // reverse the projection
        Calib3d.solvePnP(objectPts3f, imagePts2f, kMat, dMat, newRVec, newTVec);

        // show solvePnP actually reverses the projection.
        // these describe the world from the camera point of view:
        // first translate the world origin, then rotate the world.
        // rotation is correct to about 1e-6
        System.out.println("actual camera rotation");
        System.out.println(rVec.dump());
        System.out.println("derived camera rotation");
        System.out.println(newRVec.dump());
        // translation is correct to about 1e-6
        System.out.println("actual camera translation");
        System.out.println(tVec.dump());
        System.out.println("derived camera translation");
        System.out.println(newTVec.dump());

        Mat rotM = new Mat();
        Calib3d.Rodrigues(newRVec, rotM);
        System.out.println("rotation matrix");
        System.out.println(rotM.dump());

        // what is the camera pose from the world perspective?
        // (this would be given to the pose estimator)
        // world origin is at (4,-2,10) in camera coords
        // the world coord orientation is the same as the camera, so
        // camera rotation is (0, -1, 0) (just the opposite)
        Mat camRot = new Mat();
        Calib3d.Rodrigues(rotM.t(), camRot);
        System.out.println("camera rotation");
        System.out.println(camRot.dump());

        // camera origin is at roughly (6.5, 2, -8)
        Mat inv = new Mat();
        Core.gemm(rotM.t(), newTVec, -1.0, new Mat(), 0.0, inv);
        System.out.println("camera position");
        System.out.println(inv.dump());
    }

    /**
     * this tries to do the same as above but with actual image warping.
     */
    // @Test
    public void testPoseFromImage() {

        // these numbers are big because if they're 1 then the warped image is all
        // blurry.
        MatOfPoint3f objectPts3f = new MatOfPoint3f(
                new Point3(-20, -10, 0.0),
                new Point3(-20, 10, 0.0),
                new Point3(20, 10, 0.0),
                new Point3(20, -10, 0.0));
        System.out.println("objectPts3f");
        System.out.println(objectPts3f.dump());

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
        System.out.println("imagePts2f");
        System.out.println(imagePts2f.dump());

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
        System.out.println("approx curve");
        System.out.println(approxCurve.dump());
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
        System.out.println("hull");
        System.out.println(hull.dump());
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
        System.out.println("idx");
        System.out.println(idx);

        // put the idx'th element at zero
        Collections.rotate(approxCurveList, -idx);

        // ... aaand what's our position?
        Mat newRVec = new Mat(); // rVec.clone();
        Mat newTVec = new Mat(); // tVec.clone();
        MatOfPoint2f imagePoints = new MatOfPoint2f(approxCurveList.toArray(new Point[0]));
        System.out.println("imagePoints");
        System.out.println(imagePoints.dump());

        Calib3d.solvePnPRansac(objectPts3f, imagePoints, kMat, dMat,
                newRVec, newTVec, false,
                Calib3d.SOLVEPNP_SQPNP);
        // rotation is totally wrong, mostly pointing in z, just about pi/2.
        // maybe x and y are switched somehow.
        System.out.println("original rvec");
        System.out.println(rVec.dump());
        System.out.println("new rvec");
        System.out.println(newRVec.dump());
        // translation is pretty good
        System.out.println("original tvec");
        System.out.println(tVec.dump());
        System.out.println("new tvec");
        System.out.println(newTVec.dump());

        Mat rotM = new Mat();
        Calib3d.Rodrigues(newRVec, rotM);
        System.out.println("rotation matrix");
        System.out.println(rotM.dump());

        // camera rotation is just the reverse i.e. 1 radian the other way
        Mat camRot = new Mat();
        Calib3d.Rodrigues(rotM.t(), camRot);
        System.out.println("camera rotation in world coords");
        System.out.println(camRot.dump());

        // camera origin is at roughly (6.5, 2, -8)
        Mat inv = new Mat();
        Core.gemm(rotM.t(), newTVec, -1.0, new Mat(), 0.0, inv);
        System.out.println("camera position in world coords");
        System.out.println(inv.dump());

    }

    /**
     * Start with world coordinates, generate images, then generate poses.
     * seems like it works within ~4 in world units
     */
    // @Test
    public void testPoseFromImageFromWorldCoords() {
        // camera is at 2,0,-4, pointing 45 degrees to the left (which means negative
        // rotation)
        Mat worldRVec = Mat.zeros(3, 1, CvType.CV_64F);
        // worldRVec.put(0, 0, 0.0, -0.785398, 0.0);
        System.out.println("worldRVec");
        System.out.println(worldRVec.dump());

        Mat worldTVec = Mat.zeros(3, 1, CvType.CV_64F);
        worldTVec.put(0, 0, 20.0, 0.0, -40.0);
        System.out.println("worldTVec");
        System.out.println(worldTVec.dump());

        // derive camera transformations
        Mat worldRMat = new Mat();
        Calib3d.Rodrigues(worldRVec, worldRMat);

        // camera rotation is positive i.e. clockwise in these coords
        Mat camRVec = new Mat();
        Calib3d.Rodrigues(worldRMat.t(), camRVec);
        System.out.println("camRVec");
        System.out.println(camRVec.dump());

        Mat camTVec = new Mat();
        Core.gemm(worldRMat.t(), worldTVec, -1.0, new Mat(), 0.0, camTVec);
        System.out.println("camTVec");
        System.out.println(camTVec.dump());

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
        System.out.println("imagePts2f");
        System.out.println(imagePts2f.dump());

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
        System.out.println("approx curve");
        System.out.println(approxCurve.dump());
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
        System.out.println("hull");
        System.out.println(hull.dump());
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
        System.out.println("idx");
        System.out.println(idx);

        // put the idx'th element at zero
        Collections.rotate(approxCurveList, -idx);

        // ... aaand what's our position?
        Mat newRVec = new Mat(); // rVec.clone();
        Mat newTVec = new Mat(); // tVec.clone();
        MatOfPoint2f imagePoints = new MatOfPoint2f(approxCurveList.toArray(new Point[0]));
        System.out.println("imagePoints");
        System.out.println(imagePoints.dump());

        Calib3d.solvePnPRansac(objectPts3f, imagePoints, kMat, dMat,
                newRVec, newTVec, false,
                Calib3d.SOLVEPNP_SQPNP);
        // rotation is totally wrong, mostly pointing in z, just about pi/2.
        // maybe x and y are switched somehow.
        System.out.println("original rvec");
        System.out.println(camRVec.dump());
        System.out.println("new rvec");
        System.out.println(newRVec.dump());
        // translation is pretty good
        System.out.println("original tvec");
        System.out.println(camTVec.dump());
        System.out.println("new tvec");
        System.out.println(newTVec.dump());

        Mat rotM = new Mat();
        Calib3d.Rodrigues(newRVec, rotM);

        // camera rotation is just the reverse i.e. 1 radian the other way
        Mat camRot = new Mat();
        Calib3d.Rodrigues(rotM.t(), camRot);
        System.out.println("actual camera rotation in world coords");
        System.out.println(worldRVec.dump());
        System.out.println("camera rotation in world coords");
        System.out.println(camRot.dump());

        // camera origin is at roughly (6.5, 2, -8)
        Mat inv = new Mat();
        Core.gemm(rotM.t(), newTVec, -1.0, new Mat(), 0.0, inv);
        System.out.println("actual camera position in world coords");
        System.out.println(worldTVec.dump());
        System.out.println("camera position in world coords");
        System.out.println(inv.dump());

        double norm = Core.norm(worldTVec, inv);
        System.out.println("translation norm");
        System.out.println(norm);

        norm = Core.norm(worldRVec, camRot);
        System.out.println("rotation norm");
        System.out.println(norm);
    }

    /**
     * same as above but do it many times
     */
    // @Test
    public void testStrafing() {
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
                // System.out.println(idx);
                Mat cameraView = VisionUtil.makeImage(dxWorld, dyWorld, dzWorld, tilt, pan, kMat, dMat,
                        targetGeometryMeters, dsize);
                if (cameraView == null) {
                    // System.out.println("no camera view");
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

                //
                //
                //
                // just look at distortion for now
                // if (dx > 0)
                // continue;
                //
                //
                //
                //
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
                // System.out.println(result.dump());

                // Mat untiltedCameraView = Mat.zeros(dsize, CvType.CV_8UC3);
                Mat untiltedCameraView = Mat.zeros(tallSize, CvType.CV_8UC3);

                // Imgproc.warpPerspective(undistortedCameraView, untiltedCameraView, result,
                // dsize);
                Imgproc.warpPerspective(undistortedCameraView, untiltedCameraView, result, tallSize);

                Imgcodecs.imwrite(String.format("C:\\Users\\joelt\\Desktop\\pics\\target-%d-raw.png", idx),
                        untiltedCameraView);

                MatOfPoint2f imagePoints = VisionUtil.getImagePoints(idx, untiltedCameraView);
                if (imagePoints == null) {
                    // System.out.println("no image points");
                    continue;
                }

                // System.out.println(imagePoints.dump());

                //
                // // try homography.
                // // ok the homography approach isn't any better
                // // there's still no way to constrain it
                // MatOfPoint2f targetImageGeometry =
                // VisionUtil.makeTargetImageGeometryPixels(targetGeometryMeters, 1000);

                // Mat H = Calib3d.findHomography(targetImageGeometry, imagePoints);
                // System.out.println(H.dump());
                // List<Mat> rotations = new ArrayList<Mat>();
                // List<Mat> translations = new ArrayList<Mat>();
                // List<Mat> normals = new ArrayList<Mat>();
                // Calib3d.decomposeHomographyMat(H, kMat, rotations, translations, normals);
                // for (Mat m : translations) {
                // System.out.println(m.dump());
                // }

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
                // System.out.println(expandedTargetGeometryMeters.dump());
                MatOfPoint2f expandedImagePoints = new MatOfPoint2f(pointList.toArray(new Point[0]));

                // System.out.println(expandedImagePoints.dump());
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
                // System.out.println("dp");
                // System.out.println(dp.dump());
                for (int i = 0; i < dpdt.rows(); i += 2) {
                    Mat pointDpdt = dpdt.rowRange(i, i + 2);
                    // System.out.println("dpdt");
                    // System.out.println(pointDpdt.dump());
                    Mat dtdp = new Mat();
                    Core.invert(pointDpdt, dtdp, Core.DECOMP_SVD);

                    // System.out.println("dtdp");
                    // System.out.println(dtdp.dump());
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
                    System.out.println("Jworld");
                    System.out.println(Jworld.dump());
                    Mat dtWorld = new Mat();
                    Core.gemm(Jworld.t(), dt, -1.0, new Mat(), 0.0, dtWorld);
                    System.out.println("dtWorld");
                    System.out.println(dtWorld.dump());

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
                // NOTE: error in R becomes error in X and Y.
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

    /**
     * find a way to synthesize the target and also figure out units
     */
    // @Test
    public void testProjection() {
        Size dsize = new Size(960, 540); // 1/4 of 1080, just to i can see it more easily
        Mat kMat = VisionUtil.makeIntrinsicMatrix(512.0, dsize);
        MatOfDouble dMat = new MatOfDouble(Mat.zeros(4, 1, CvType.CV_64F));

        // target is 0.4m wide, 0.1m high .
        // MatOfPoint3f targetGeometryMeters = VisionUtil.makeTargetGeometry3f(0.4,
        // 0.1);
        MatOfPoint3f targetGeometryMeters = new MatOfPoint3f(new Point3(0.0, 0.01, 0));
        System.out.println("target geometry");
        System.out.println(targetGeometryMeters.dump());

        // in meters
        // double xPos = -0.2;
        // double yPos = 0.4;
        // double zPos = -0.8;
        double xPos = 0.0;
        double yPos = 0.0;
        double zPos = -10.0;
        // in radians
        // double tilt = 0.2;
        // double pan = 0.2;
        double tilt = 0.0;
        double pan = 0.0;

        Mat cameraView = VisionUtil.makeImage(xPos, yPos, zPos, tilt, pan, kMat, dMat, targetGeometryMeters, dsize);
        Imgcodecs.imwrite("C:\\Users\\joelt\\Desktop\\pics\\skewed.jpg", cameraView);
    }

    // project a single pixel into world space to show the shape of
    // the projection error
    public void projectPixel(Mat field, double scale, int fieldZ, Mat actualWorldTV, Mat camRV) {

        // 1/4 of 1080, so it doesn't take up the whole screen and doesn't take forever
        Size dsize = new Size(960, 540);

        // f=512 camera matrix
        Mat kMat = VisionUtil.makeIntrinsicMatrix(256.0, dsize);

        // no distortion for now
        MatOfDouble dMat = new MatOfDouble(Mat.zeros(4, 1, CvType.CV_64F));
        // System.out.println(dMat.dump());

        // just one target point for now
        // keep it away from the origin to prevent singularity
        MatOfPoint3f targetGeometryMeters = new MatOfPoint3f(new Point3(2.0, 2.0, 0.0));
        MatOfPoint2f targetImageGeometry = VisionUtil.makeTargetImageGeometryPixels(targetGeometryMeters, 1000);
        System.out.println(targetImageGeometry.dump());

        Mat camTV = VisionUtil.world2Cam(camRV, actualWorldTV);

        MatOfPoint2f skewedImagePts2f = new MatOfPoint2f();
        Mat jacobian = new Mat();
        Calib3d.projectPoints(targetGeometryMeters, camRV, camTV, kMat, dMat, skewedImagePts2f, jacobian);

        System.out.println("jacobian");
        System.out.println(jacobian.dump());

        // from calibration.cpp.
        // these are many 2x3 stacked vertically if there are multiple target points
        // but right now they are just 2x3.
        Mat dpdrot = jacobian.colRange(0, 3);
        Mat dpdt = jacobian.colRange(3, 6);

        // Mat dpdf = jacobian.colRange(6, 8);
        // Mat dpdc = jacobian.colRange(8, 10);
        // Mat dpddist = jacobian.colRange(10, jacobian.cols());

        // double dp = 3.0;
        for (int i = 0; i < targetGeometryMeters.rows(); ++i) {
            // dpixel/drot
            Mat pointdpdrot = dpdrot.rowRange(i * 2, i * 2 + 2);
            System.out.println("dpdrot");
            System.out.println(pointdpdrot.dump());

            // dpixel/dtranslation
            Mat pointDpdt = dpdt.rowRange(i * 2, i * 2 + 2);
            System.out.println("dpdt");
            System.out.println(pointDpdt.dump());

            // find the inverse, so drotation/dpixel
            // if condition number is too low, the matrix is "nearly singular"
            Mat drotdp = new Mat(); // 3x2
            double rotInvConditionNumber = Core.invert(pointdpdrot, drotdp, Core.DECOMP_SVD);
            System.out.println("rotation inverse condition number");
            System.out.println(rotInvConditionNumber);
            System.out.println("drotdp  ");
            System.out.println(drotdp.dump());

            // find the inverse, so dtranslation/dpixel
            Mat dtdp = new Mat(); // 3x2
            double tInvConditionNumber = Core.invert(pointDpdt, dtdp, Core.DECOMP_SVD);
            System.out.println("translation inverse condition number");
            System.out.println(tInvConditionNumber);
            System.out.println("dtdp");
            System.out.println(dtdp.dump());

            // make the center of the world translation estimate; in reality these would
            // come from solvePNP.
            // first the world rotation
            Mat camRM = new Mat();
            // this is the jacobian of the rodrigues transform; it yields 3x9 partials.
            Mat rodJ = new Mat();
            Calib3d.Rodrigues(camRV, camRM, rodJ);
            System.out.println("rodJ");
            System.out.println(rodJ.dump());
            Mat worldRM = camRM.t();
            // jacobian of the transpose is the transpose of the jacobian
            // which will be useful below
            Mat rodJT = rodJ.t(); // 9x3
            System.out.println("rodJT");
            System.out.println(rodJT.dump());

            // then the world translation
            Mat worldTV = new Mat();
            Core.gemm(worldRM, camTV, -1.0, new Mat(), 0.0, worldTV);

            System.out.println("world translation");
            System.out.println(worldTV.dump());

            // now the error in the translation estimate.
            //
            // the function is multiply(transposedrodrigues(rotation),-translation)
            // so the chain rule says we need
            // dmultiply/dtransposedrodrigues * dtransposedrodrigues/drot
            // * drot/dp - dmultiply/dtranslation * dtrans/dp
            // note the -1

            // first the multiplication derivatives
            Mat dmultdrot = new Mat(); // 3x9
            Mat dmultdt = new Mat(); // 3x3
            Calib3d.matMulDeriv(worldRM, camTV, dmultdrot, dmultdt);
            System.out.println("dmultdrot");
            System.out.println(dmultdrot.dump());
            System.out.println("dmultdt");
            System.out.println(dmultdt.dump());

            // the rodrigues jacobian is above

            // rotation term:
            Mat drotTdp = new Mat(); // 9x2
            Core.gemm(rodJT, drotdp, 1.0, new Mat(), 0.0, drotTdp);
            Mat dworldtdpR = new Mat();
            Core.gemm(dmultdrot, drotTdp, 1.0, new Mat(), 0.0, dworldtdpR);
            // translation term:
            Mat dworldtdpT = new Mat();
            Core.gemm(dmultdt, dtdp, -1.0, new Mat(), 0.0, dworldtdpT);
            Mat dworldtdp = new Mat();
            Core.add(dworldtdpR, dworldtdpT, dworldtdp);
            // resulting jacobian, (u,v) -> (xw,yw,zw):
            System.out.println("dworldtdp");
            System.out.println(dworldtdp.dump());

            Scalar color = new Scalar(255, 255, 255);
            Size axes = new Size(1 + dworldtdp.get(0, 0)[0] * scale, 1 + dworldtdp.get(2, 0)[0] * scale);
            System.out.println(axes.toString());
            Point center = new Point(fieldZ / 2 + worldTV.get(0, 0)[0] * scale, -1 * worldTV.get(2, 0)[0] * scale);
            System.out.println(center.toString());
            System.out.println(field.size().toString());

            Point pt1 = new Point(center.x - axes.width, center.y - axes.height);
            Point pt2 = new Point(center.x + axes.width, center.y + axes.height);
            System.out.println(pt1.toString());
            System.out.println(pt2.toString());
            Imgproc.rectangle(field, pt1, pt2, color);

        }
    }

    /**
     * try one projection with all the jacobians hooked up.
     */
    //@Test
    public void testJacobian() {

        double scale = 90; // pixels per meter in the xy projection
        int fieldX = (int) (12 * scale);
        int fieldZ = (int) (12 * scale);
        Mat field = Mat.zeros(fieldX, fieldZ, CvType.CV_64FC3);

        // just z translation, no rotation
        // double xPos = 0.0;
        double yPos = 0.0;
        // double zPos = -10.0;
        double tilt = 0.0;
        double pan = -0.1;

        for (double zPos = -10; zPos <= -1; zPos += 0.5) {
            for (double xPos = -4; xPos <= 4; xPos += 0.5) {

                Mat actualWorldTV = Mat.zeros(3, 1, CvType.CV_64F);
                actualWorldTV.put(0, 0, xPos, yPos, zPos);
                // camera up/right means world down/left, so both negative
                Mat camRV = VisionUtil.panTilt(-pan, -tilt);
                projectPixel(field, scale, fieldZ, actualWorldTV, camRV);

            }
        }

        Imgcodecs.imwrite("C:\\Users\\joelt\\Desktop\\pics\\field.jpg", field);

    }

    // @Test
    public void testMatMulJacobian() {
        Mat A = Mat.zeros(3, 3, CvType.CV_64F);
        A.put(0, 0,
                0.707, 0.707, 0,
                -0.707, 0.707, 0,
                0, 0, 1);
        Mat B = Mat.zeros(3, 1, CvType.CV_64F);
        B.put(0, 0, 1, -10, 100);
        Mat dABdA = new Mat();
        Mat dABdB = new Mat();

        Calib3d.matMulDeriv(A, B, dABdA, dABdB);
        System.out.println("A");
        System.out.println(A.dump());
        System.out.println("B");
        System.out.println(B.dump());
        System.out.println("dABdA");
        System.out.println(dABdA.dump());
        System.out.println("dABdB");
        System.out.println(dABdB.dump());

    }

    // @Test
    public void testRodriguesJacobian() {
        Mat RV = Mat.zeros(3, 1, CvType.CV_64F);
        RV.put(0, 0, 0, 0.0001, 0.0);
        Mat RM = new Mat();
        Mat J = new Mat();
        Calib3d.Rodrigues(RV, RM, J);
        System.out.println("RV");
        System.out.println(RV.dump());
        System.out.println("RM");
        System.out.println(RM.dump());
        System.out.println("J");
        System.out.println(J.dump());

    }
}