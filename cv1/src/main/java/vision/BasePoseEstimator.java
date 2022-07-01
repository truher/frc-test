package vision;

import org.opencv.calib3d.Calib3d;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint2f;
import org.opencv.core.MatOfPoint3f;
import org.opencv.core.Size;
import org.opencv.imgcodecs.Imgcodecs;

public abstract class BasePoseEstimator implements PoseEstimator {
    final static Log log = new Log(4, BasePoseEstimator.class.getName());

    /**
     * includes untilting, which means i should (TODO) add a "tall" size here.
     */
    @Override
    public Mat getPose(int idx, boolean writeFiles, double heading, MatOfPoint3f targetPoints, Mat[] images) {
        MatOfPoint2f[] imagePointArray = new MatOfPoint2f[images.length];
        Mat[] intrinsics = getIntrinsicMatrices();
        Mat[] distortion = getDistortionMatrices();
        double[] f = getF();
        double[] tilt = getTilt();
        Size[] size = getSizes();
        for (int i = 0; i < images.length; ++i) {

            Mat cameraView = images[i];

            // manually undistort the camera view.
            Mat undistortedCameraView = new Mat();
            Calib3d.undistort(cameraView, undistortedCameraView, intrinsics[i], distortion[i]);
            if (writeFiles)
                Imgcodecs.imwrite(String.format("C:\\Users\\joelt\\Desktop\\pics\\target-%d-undistorted.png", idx),
                        undistortedCameraView);

            // for now use the same size/kmat as the real camera, which is not optimal.
            Mat untiltedCameraView = VisionUtil.removeTilt(undistortedCameraView, tilt[i], f[i], intrinsics[i],
                    size[i]);
            if (writeFiles)
                Imgcodecs.imwrite(String.format("C:\\Users\\joelt\\Desktop\\pics\\target-%d-raw.png", idx),
                        untiltedCameraView);

            VisionUtil.removeSaltAndPepperInPlace(untiltedCameraView);
            if (writeFiles)
                Imgcodecs.imwrite(String.format("C:\\Users\\joelt\\Desktop\\pics\\target-%d-denoised.png", idx),
                        untiltedCameraView);

            MatOfPoint2f imagePoints = VisionUtil.findTargetCornersInImage(idx, writeFiles, untiltedCameraView);
            if (imagePoints == null) {
                log.debugmsg(2, "no points");
                return null;
            }
            imagePointArray[i] = imagePoints;
        }
        return getPose(heading, targetPoints, imagePointArray);
    }
}
