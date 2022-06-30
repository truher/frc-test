package vision;

import org.opencv.core.Mat;
import org.opencv.core.MatOfDouble;

/**
 * Derive robot pose from imagery.
 */
public interface PoseEstimator {
    public Mat[] getIntrinsicMatrices();

    public MatOfDouble[] getDistortionMatrices();

    // TODO: make this return transformation matrices instead. for now it should
    // return +- b/2
    public double[] getXOffsets();

    public Mat getPose(double heading, Mat[] images);
}
