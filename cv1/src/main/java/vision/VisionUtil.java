package vision;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Random;

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

public abstract class VisionUtil {
    static {
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
    }
    static final boolean DEBUG = false;
    static final int LEVEL = 1;

    /**
     * return the Tait-Bryan (ZYX) "Euler" angles for the supplied rotation matrix.
     * 
     * @param r rotation matrix
     * @return Tait-Bryan ZYX angles in a 1x3 matrix
     */
    public static Mat rotm2euler(Mat r) {
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

    public static double rotm2euler2d(Mat r) {
        return Math.atan2(r.get(1, 0)[0], r.get(0, 0)[0]);
    }

    /**
     * apply the first Rodrigues rotation vector and then the second, return
     * resulting rotation vector
     * 
     * @param first  Rodrigues rotation vector to apply first
     * @param second Rodrigues rotation vector to apply second
     * @return resulting Rodrigues rotation vector
     */
    public static Mat combineRotations(Mat first, Mat second) {
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

    /**
     * first pan (about y) and then tilt (about x), return resulting rotation vector
     * 
     * @param pan  radians, up is positive
     * @param tilt radians, right is positive
     * @return Rodrigues rotation vector
     */
    public static Mat panTilt(double pan, double tilt) {
        Mat panV = Mat.zeros(3, 1, CvType.CV_64F);
        panV.put(0, 0, 0.0, pan, 0.0);
        Mat tiltV = Mat.zeros(3, 1, CvType.CV_64F);
        tiltV.put(0, 0, tilt, 0.0, 0.0);
        return combineRotations(panV, tiltV);
    }

    /**
     * make camera intrinsic matrix
     * 
     * @param f     focal length
     * @param dsize
     *              image size; camera axis goes in the center
     * @return camera intrinsic matrix
     */
    public static Mat makeIntrinsicMatrix(double f, Size dsize) {
        Mat kMat = Mat.zeros(3, 3, CvType.CV_64F);
        kMat.put(0, 0,
                f, 0.0, dsize.width / 2,
                0.0, f, dsize.height / 2,
                0.0, 0.0, 1.0);
        return kMat;
    }

    /**
     * return x and y components of 3d points
     * 
     * @param geometry 3d points
     * @return 2d points
     */
    public static MatOfPoint2f slice(MatOfPoint3f geometry) {
        List<Point> pointList = new ArrayList<Point>();
        for (Point3 p : geometry.toList()) {
            pointList.add(new Point(p.x, p.y));
        }
        return new MatOfPoint2f(pointList.toArray(new Point[0]));
    }

    /**
     * return size of bounding box
     * 
     * @param geometry 2d points
     * @return size of box containing the points
     */
    public static Size boundingBox(MatOfPoint2f geometry) {
        double minX = Collections.min(geometry.toList(), Comparator.comparingDouble((s) -> s.x)).x;
        double minY = Collections.min(geometry.toList(), Comparator.comparingDouble((s) -> s.y)).y;
        double maxX = Collections.max(geometry.toList(), Comparator.comparingDouble((s) -> s.x)).x;
        double maxY = Collections.max(geometry.toList(), Comparator.comparingDouble((s) -> s.y)).y;
        return new Size(maxX - minX, maxY - minY);
    }

    /**
     * Make geometry for a rectangular target centered at the origin with specified
     * x width and y height and zero z thickness, upper-left first, then counter
     * clockwise viewed from the usual camera position with negative z (note this
     * actually means clockwise using the usual z axis/angle notion).
     * 
     * @param width
     * @param height
     * @return four 3d points at the corners
     */
    public static MatOfPoint3f makeTargetGeometry3f(double width, double height) {
        return new MatOfPoint3f(
                new Point3(-width / 2, -height / 2, 0.0),
                new Point3(-width / 2, height / 2, 0.0),
                new Point3(width / 2, height / 2, 0.0),
                new Point3(width / 2, -height / 2, 0.0));
    }

    /**
     * specify corner points (x1,y1) and (x2,y2), return rectangle at z=0.
     */
    public static MatOfPoint3f makeTarget(double x1, double y1, double x2, double y2) {
        return new MatOfPoint3f(
                new Point3(x2, y1, 0.0),
                new Point3(x2, y2, 0.0),
                new Point3(x1, y2, 0.0),
                new Point3(x1, y1, 0.0));
    }

    /**
     * more points
     */
    public static MatOfPoint3f makeTargetGeometry3f2(double width, double height) {
        // precisely planar target makes the solver freak out.
        // i added a tiny bit of z to "fix" it
        return new MatOfPoint3f(
                new Point3(0.0, 0.0, 0.001),
                new Point3(-width / 2, -height / 2, 0.0),
                new Point3(-width / 2, height / 2, 0.0),
                new Point3(width / 2, height / 2, 0.0),
                new Point3(width / 2, -height / 2, 0.0),
                new Point3(-width / 4, -height / 4, 0.0),
                new Point3(-width / 4, height / 4, 0.0),
                new Point3(width / 4, height / 4, 0.0),
                new Point3(width / 4, -height / 4, 0.0));
    }

    /**
     * Transform the world-coordinates 3d-but-planar target into pixels at the
     * specified scale.
     * 
     * @param targetGeometryMeters 3d points representing the target in world
     *                             coordinates
     * @param scalePixelsPerMeter  how many pixels per world unit
     * @return 2d points
     */
    public static MatOfPoint2f makeTargetImageGeometryPixels(MatOfPoint3f targetGeometryMeters,
            double scalePixelsPerMeter) {
        MatOfPoint2f slice = VisionUtil.slice(targetGeometryMeters);
        double minX = Collections.min(slice.toList(), Comparator.comparingDouble((s) -> s.x)).x;
        double minY = Collections.min(slice.toList(), Comparator.comparingDouble((s) -> s.y)).y;
        List<Point> pointList = new ArrayList<Point>();
        for (Point3 p : targetGeometryMeters.toList()) {
            pointList.add(new Point((p.x - minX) * scalePixelsPerMeter,
                    (p.y - minY) * scalePixelsPerMeter));
        }
        return new MatOfPoint2f(pointList.toArray(new Point[0]));
    }

    static Mat homogeneousRigidTransform(Mat R, Mat t) {
        Mat worldToCamera = Mat.zeros(4, 4, CvType.CV_32F);
        worldToCamera.put(0, 0,
                R.get(0, 0)[0], R.get(0, 1)[0], R.get(0, 2)[0], t.get(0, 0)[0],
                R.get(1, 0)[0], R.get(1, 1)[0], R.get(1, 2)[0], t.get(1, 0)[0],
                R.get(2, 0)[0], R.get(2, 1)[0], R.get(2, 2)[0], t.get(2, 0)[0],
                0, 0, 0, 1);
        return worldToCamera;
    }

    public static MatOfPoint3f duplicatePoints(MatOfPoint3f targetGeometryMeters, int pointMultiplier) {
        MatOfPoint3f targetPointsMultiplied = new MatOfPoint3f();
        List<Point3> targetpointlist = new ArrayList<Point3>();
        for (int reps = 0; reps < pointMultiplier; reps++) {
            for (Point3 p : targetGeometryMeters.toList()) {
                targetpointlist.add(p);
            }
        }
        targetPointsMultiplied = new MatOfPoint3f(targetpointlist.toArray(new Point3[0]));
        return targetPointsMultiplied;
    }

    public static Mat makeWorldToCameraHomogeneous(double pan, double xPos, double yPos, double zPos) {
        // these are camera-to-world transforms
        Mat cameraToWorldTVec = Mat.zeros(3, 1, CvType.CV_32F);
        cameraToWorldTVec.put(0, 0, xPos, yPos, zPos);
        debug(0, "worldTVec", cameraToWorldTVec);

        Mat cameraToWorldRV = Mat.zeros(3, 1, CvType.CV_32F);
        cameraToWorldRV.put(0, 0, 0.0, pan, 0.0);
        debug(0, "worldRV", cameraToWorldRV);

        Mat cameraToWorldRMat = new Mat();
        Calib3d.Rodrigues(cameraToWorldRV, cameraToWorldRMat);

        Mat cameraToWorldHomogeneous = homogeneousRigidTransform(cameraToWorldRMat, cameraToWorldTVec);
        debug(0, "cameraToWorld (just to see)", cameraToWorldHomogeneous);

        // this is inverse(worldT*worldR)
        // inverse of multiplication is order-reversed multipication of inverses, so
        // which is worldR.t * -worldT or camR*-worldT
        Mat worldToCameraRMat = cameraToWorldRMat.t();
        Mat worldToCameraTVec = new Mat();
        Core.gemm(worldToCameraRMat, cameraToWorldTVec, -1.0, new Mat(), 0, worldToCameraTVec);
        debug(0, "camTVec", worldToCameraTVec);

        Mat worldToCameraHomogeneous = homogeneousRigidTransform(worldToCameraRMat, worldToCameraTVec);
        debug(0, "worldToCamera", worldToCameraHomogeneous);
        return worldToCameraHomogeneous;
    }

    /**
     * find the target corners, in the supplied image, upper-left first, then
     * clockwise.
     * 
     * 
     * @param picIdx        for debugging
     * @param rawCameraView unprocessed image
     * @return 2d geometry of the corners, in the image
     */
    public static MatOfPoint2f getImagePoints(int picIdx, Mat rawCameraView) {

        // first "binarize" to remove blur
        Mat cameraView = new Mat();
        Imgproc.threshold(rawCameraView, cameraView, 250, 255, Imgproc.THRESH_BINARY);
        Imgcodecs.imwrite(String.format("C:\\Users\\joelt\\Desktop\\pics\\target-%d-thresholded.png", picIdx),
                cameraView);

        Mat singleChannelCameraView = new Mat();
        Imgproc.cvtColor(cameraView, singleChannelCameraView, Imgproc.COLOR_BGR2GRAY);
        Imgcodecs.imwrite(String.format("C:\\Users\\joelt\\Desktop\\pics\\target-%d-bw.png", picIdx),
                singleChannelCameraView);

        /*
         * Mat edges = new Mat();
         * Imgproc.Canny(singleChannelCameraView, edges, 250, 255);
         * MatOfPoint approxCurve = new MatOfPoint();
         * Imgproc.goodFeaturesToTrack(edges, approxCurve, 4, 0.5, 2);
         * System.out.println("approxcurve");
         * System.out.println(approxCurve.dump());
         */

        List<MatOfPoint> contours = new ArrayList<>();
        Mat hierarchy = new Mat();
        Imgproc.findContours(singleChannelCameraView,
                contours,
                hierarchy,
                Imgproc.RETR_LIST,
                Imgproc.CHAIN_APPROX_NONE);

        if (contours.size() != 1) {
            // System.out.println("no contours!");
            return null;
        }

        {
            Mat contourView2 = Mat.zeros(cameraView.size(), CvType.CV_8U);
            Imgproc.drawContours(contourView2, contours, 0, new Scalar(255, 0, 0));
            Imgcodecs.imwrite(String.format("C:\\Users\\joelt\\Desktop\\pics\\target-%d-contours.png", picIdx),
                    contourView2);
        }

        MatOfPoint2f curve = new MatOfPoint2f(contours.get(0).toArray());
        MatOfPoint2f approxCurve = new MatOfPoint2f();
        double epsilon = 0.04 * Imgproc.arcLength(curve, true);
        Imgproc.approxPolyDP(curve, approxCurve, epsilon, true);
        MatOfPoint points = new MatOfPoint(approxCurve.toArray());
        // System.out.println("points");
        // System.out.println(points.dump());

        {
            Mat contourView = Mat.zeros(cameraView.size(), CvType.CV_8U);
            Imgproc.drawContours(contourView, List.of(points), 0, new Scalar(255, 0, 0));
            Imgcodecs.imwrite(String.format("C:\\Users\\joelt\\Desktop\\pics\\target-%d-poly.png", picIdx),
                    contourView);
        }
        // System.out.println("approxcurve");
        // System.out.println(approxCurve.dump());
        if (approxCurve.toList().size() != 4)

        {
            // System.out.println("wrong size");
            // System.out.println(approxCurve.dump());
            return null;
        }

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

        Mat pointView = rawCameraView.clone();
        for (Point pt : imagePoints.toList()) {
            Imgproc.circle(pointView,
                    new Point(pt.x, pt.y),
                    3,
                    new Scalar(0, 255, 0),
                    Imgproc.FILLED);
        }
        Imgcodecs.imwrite(String.format("C:\\Users\\joelt\\Desktop\\pics\\target-%d-points.png", picIdx),
                pointView);
        return imagePoints;
    }

    /**
     * given camera rotation in camera frame and camera position in *world* frame,
     * *in* the world, derive camera position in camera frame, for warping.
     * 
     * @param camera rotation vector
     * @param world  translation vector
     * @return camera translation vector
     */
    public static Mat world2Cam(Mat camRV, Mat worldTVec) {
        // make camera coords from world coords
        Mat camRMat = new Mat();
        Calib3d.Rodrigues(camRV, camRMat);

        Mat worldRMat = camRMat.t();
        Mat worldRVec = new Mat();
        Calib3d.Rodrigues(worldRMat, worldRVec);

        Mat camTVec = new Mat();
        Core.gemm(worldRMat.t(), worldTVec, -1.0, new Mat(), 0.0, camTVec);
        return camTVec;
    }

    /**
     * apply an dx-translation transform to the given transform (i.e. multiply it)
     */
    public static Mat translateX(Mat worldToCamera, double dx) {
        Mat translation = Mat.zeros(4, 4, CvType.CV_32F);
        translation.put(0, 0,
                1, 0, 0, dx,
                0, 1, 0, 0,
                0, 0, 1, 0,
                0, 0, 0, 1);
        debug(0, "translation", translation);
        Mat result = new Mat();
        Core.gemm(translation, worldToCamera, 1.0, new Mat(), 0.0, result);
        debug(0, "result", result);
        return result;
    }

    public static boolean inViewport(MatOfPoint2f pts, Rect viewport) {
        for (Point pt : pts.toList()) {
            if (!viewport.contains(pt))
                return false;
        }
        return true;
    }

    public static MatOfPoint2f imagePoints(Mat kMat, MatOfDouble dMat, MatOfPoint3f geometry, Mat worldToCamera,
            int pointMultiplier, double noisePixels, Random rand) {
        Mat Rvec = Mat.zeros(3, 1, CvType.CV_32F);
        Calib3d.Rodrigues(worldToCamera.rowRange(0, 3).colRange(0, 3), Rvec);
        debug(0, "Rvec", Rvec);
        Mat t = worldToCamera.colRange(3, 4).rowRange(0, 3);
        debug(0, "t", t);

        MatOfPoint2f pts = new MatOfPoint2f();
        Mat jacobian = new Mat();
        Calib3d.projectPoints(geometry, Rvec, t, kMat, dMat, pts, jacobian);
        debug(0, "pts", pts);
        // add image noise in image coords
        List<Point> ptsList = new ArrayList<Point>();
        for (int reps = 0; reps < pointMultiplier; reps++) {
            for (Point p : pts.toList()) {
                p.x = p.x + rand.nextGaussian() * noisePixels;
                p.y = p.y + rand.nextGaussian() * noisePixels;
                ptsList.add(p);
            }
        }
        return new MatOfPoint2f(ptsList.toArray(new Point[0]));
    }

    public static MatOfPoint2f getImagePoints(
            double xPos,
            double yPos,
            double zPos,
            double tilt,
            double pan,
            Mat kMat,
            MatOfDouble dMat,
            MatOfPoint3f targetGeometryMeters) {
        // System.out.println(targetGeometryMeters.dump());
        Mat worldTVec = Mat.zeros(3, 1, CvType.CV_64F);
        worldTVec.put(0, 0, xPos, yPos, zPos);

        // camera up/right means world down/left, so both negative
        Mat camRV = VisionUtil.panTilt(-pan, -tilt);
        Mat camRMat = new Mat();
        Calib3d.Rodrigues(camRV, camRMat);

        Mat worldRMat = camRMat.t();
        Mat worldRV = new Mat();
        Calib3d.Rodrigues(worldRMat, worldRV);

        Mat camTVec = VisionUtil.world2Cam(camRV, worldTVec);
        // System.out.println(dMat.dump());

        MatOfPoint2f skewedImagePts2f = new MatOfPoint2f();
        Mat jacobian = new Mat();
        // Calib3d.projectPoints(targetGeometryMeters, camRV, camTVec, kMat, dMat,
        // skewedImagePts2f, jacobian);
        Calib3d.projectPoints(targetGeometryMeters, camRV, camTVec, kMat, dMat, skewedImagePts2f, jacobian);

        return skewedImagePts2f;
    }

    /**
     * synthesize an image of a vision target using the supplied location and
     * camera.
     * using a known target geometry, generate an image of the target viewed
     * from the specified location, which is the position of the camera in the
     * world, and the rotations are rotations *of the camera*.
     * 
     * @param xPos                 camera location in world coords
     * @param yPos                 camera location in world coords
     * @param zPos                 camera location in world coords
     * @param tilt                 camera upwards tilt (around x axis)
     * @param pan                  camera rightwards pan (around y axis)
     * @param targetGeometryMeters target geometry in world coords
     */
    public static Mat makeImage(
            double xPos,
            double yPos,
            double zPos,
            double tilt,
            double pan,
            Mat kMat,
            MatOfDouble dMat,
            MatOfPoint3f targetGeometryMeters,
            Size dsize) {

        // // System.out.println(targetGeometryMeters.dump());
        // Mat worldTVec = Mat.zeros(3, 1, CvType.CV_64F);
        // worldTVec.put(0, 0, xPos, yPos, zPos);
        // MatOfPoint2f targetImageGeometry =
        // VisionUtil.makeTargetImageGeometryPixels(targetGeometryMeters, 1000);
        // // System.out.println(targetImageGeometry.dump());

        // // make an image corresponding to the pixel geometry, for warping
        // Mat visionTarget = new Mat(VisionUtil.boundingBox(targetImageGeometry),
        // CvType.CV_8UC3,
        // new Scalar(255, 255, 255));
        // // Imgcodecs.imwrite("C:\\Users\\joelt\\Desktop\\projection.jpg",
        // visionTarget);

        // // camera up/right means world down/left, so both negative
        // Mat camRV = VisionUtil.panTilt(-pan, -tilt);

        // Mat camTVec = VisionUtil.world2Cam(camRV, worldTVec);
        // // System.out.println(dMat.dump());

        // MatOfPoint2f skewedImagePts2f = new MatOfPoint2f();
        // Mat jacobian = new Mat();
        // Calib3d.projectPoints(targetGeometryMeters, camRV, camTVec, kMat, dMat,
        // skewedImagePts2f, jacobian);

        MatOfPoint2f targetImageGeometry = VisionUtil.makeTargetImageGeometryPixels(targetGeometryMeters, 1000);
        // System.out.println(targetImageGeometry.dump());

        // make an image corresponding to the pixel geometry, for warping
        Mat visionTarget = new Mat(VisionUtil.boundingBox(targetImageGeometry), CvType.CV_8UC3,
                new Scalar(255, 255, 255));
        // Imgcodecs.imwrite("C:\\Users\\joelt\\Desktop\\projection.jpg", visionTarget);

        MatOfPoint2f skewedImagePts2f = getImagePoints(xPos,
                yPos,
                zPos,
                tilt,
                pan,
                kMat,
                dMat,
                targetGeometryMeters);

        // System.out.println("jacobian");
        // System.out.println(jacobian.dump());
        // System.out.println(skewedImagePts2f.dump());
        // MatOfPoint2f undistortedPts = new MatOfPoint2f();
        // Calib3d.projectPoints(targetGeometryMeters, camRV, camTVec, kMat, new
        // MatOfDouble(), undistortedPts);
        // System.out.println("undistorted?");
        // System.out.println(undistortedPts.dump());

        // MatOfPoint2f maybeGood = new MatOfPoint2f();
        // System.out.println(kMat.dump());
        // Calib3d.undistortPoints(skewedImagePts2f, maybeGood, kMat, dMat, new Mat(),
        // kMat);
        // System.out.println("maybe good");
        // System.out.println(maybeGood.dump());
        // System.out.println(kMat.dump());

        // if clipping, this isn't going to work, so bail
        // actually it also doesn't work if the area is too close to the edge
        final int border = 5;
        Rect r = new Rect(border, border, (int) (dsize.width - border), (int) (dsize.height - border));
        for (Point p : skewedImagePts2f.toList()) {
            if (!r.contains(p)) {
                // System.out.println("out of frame");
                return null;
            }
        }

        Mat transformMat = Imgproc.getPerspectiveTransform(targetImageGeometry, skewedImagePts2f);

        Mat cameraView = Mat.zeros(dsize, CvType.CV_8UC3);

        Imgproc.warpPerspective(visionTarget, cameraView, transformMat, dsize);

        return cameraView;
    }

    public static void writePng(MatOfPoint2f pts, int width, int height, String filename) {
        final Scalar green = new Scalar(0, 255, 0);
        Mat img = Mat.zeros(height, width, CvType.CV_32FC3);
        for (Point pt : pts.toList()) {
            Imgproc.circle(img, pt, 6, green, 1);
        }
        Imgcodecs.imwrite(filename, img);
    }

    public static void debug(int level, String msg, Mat m) {
        if (!DEBUG)
            return;
        if (level < LEVEL)
            return;
        System.out.println(msg);
        System.out.println(m.dump());
    }

    public static void debug(int level, String msg, double d) {
        if (!DEBUG)
            return;
        if (level < LEVEL)
            return;
        System.out.println(msg);
        System.out.println(d);
    }

}
