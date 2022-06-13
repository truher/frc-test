package vision;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.opencv.calib3d.Calib3d;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfInt;
import org.opencv.core.MatOfPoint;
import org.opencv.core.MatOfPoint2f;
import org.opencv.core.MatOfPoint3f;
import org.opencv.core.Point;
import org.opencv.core.Point3;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

public abstract class VisionUtil {
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

    /**
     * find the target corners, in the supplied image, upper-left first, then
     * clockwise.
     * 
     * TODO: denoising etc
     * 
     * @param rawCameraView unprocessed image
     * @return 2d geometry of the corners, in the image
     */
    public static MatOfPoint2f getImagePoints(Mat rawCameraView) {

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

        if (contours.size() != 1)
            return null;

        // TODO: remove this
        {
            Mat contourView2 = Mat.zeros(cameraView.size(), CvType.CV_8U);
            Imgproc.drawContours(contourView2, contours, 0, new Scalar(255, 0, 0));
            Imgcodecs.imwrite("C:\\Users\\joelt\\Desktop\\contours.jpg", contourView2);
        }

        MatOfPoint2f curve = new MatOfPoint2f(contours.get(0).toArray());
        MatOfPoint2f approxCurve = new MatOfPoint2f();
        Imgproc.approxPolyDP(curve, approxCurve, 5, true);
        MatOfPoint points = new MatOfPoint(approxCurve.toArray());
        // System.out.println("points");
        // System.out.println(points.dump());

        // TODO: remove this
        {
            Mat contourView = Mat.zeros(cameraView.size(), CvType.CV_8U);
            Imgproc.drawContours(contourView, List.of(points), 0, new Scalar(255, 0, 0));
            Imgcodecs.imwrite("C:\\Users\\joelt\\Desktop\\poly.jpg", contourView);
        }

        if (approxCurve.toList().size() != 4)
            return null;

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

}
