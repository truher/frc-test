package vision;

import org.opencv.core.Mat;
import org.opencv.core.MatOfDouble;
import org.opencv.core.MatOfPoint2f;
import org.opencv.core.MatOfPoint3f;
import org.opencv.core.Size;

/**
 * Derive robot pose from imagery.
 */
public interface PoseEstimator {
    /** used in filenames */
    public String getName();

    public String getDescription();

    public Mat[] getIntrinsicMatrices();

    public MatOfDouble[] getDistortionMatrices();

    // TODO: make this return transformation matrices instead. for now it should
    // return +- b/2
    public double[] getXOffsets();

    public Size[] getSizes();

    // for now this operates on *points* found in images
    // TODO: make it operate on images, supply some sort of point-finding utility?
    public Mat getPose(double heading, MatOfPoint3f targetPoints, MatOfPoint2f[] imagePoints);

    // idx is for filenames for debugging
    public Mat getPose(int idx, double heading, MatOfPoint3f targetPoints, Mat[] images);
}
