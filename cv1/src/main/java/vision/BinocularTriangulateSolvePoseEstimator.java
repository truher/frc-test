package vision;

import org.opencv.calib3d.Calib3d;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfDouble;
import org.opencv.core.MatOfPoint2f;
import org.opencv.core.MatOfPoint3f;
import org.opencv.core.Size;

/**
 * Uses triangulation to reconstruct the 3d points and then SVD to find a transform
 * to those points.  Doesn't work well, too many degrees of freedom.  I think there
 * is also something wrong with it, but it's not worth finding the problem.
 */
public class BinocularTriangulateSolvePoseEstimator implements PoseEstimator {
    public static final boolean DEBUG = false;
    final int height = 540; // c=270
    final int width = 960; // c=480
    final Size dsize = new Size(width, height);
    final double f = 256.0;
    final double base = 0.4;

    @Override
    public String getName() {
        return "BinocularTriangulateSolvePoseEstimator";
    }

    @Override
    public String getDescription() {
        return "Triangulate and then solve using Core.solve";
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

        Mat predictedHomogeneousNormalized = new Mat();
        Calib3d.convertPointsToHomogeneous(predictedNormal, predictedHomogeneousNormalized);

        predictedHomogeneousNormalized = predictedHomogeneousNormalized.reshape(1);
        debug("predictedHomogeneousNormalized", predictedHomogeneousNormalized);
        Mat homogeneousTarget = new Mat();
        Calib3d.convertPointsToHomogeneous(targetPoints, homogeneousTarget);
        homogeneousTarget = homogeneousTarget.reshape(1);
        debug("homogeneousTarget", homogeneousTarget);
        Mat A = new Mat();
        Core.solve(predictedHomogeneousNormalized, homogeneousTarget, A, Core.DECOMP_SVD);
        A.convertTo(A, CvType.CV_64F);
        return A.t().submat(0, 3, 0, 4);
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
