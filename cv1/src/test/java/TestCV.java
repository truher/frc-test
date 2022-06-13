import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.opencv.calib3d.Calib3d;
import org.opencv.core.Core;
import org.opencv.core.Scalar;
import org.opencv.core.Point;
import org.opencv.core.Mat;
import org.opencv.core.MatOfByte;
import org.opencv.core.MatOfDouble;
import org.opencv.core.MatOfInt;
import org.opencv.core.MatOfPoint;
import org.opencv.core.MatOfPoint2f;
import org.opencv.core.MatOfPoint3f;
import org.opencv.core.Point3;
import org.opencv.core.Rect;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;
import org.opencv.core.CvType;
import org.opencv.core.Size;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.ArrayList;

public class TestCV {
    public static final double DELTA = 0.00001;

    public TestCV() {
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
    }

    // @Test
    public void testToy() {
        assertTrue(true);
    }

    // @Test
    public void testFile() throws Exception {
        String foo = new String(getClass().getClassLoader().getResourceAsStream("readme.md").readAllBytes());
        assertEquals("hello", foo);
    }

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
        // TODO: make a rect.
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
        // look at it. Imgcodecs.imwrite("C:\\Users\\joelt\\Desktop\\foo.jpg", matrix);
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

        Imgcodecs.imwrite("C:\\Users\\joelt\\Desktop\\foo.jpg", matrix);

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
        // this is wrong, lines are straight. TODO: the right way.
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

        Imgcodecs.imwrite("C:\\Users\\joelt\\Desktop\\foo2.jpg", matrix);

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
        Imgcodecs.imwrite("C:\\Users\\joelt\\Desktop\\foo5.jpg", cameraView);

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
        Imgcodecs.imwrite("C:\\Users\\joelt\\Desktop\\foo6.jpg", cameraView);

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
        Imgcodecs.imwrite("C:\\Users\\joelt\\Desktop\\foo7.jpg", cameraView);

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
        Imgcodecs.imwrite("C:\\Users\\joelt\\Desktop\\foo7.jpg", cameraView);

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
     * synthesize an image of a vision target using the supplied location and
     * camera.
     * 
     * @param xPos                 camera location in world coords
     * @param yPos                 camera location in world coords
     * @param zPos                 camera location in world coords
     * @param tilt                 camera upwards tilt (around x axis)
     * @param pan                  camera rightwards pan (around y axis)
     * @param targetGeometryMeters target geometry in world coords
     */
    public Mat makeImage(
            double xPos,
            double yPos,
            double zPos,
            double tilt,
            double pan,
            Mat kMat,
            MatOfDouble dMat,
            MatOfPoint3f targetGeometryMeters,
            Size dsize) {

        MatOfPoint2f targetImageGeometry = makeTargetImageGeometryPixels(targetGeometryMeters, 1000);

        // camera up means world down, so negative
        // camera right means world left, so negative
        Mat camRV = panTilt(-pan, -tilt);

        Mat camRMat = new Mat();
        Calib3d.Rodrigues(camRV, camRMat);

        Mat worldRMat = camRMat.t();
        Mat worldRVec = new Mat();
        Calib3d.Rodrigues(worldRMat, worldRVec);

        Mat worldTVec = Mat.zeros(3, 1, CvType.CV_64F);
        worldTVec.put(0, 0, xPos, yPos, zPos);

        Mat camTVec = new Mat();
        Core.gemm(worldRMat.t(), worldTVec, -1.0, new Mat(), 0.0, camTVec);

        MatOfPoint2f skewedImagePts2f = new MatOfPoint2f();
        Calib3d.projectPoints(targetGeometryMeters, camRV, camTVec, kMat, dMat, skewedImagePts2f);

        // if clipping, this isn't going to work, so bail
        // actually it also doesn't work if the area is too close to the edge
        Rect r = new Rect(10, 10, 490, 490);
        for (Point p : skewedImagePts2f.toList()) {
            if (!r.contains(p)) {
                return null;
            }
        }

        Mat transformMat = Imgproc.getPerspectiveTransform(targetImageGeometry, skewedImagePts2f);

        // make an image corresponding to the pixel geometry, for warping
        Mat visionTarget = new Mat(boundingBox(targetImageGeometry), CvType.CV_8U, new Scalar(255, 255, 255));
        Imgcodecs.imwrite("C:\\Users\\joelt\\Desktop\\projection.jpg", visionTarget);

        Mat cameraView = Mat.zeros(dsize, CvType.CV_8U);
        Imgproc.warpPerspective(visionTarget, cameraView, transformMat, dsize);
        Imgcodecs.imwrite("C:\\Users\\joelt\\Desktop\\foo7.jpg", cameraView);
        return cameraView;
    }

    /**
     * using a known target geometry, generate an image of the target viewed
     * from the specified location.
     */
    public Mat makeSkewedTargetImage(
            double xPos,
            double yPos,
            double zPos,
            double tilt,
            double pan,
            Mat kMat,
            MatOfDouble dMat,
            MatOfPoint3f targetGeometryMeters,
            Size dsize) {

        MatOfPoint2f targetImageGeometry = makeTargetImageGeometryPixels(targetGeometryMeters, 1000);
        System.out.println("target image geometry");
        System.out.println(targetImageGeometry.dump());

        // make an image corresponding to the pixel geometry, for warping
        Mat visionTarget = new Mat(boundingBox(targetImageGeometry), CvType.CV_8U, new Scalar(255, 255, 255));
        Imgcodecs.imwrite("C:\\Users\\joelt\\Desktop\\projection.jpg", visionTarget);

        Mat camRVec = Mat.zeros(3, 1, CvType.CV_64F);
        camRVec.put(0, 0, 0.0, -pan, 0.0);

        Mat camTVec = Mat.zeros(3, 1, CvType.CV_64F);
        camTVec.put(0, 0, xPos, yPos, zPos);

        MatOfPoint2f skewedImagePts2f = new MatOfPoint2f();
        // same object, different camera position
        Calib3d.projectPoints(targetGeometryMeters, camRVec, camTVec, kMat, dMat, skewedImagePts2f);
        // transform between the two projections
        Mat transformMat = Imgproc.getPerspectiveTransform(targetImageGeometry, skewedImagePts2f);
        // apply the transform to the flat image
        Mat cameraView = Mat.zeros(dsize, CvType.CV_8U);
        Imgproc.warpPerspective(visionTarget, cameraView, transformMat, dsize);
        return cameraView;
    }

    /**
     * find the target corners, in the correct order, in the supplied image.
     */
    public MatOfPoint2f getImagePoints(Mat rawCameraView) {

        // first "binarize" to remove blur
        Mat cameraView = new Mat();
        Imgproc.threshold(rawCameraView, cameraView, 250, 255, Imgproc.THRESH_BINARY);

        List<MatOfPoint> contours = new ArrayList<>();
        Mat hierarchy = new Mat();
        Imgproc.findContours(cameraView,
                contours,
                hierarchy,
                Imgproc.RETR_LIST,
                Imgproc.CHAIN_APPROX_SIMPLE);

        assertEquals(1, contours.size());

        Mat contourView2 = Mat.zeros(cameraView.size(), CvType.CV_8U);
        Imgproc.drawContours(contourView2, contours, 0, new Scalar(255, 0, 0));
        Imgcodecs.imwrite("C:\\Users\\joelt\\Desktop\\contours.jpg", contourView2);

        MatOfPoint2f curve = new MatOfPoint2f(contours.get(0).toArray());
        MatOfPoint2f approxCurve = new MatOfPoint2f();
        Imgproc.approxPolyDP(curve, approxCurve, 5, true);
        MatOfPoint points = new MatOfPoint(approxCurve.toArray());
        // System.out.println("points");
        // System.out.println(points.dump());

        Mat contourView = Mat.zeros(cameraView.size(), CvType.CV_8U);
        Imgproc.drawContours(contourView, List.of(points), 0, new Scalar(255, 0, 0));
        Imgcodecs.imwrite("C:\\Users\\joelt\\Desktop\\poly.jpg", contourView);

        assertEquals(4, approxCurve.toList().size());

        MatOfInt hull = new MatOfInt();

        Imgproc.convexHull(points, hull, true);

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

        Collections.rotate(approxCurveList, -idx);

        MatOfPoint2f imagePoints = new MatOfPoint2f(approxCurveList.toArray(new Point[0]));
        return imagePoints;
    }

    private Mat makeKMat(double f, Size dsize) {
        Mat kMat = Mat.zeros(3, 3, CvType.CV_64F);
        kMat.put(0, 0,
                f, 0.0, dsize.width / 2,
                0.0, f, dsize.height / 2,
                0.0, 0.0, 1.0);
        return kMat;
    }

    /**
     * same as above but do it many times
     */
    // @Test
    public void testStrafing() {
        Size dsize = new Size(960, 540);
        Mat kMat = makeKMat(512.0, dsize);

        // TODO: measure distortion in a real camera
        // Note: distortion confuses pnpransac, use normal pnp instead
        MatOfDouble dMat = new MatOfDouble(Mat.zeros(4, 1, CvType.CV_64F));
        dMat.put(0, 0, -0.05, 0.0, 0.0, 0.0); // a bit of barrel
        // target is 0.4m wide, 0.1m high .
        MatOfPoint3f targetGeometryMeters = makeTargetGeometry3f(0.4, 0.1);

        System.out.println("dx, dy, dz, pdx, pdy, pdz");

        final double dy = -0.2; // camera is below (+y) relative to the target
        final double tilt = 0.25; // camera tilts up 0.25 radians == about 15 degrees
        final double pan = 0.0; // for now, straight ahead
        for (double dz = -2; dz < -1; dz += 0.1) {
            for (double dx = -1; dx < 1; dx += 0.1) {
                Mat cameraView = makeImage(dx, dy, dz, tilt, pan, kMat, dMat,
                        targetGeometryMeters, dsize);
                if (cameraView == null)
                    continue;
                MatOfPoint2f imagePoints = getImagePoints(cameraView);
                Mat newRVec = new Mat();
                Mat newTVec = new Mat();
                Calib3d.solvePnP(targetGeometryMeters, imagePoints,
                        kMat, dMat, newRVec, newTVec);

                Mat rotM = new Mat();
                Calib3d.Rodrigues(newRVec, rotM);
                Mat camRot = new Mat();
                Calib3d.Rodrigues(rotM.t(), camRot);
                Mat inv = new Mat();
                Core.gemm(rotM.t(), newTVec, -1.0, new Mat(), 0.0, inv);
                System.out.printf("%f, %f, %f, %f, %f, %f\n", dx, dy, dz,
                        inv.get(0, 0)[0], inv.get(1, 0)[0], inv.get(2, 0)[0]);
            }
        }
    }

    /**
     * find a way to synthesize the target and also figure out units
     */
    // @Test
    public void testProjection() {
        Size dsize = new Size(960, 540); // 1/4 of 1080, just to i can see it more easily
        Mat kMat = makeKMat(512.0, dsize);
        MatOfDouble dMat = new MatOfDouble(Mat.zeros(4, 1, CvType.CV_64F));
        // target is 0.4m wide, 0.1m high .
        MatOfPoint3f targetGeometryMeters = makeTargetGeometry3f(0.4, 0.1);
        System.out.println("target geometry");
        System.out.println(targetGeometryMeters.dump());

        double xPos = 0.1;
        double yPos = -0.2;
        double zPos = 0.8;
        double tilt = 0.0;
        double pan = 0.2;

        Mat cameraView = makeSkewedTargetImage(xPos, yPos, zPos, tilt, pan, kMat, dMat, targetGeometryMeters, dsize);
        Imgcodecs.imwrite("C:\\Users\\joelt\\Desktop\\skewed.jpg", cameraView);

    }

    public MatOfPoint2f slice(MatOfPoint3f geometry) {
        List<Point> pointList = new ArrayList<Point>();
        for (Point3 p : geometry.toList()) {
            pointList.add(new Point(p.x, p.y));
        }
        return new MatOfPoint2f(pointList.toArray(new Point[0]));
    }

    public Size boundingBox(MatOfPoint2f geometry) {
        double minX = Collections.min(geometry.toList(), Comparator.comparingDouble((s) -> s.x)).x;
        double minY = Collections.min(geometry.toList(), Comparator.comparingDouble((s) -> s.y)).y;
        double maxX = Collections.max(geometry.toList(), Comparator.comparingDouble((s) -> s.x)).x;
        double maxY = Collections.max(geometry.toList(), Comparator.comparingDouble((s) -> s.y)).y;
        return new Size(maxX - minX, maxY - minY);
    }

    /**
     * Transform the world-coordinates 3d-but-planar target into pixels at the
     * specified scale.
     */
    public MatOfPoint2f makeTargetImageGeometryPixels(MatOfPoint3f targetGeometryMeters,
            double scalePixelsPerMeter) {
        MatOfPoint2f slice = slice(targetGeometryMeters);
        double minX = Collections.min(slice.toList(), Comparator.comparingDouble((s) -> s.x)).x;
        double minY = Collections.min(slice.toList(), Comparator.comparingDouble((s) -> s.y)).y;
        List<Point> pointList = new ArrayList<Point>();
        for (Point3 p : targetGeometryMeters.toList()) {
            pointList.add(new Point((p.x - minX) * scalePixelsPerMeter,
                    (p.y - minY) * scalePixelsPerMeter));
        }
        return new MatOfPoint2f(pointList.toArray(new Point[0]));
    }

    /**
     * the actual target geometry in world coordintes: the target
     * is centered at the origin.
     */
    public MatOfPoint3f makeTargetGeometry3f(double targetWidthMeters, double targetHeightMeters) {
        return new MatOfPoint3f(
                new Point3(-targetWidthMeters / 2, -targetHeightMeters / 2, 0.0),
                new Point3(-targetWidthMeters / 2, targetHeightMeters / 2, 0.0),
                new Point3(targetWidthMeters / 2, targetHeightMeters / 2, 0.0),
                new Point3(targetWidthMeters / 2, -targetHeightMeters / 2, 0.0));
    }

    /**
     * first pan (about y) and then tilt (about x), return resulting rotation vector
     */
    public Mat panTilt(double pan, double tilt) {
        Mat panV = Mat.zeros(3, 1, CvType.CV_64F);
        panV.put(0, 0, 0.0, pan, 0.0);
        Mat tiltV = Mat.zeros(3, 1, CvType.CV_64F);
        tiltV.put(0, 0, 0.0, tilt, 0.0);
        return combineRotations(panV, tiltV);
    }

    /**
     * apply the first rotation and then the second, return resulting rotation
     * vector
     */
    public Mat combineRotations(Mat first, Mat second) {
        Mat firstM = new Mat();
        Calib3d.Rodrigues(first, firstM);
        Mat secondM = new Mat();
        Calib3d.Rodrigues(second, secondM);
        // "first" means on the right side since the resulting matrix appears on the
        // left in actual computation (i think?)
        Mat productM = new Mat();
        Core.gemm(secondM, firstM, 1.0, new Mat(), 0.0, productM);
        Mat productV = new Mat();
        Calib3d.Rodrigues(productM, productV);
        return productV;
    }

    // @Test
    public void testCombiningRotations() {
        Mat pan = Mat.zeros(3, 1, CvType.CV_64F);
        pan.put(0, 0, 0.0, -0.5, 0.0); // pan to right, world to left, so negative
        Mat tilt = Mat.zeros(3, 1, CvType.CV_64F);
        tilt.put(0, 0, -0.5, 0.0, 0.0); // tilt up, world down, so negative
        System.out.println("pan vector");
        System.out.println(pan.dump());
        System.out.println("tilt vector");
        System.out.println(tilt.dump());
        // pan first to keep horizon horizontal
        Mat productV = combineRotations(pan, tilt);
        System.out.println("product vector");
        System.out.println(productV.dump());
    }

    public Mat rotm2euler(Mat r) {
        double sy = Math.sqrt(r.get(0, 0)[0] * r.get(0, 0)[0] + r.get(1, 0)[0] * r.get(1, 0)[0]);
        double x, y, z;
        if (sy > 1e-6) { // singular
            x = Math.atan2(r.get(2, 1)[0], r.get(2, 2)[0]);
            y = Math.atan2(-r.get(2, 0)[0], sy);
            z = Math.atan2(r.get(1, 0)[0], r.get(0, 0)[0]);
        } else {
            x = Math.atan2(-r.get(1, 2)[0], r.get(1, 1)[0]);
            y = Math.atan2(-r.get(2, 0)[0], sy);
            z = 0;
        }
        Mat euler = Mat.zeros(3, 1, CvType.CV_64F);
        euler.put(0, 0, x, y, z);
        return euler;
    }

    @Test
    public void testEuler() {
        Mat pan = Mat.zeros(3, 1, CvType.CV_64F);
        pan.put(0, 0, 0.0, -0.7854, 0.0); // pan 45deg to right, world to left, so
        // negative
        Mat tilt = Mat.zeros(3, 1, CvType.CV_64F);
        tilt.put(0, 0, -0.7854, 0.0, 0.0); // tilt 45deg up, world down, so negative
        System.out.println("pan vector");
        System.out.println(pan.dump());
        System.out.println("tilt vector");
        System.out.println(tilt.dump());
        // pan first to keep horizon horizontal
        Mat productV = combineRotations(pan, tilt);
        Mat productM = new Mat();
        Calib3d.Rodrigues(productV, productM);

        Mat r = productM.t();

        System.out.println("result");
        System.out.println(r.dump());

        Mat euler = rotm2euler(r);

        System.out.println("euler radians");
        System.out.println(euler.dump());

    }
}
