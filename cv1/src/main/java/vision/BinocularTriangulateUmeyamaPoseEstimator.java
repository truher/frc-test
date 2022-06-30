package vision;

import org.opencv.calib3d.Calib3d;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfDouble;
import org.opencv.core.MatOfPoint2f;
import org.opencv.core.MatOfPoint3f;
import org.opencv.core.Size;

/**
 * Similar to the other triangulation estimator, but uses a slightly constrained
 * solver, the Umeyama SVD method. Still doesn't work well, still there's
 * probably something broken here.
 */
public class BinocularTriangulateUmeyamaPoseEstimator implements PoseEstimator {
    public static final boolean DEBUG = false;
    final int height = 540; // c=270
    final int width = 960; // c=480
    final Size dsize = new Size(width, height);
    final double f = 256.0;
    final double base = 0.4;

    @Override
    public String getName() {
        return "BinocularTriangulateUmeyamaPoseEstimator";
    }

    @Override
    public String getDescription() {
        return "Triangulate and then solve using Umeyama method";
    }

    @Override
    public Mat[] getIntrinsicMatrices() {
        Mat kMat = VisionUtil.makeIntrinsicMatrix(f, dsize);
        return new Mat[] { kMat, kMat };
    }

    @Override
    public MatOfDouble[] getDistortionMatrices() {
        MatOfDouble dMat = new MatOfDouble(Mat.zeros(4, 1, CvType.CV_64F));
        return new MatOfDouble[] { dMat, dMat };
    }

    @Override
    public double[] getXOffsets() {
        return new double[] { base / 2, -base / 2 };
    }

    @Override
    public Size[] getSizes() {
        return new Size[] { dsize, dsize };
    }

    @Override
    public Mat getPose(double heading, MatOfPoint3f targetPoints, MatOfPoint2f[] imagePoints) {
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
        MatOfPoint2f leftPts = imagePoints[0];
        MatOfPoint2f rightPts = imagePoints[1];

        Mat predictedHomogeneous = new Mat();
        Calib3d.triangulatePoints(Pleft, Pright, leftPts, rightPts, predictedHomogeneous);
        Mat predictedNormal = new Mat();
        Calib3d.convertPointsFromHomogeneous(predictedHomogeneous.t(), predictedNormal);

        Mat affineTransform = MyCalib3d.estimateAffine3D(predictedNormal, targetPoints, null,
                true);

        return affineTransform.submat(0, 3, 0, 4);

        // // i think this is in the wrong coordinates, so invert.
        // Mat homogeneousAffineTransform = Mat.zeros(4, 4, CvType.CV_64F);
        // affineTransform.copyTo(homogeneousAffineTransform.submat(0, 3, 0, 4));
        // homogeneousAffineTransform.put(3, 3, 1.0);
        // return homogeneousAffineTransform.inv().submat(0, 3, 0, 4);

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
