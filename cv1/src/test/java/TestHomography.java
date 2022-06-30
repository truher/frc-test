import java.util.ArrayList;
import java.util.List;

import org.opencv.calib3d.Calib3d;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfDouble;
import org.opencv.core.MatOfPoint2f;
import org.opencv.core.MatOfPoint3f;
import org.opencv.core.Size;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

import vision.VisionUtil;

/**
 * Find the homography between target and image, extract transformations.
 * 
 * This didn't work but i didn't want to delete it.
 */
public class TestHomography {

    public static final double DELTA = 0.00001;
    public static final boolean DEBUG = false;

    public TestHomography() {
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
    }

    /**
     * Try many world locations; generate an image, extract pose from it.
     */
    //@Test
    public void testHomography() {
        Size dsize = new Size(1920, 1080);
        double f = 600.0;
        Mat kMat = VisionUtil.makeIntrinsicMatrix(f, dsize);

        // don't forget to measure distortion in a real camera
        // Note: distortion confuses pnpransac, use normal pnp instead
        // a bit of barrel
        // MatOfDouble dMat = new MatOfDouble(Mat.zeros(4, 1, CvType.CV_64F));
        // dMat.put(0, 0, -0.05, 0.0, 0.0, 0.0);
        // this is from github.com/SharadRawat/pi_sensor_setup
        // MatOfDouble dMat = new MatOfDouble(Mat.zeros(5, 1, CvType.CV_64F));
        // dMat.put(0, 0, 0.159, -0.0661, -0.00570, 0.0117, -0.503);
        // try a less agro version of that
        MatOfDouble dMat = new MatOfDouble(Mat.zeros(5, 1, CvType.CV_64F));
        //
        //
        // // is distortion broken?
        //
        dMat.put(0, 0, -0.1, 0, 0, 0, 0);
        double height = 0.1;
        double width = 0.4;
        MatOfPoint3f targetGeometryMeters = VisionUtil.makeTargetGeometry3f(width, height);

        final double dyWorld = 1.0; // say the camera is 1m below (+y) relative to the target
        final double tilt = 0.45; // camera tilts up
        final double pan = 0.0; // for now, straight ahead
        int idx = 0;

        // FRC field is 8x16m, let's try for half-length and full-width i.e. 8x8
        for (double dzWorld = -10; dzWorld <= -1; dzWorld += 1.0) { // meters, start far, move closer
            for (double dxWorld = -5; dxWorld <= 5; dxWorld += 1.0) { // meters, start left, move right

                idx += 1;
                Mat cameraView = VisionUtil.makeImage(dxWorld, dyWorld, dzWorld, tilt, pan, kMat, dMat,
                        targetGeometryMeters, dsize);
                if (cameraView == null) {
                    debugmsg("no camera view");
                    continue;
                }
                Imgcodecs.imwrite(String.format("C:\\Users\\joelt\\Desktop\\pics\\target-%d-distorted.png", idx),
                        cameraView);

                //
                // manually undistort the camera view.
                Mat undistortedCameraView = new Mat();
                Calib3d.undistort(cameraView, undistortedCameraView, kMat, dMat);
                Imgcodecs.imwrite(String.format("C:\\Users\\joelt\\Desktop\\pics\\target-%d-undistorted.png", idx),
                        undistortedCameraView);

                // try removing the camera tilt and using the camera y to make fake points.

                Mat invKMat = kMat.inv();
                Mat unTiltV = Mat.zeros(3, 1, CvType.CV_64F);
                unTiltV.put(0, 0, tilt, 0.0, 0.0);
                Mat unTiltM = new Mat();
                Calib3d.Rodrigues(unTiltV, unTiltM);

                Mat result = new Mat();

                Core.gemm(unTiltM, invKMat, 1.0, new Mat(), 0.0, result);

                // make a tall camera
                Size tallSize = new Size(1920, 2160);
                Mat tallKMat = VisionUtil.makeIntrinsicMatrix(f, tallSize);

                Core.gemm(tallKMat, result, 1.0, new Mat(), 0.0, result);
                debug("result", result);

                Mat untiltedCameraView = Mat.zeros(tallSize, CvType.CV_8UC3);
                Imgproc.warpPerspective(undistortedCameraView, untiltedCameraView, result, tallSize);

                Imgcodecs.imwrite(String.format("C:\\Users\\joelt\\Desktop\\pics\\target-%d-raw.png", idx),
                        untiltedCameraView);

                MatOfPoint2f imagePoints = VisionUtil.getImagePoints(idx, untiltedCameraView);
                if (imagePoints == null) {
                    debugmsg("no image points");
                    continue;
                }

                debug("imagePoints", imagePoints);

                MatOfPoint2f targetImageGeometry = VisionUtil.makeTargetImageGeometryPixels(targetGeometryMeters, 1000);

                Mat H = Calib3d.findHomography(targetImageGeometry, imagePoints);
                debug("H", H);
                List<Mat> rotations = new ArrayList<Mat>();
                List<Mat> translations = new ArrayList<Mat>();
                List<Mat> normals = new ArrayList<Mat>();
                Calib3d.decomposeHomographyMat(H, kMat, rotations, translations, normals);
                for (Mat m : translations) {
                    debug("m", m);
                }

            }
        }
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
