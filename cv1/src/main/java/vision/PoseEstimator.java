package vision;

import org.opencv.core.Mat;

/**
 * Derive robot pose from imagery.
 */
public interface PoseEstimator {
    public Mat[] getIntrinsicMatrices();

    public Mat[] getDistortionMatrices();

    public Mat getPose(double heading, Mat[] images);
}
