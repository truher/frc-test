package vision;

import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfDouble;
import org.opencv.core.Size;

public class BinocularConstrainedPoseEstimator implements PoseEstimator {
    final double f = 985; // 2.8mm lens
    final int height = 800;
    final int width = 1280;
    final int cx = width / 2;
    final int cy = height / 2;
    final Size dsize = new Size(width, height);

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
    public Mat getPose(double heading, Mat[] images) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public double[] getXOffsets() {
        // TODO Auto-generated method stub
        return null;
    }

}
