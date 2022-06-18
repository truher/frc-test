import java.util.List;

import org.junit.Test;
import org.opencv.calib3d.Calib3d;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfDouble;
import org.opencv.core.MatOfPoint2f;
import org.opencv.core.MatOfPoint3f;
import org.opencv.core.Size;
import org.opencv.imgcodecs.Imgcodecs;

import vision.VisionUtil;

/**
 * Binocular vision to try to improve distant pose estimation.
 */
public class TestBinocular {
    public TestBinocular() {
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
    }

    @Test
    public void testSimple() {
        Size dsize = new Size(960, 540); // 1/4 of 1080, just to i can see it more easily
        Mat kMat = VisionUtil.makeIntrinsicMatrix(512.0, dsize);
        MatOfDouble dMat = new MatOfDouble(Mat.zeros(4, 1, CvType.CV_64F));

        // target is 0.4m wide, 0.1m high .
        double width = 0.4;
        double height = 0.1;
        MatOfPoint3f targetGeometryMeters = VisionUtil.makeTargetGeometry3f(width, height);
        System.out.println("target geometry");
        System.out.println(targetGeometryMeters.dump());
        List<Mat> objectPoints = List.of(targetGeometryMeters);

        // in meters, world coords
        double xPos = 0.0;
        double yPos = 0.0;
        double zPos = -4.0;
        // in radians
        double tilt = 0.0;
        double pan = 0.1;

        double base = 0.4; // 40cm camera separation (wide!)

        MatOfPoint2f leftPts = VisionUtil.getImagePoints(xPos - base / 2, yPos, zPos, tilt, pan, kMat, dMat,
                targetGeometryMeters);
        MatOfPoint2f rightPts = VisionUtil.getImagePoints(xPos + base / 2, yPos, zPos, tilt, pan, kMat, dMat,
                targetGeometryMeters);
        List<Mat> leftPointList = List.of(leftPts);
        List<Mat> rightPointList = List.of(rightPts);

        Mat leftView = VisionUtil.makeImage(xPos - base / 2, yPos, zPos, tilt, pan, kMat, dMat, targetGeometryMeters,
                dsize);
        Mat rightView = VisionUtil.makeImage(xPos + base / 2, yPos, zPos, tilt, pan, kMat, dMat, targetGeometryMeters,
                dsize);
        Imgcodecs.imwrite("C:\\Users\\joelt\\Desktop\\pics\\leftView.jpg", leftView);
        Imgcodecs.imwrite("C:\\Users\\joelt\\Desktop\\pics\\rightView.jpg", rightView);
        Mat R = new Mat();
        Mat T = new Mat();
        Mat E = new Mat();
        Mat F = new Mat();
        double reprojectionError = Calib3d.stereoCalibrate(objectPoints,
                leftPointList, rightPointList, kMat, dMat, kMat, dMat, dsize, R, T, E, F);
        System.out.println("reprojectionError");
        System.out.println(reprojectionError);
        System.out.println("R");
        System.out.println(R.dump());
        System.out.println("T");
        System.out.println(T.dump());
        System.out.println("E");
        System.out.println(E.dump());
        System.out.println("F");
        System.out.println(F.dump());

        Mat Rleft = new Mat(); // rectification (rotation) for left cam
        Mat Rright = new Mat();
        Mat Pleft = new Mat(); // projection (3d->2d)
        Mat Pright = new Mat();
        Mat Q = new Mat(); // disparity-to-depth
        Calib3d.stereoRectify(kMat, dMat, kMat, dMat, dsize, R, T, Rleft, Rright, Pleft, Pright, Q);
        System.out.println("R1");
        System.out.println(Rleft.dump());
        System.out.println("R2");
        System.out.println(Rright.dump());
        System.out.println("P1");
        System.out.println(Pleft.dump());
        System.out.println("P2");
        System.out.println(Pright.dump());
        System.out.println("Q");
        System.out.println(Q.dump());

        Mat points4D = new Mat(); // points in the left camera's rectified coords
        Calib3d.triangulatePoints(Pleft, Pright, leftPts, rightPts, points4D);
        System.out.println("points4D");
        System.out.println(points4D.dump());

        // hm this seems wrong
        Mat points3D = new Mat();
        Calib3d.convertPointsFromHomogeneous(points4D, points3D);
        System.out.println("points3D");
        System.out.println(points3D.dump());

        Mat rotM = new Mat(); // estimated rotation matrix
        Mat tV = new Mat(); // estimated translation vector
        Calib3d.recoverPose(E, leftPts, rightPts, kMat, rotM, tV);
        System.out.println("rotM");
        System.out.println(rotM.dump());
        System.out.println("tV");
        System.out.println(tV.dump());
    }

}
