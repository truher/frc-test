package vision;

import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfDouble;

/**
 * Always returns the origin.
 */
public class BadPoseEstimator implements PoseEstimator {

    @Override
    public MatOfDouble[] getDistortionMatrices() {
        MatOfDouble dMat = new MatOfDouble(Mat.zeros(4, 1, CvType.CV_64F));
        return new MatOfDouble[] { dMat };
    }

    @Override
    public Mat[] getIntrinsicMatrices() {
        Mat kMat = Mat.zeros(3, 3, CvType.CV_64F);
        kMat.put(0, 0,
                1, 0, 0,
                0, 1, 0,
                0, 0, 1);
        return new Mat[] { kMat };
    }

    @Override
    public Mat getPose(double heading, Mat[] images) {
        Mat pose = Mat.zeros(3, 4, CvType.CV_64F);
        pose.put(0, 0,
                1, 0, 0, 0,
                0, 1, 0, 0,
                0, 0, 1, 0);
        return pose;
    }

    @Override
    public double[] getXOffsets() {
        return new double[] { 0 };
    }

}
