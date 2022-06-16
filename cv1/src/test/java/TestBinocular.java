import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import vision.VisionUtil;

import org.junit.Test;
import org.opencv.calib3d.Calib3d;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfByte;
import org.opencv.core.MatOfDouble;
import org.opencv.core.MatOfInt;
import org.opencv.core.MatOfPoint;
import org.opencv.core.MatOfPoint2f;
import org.opencv.core.MatOfPoint3f;
import org.opencv.core.Point;
import org.opencv.core.Point3;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.core.TermCriteria;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

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

        // in meters, world coords
        double xPos = 0.0;
        double yPos = 0.0;
        double zPos = -2.0;
        // in radians
        double tilt = 0.0;
        double pan = 0.0;

        double base = 0.4; // 40cm camera separation (wide!)

        Mat leftView = VisionUtil.makeImage(xPos - base / 2, yPos, zPos, tilt, pan, kMat, dMat, targetGeometryMeters,
                dsize);
        Mat rightView = VisionUtil.makeImage(xPos + base / 2, yPos, zPos, tilt, pan, kMat, dMat, targetGeometryMeters,
                dsize);
        Imgcodecs.imwrite("C:\\Users\\joelt\\Desktop\\pics\\leftView.jpg", leftView);
        Imgcodecs.imwrite("C:\\Users\\joelt\\Desktop\\pics\\rightView.jpg", rightView);
    }

}
