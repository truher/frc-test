package vision;

import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint2f;
import org.opencv.core.MatOfPoint3f;

public abstract class BasePoseEstimator implements PoseEstimator {
    @Override
    public Mat getPose(int idx, double heading, MatOfPoint3f targetPoints, Mat[] images) {
        MatOfPoint2f[] imagePointArray = new MatOfPoint2f[images.length];
        for (int i = 0; i < images.length; ++i) {
            MatOfPoint2f imagePoints = VisionUtil.findTargetCornersInImage(idx, images[i]);
            if (imagePoints == null) {
                return null;
            }
            imagePointArray[i] = imagePoints;
        }
        return getPose(heading, targetPoints, imagePointArray);
    }
}
