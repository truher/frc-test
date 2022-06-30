import static org.junit.Assert.assertEquals;

import java.util.Random;
import java.util.function.BinaryOperator;
import java.util.function.Function;

import org.junit.Test;
import org.opencv.calib3d.Calib3d;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfDouble;
import org.opencv.core.MatOfPoint2f;
import org.opencv.core.MatOfPoint3f;
import org.opencv.core.Rect;
import org.opencv.core.Size;

import vision.VisionUtil;

/**
 * Solve the 3d binocular pose estimation problem constrained to rotation and
 * translation in the XZ plane.
 */
public class TestBinocularConstrained {
    static {
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
    }
    static final boolean DEBUG = false;
    static final int LEVEL = 1;
    static final double DELTA = 0.001;

    //@Test
    public void testHomogeneous() {
        MatOfPoint3f targetGeometryMeters = VisionUtil.makeTarget(-0.2, -0.1, 0.2, 0.0);
        debug(1, "target", targetGeometryMeters);
        Mat XMat = makeXMat3d(targetGeometryMeters);
        debug(1, "homogeneous target", XMat);
        System.out.println(XMat.size());
    }

    //@Test
    public void testReproject() {
        Random rand = new Random(42);
        final double f = 985; // 2.8mm lens
        final int height = 800;
        final int width = 1280;
        final int cx = width / 2;
        final int cy = height / 2;
        final Size dsize = new Size(width, height);
        final Rect viewport = new Rect(0, 0, width, height);
        final Mat kMat = VisionUtil.makeIntrinsicMatrix(f, dsize);
        final MatOfDouble dMat = new MatOfDouble(Mat.zeros(4, 1, CvType.CV_64F));
        // MatOfPoint3f targetGeometryMeters = VisionUtil.makeTarget(-0.2, -0.1, 0.2,
        // 0.0);
        MatOfPoint3f targetGeometryMeters = VisionUtil.makeTarget(-0.25, 0, 0.25, -0.5);
        int pointMultiplier = 1;
        double noisePixels = 1;
        MatOfPoint3f targetPointsMultiplied = VisionUtil.duplicatePoints(targetGeometryMeters, pointMultiplier);

        final double b = 0.4; // camera width meters
        // double pan = 0;
        // double xPos = 0;
        // double pan = Math.PI / 4;
        // double xPos = -1;
        double yPos = 0;
        // double zPos = -3;
        long startTime = System.currentTimeMillis();
        int idx = 0;

        double panErrSquareSum = 0.0;
        double xErrSquareSum = 0.0;
        double zErrSquareSum = 0.0;
        double relativeBearingErrSquareSum = 0.0;
        double rangeErrSquareSum = 0.0;

        System.out.println(
                "idx, pan, xpos, ypos, zpos, rbear, range, ppan, pxpos, pypos, pzpos, prbear, prange, panErr, xErr, zErr, relativeBearingErr, rangeErr");

        for (double pan = -3 * Math.PI / 8; pan <= 3 * Math.PI / 8; pan += Math.PI / 8) {

            // field is 16m long, say half the field is relevant
            for (double zPos = -10.0; zPos <= -1.0; zPos += 1.0) {
                // field is 8m wide, so +/- 4m

                for (double xPos = -5; xPos <= 5; xPos += 1.0) {




                    double navBearing = Math.atan2(xPos, -zPos);
                    double relativeBearing = navBearing + pan;
                    double range = Math.sqrt(xPos * xPos + zPos * zPos);

                    // don't bother with oblique angles, the projection is wrong for these cases.
                    if (Math.abs(relativeBearing) > Math.PI / 2)
                        continue;

                    // make transform from world origin to camera center
                    Mat worldToCameraHomogeneous = VisionUtil.makeWorldToCameraHomogeneous(pan, xPos, yPos, zPos);

                    // apply transform from camera center to each eye
                    Mat worldToLeftEye = VisionUtil.translateX(worldToCameraHomogeneous, b / 2);
                    Mat worldToRightEye = VisionUtil.translateX(worldToCameraHomogeneous, -b / 2);
                    debug(0, "worldToLeftEye", worldToLeftEye);
                    debug(0, "worldToRightEye", worldToRightEye);

                    // make images based on target points and transforms
                    MatOfPoint2f leftPts = VisionUtil.imagePoints(kMat, dMat, targetGeometryMeters, worldToLeftEye,
                            pointMultiplier,
                            noisePixels, rand);
                    MatOfPoint2f rightPts = VisionUtil.imagePoints(kMat, dMat, targetGeometryMeters, worldToRightEye,
                            pointMultiplier, noisePixels, rand);
                    if (!VisionUtil.inViewport(leftPts, viewport) || !VisionUtil.inViewport(rightPts, viewport))
                        continue;

                    ++idx;
                    VisionUtil.writePng(leftPts, width, height,
                            String.format("C:\\Users\\joelt\\Desktop\\pics\\reproject-%d-left.png", idx));
                    VisionUtil.writePng(rightPts, width, height,
                            String.format("C:\\Users\\joelt\\Desktop\\pics\\reproject-%d-right.png", idx));

                    // To solve Ax=b triangulation, first make b (the points in each eye,
                    // transformed back into XYZ):
                    Mat bMat = makeBMat3d(leftPts, rightPts, f, cx, cy, b);
                    debug(1, "bMat", bMat);

                    // ... and the target x: (X, Y, Z, 1):
                    Mat XMat = makeXMat3d(targetPointsMultiplied);
                    // XMat = XMat.t();
                    debug(1, "XMat", XMat);

                    // Ax=b : A(from) = (to)
                    // now cribbing from the Umeyama thing:
                    // first discard the homogeneous part
                    Mat from = new Mat();
                    Calib3d.convertPointsFromHomogeneous(XMat, from);
                    // points in rows
                    from = from.reshape(1);
                    debug(1, "from", from);
                    // System.out.println(from.type());
                    // System.out.println(from.size());

                    final int count = from.checkVector(3); // 4 points
                    // System.out.println(count);

                    Mat to = new Mat();
                    Calib3d.convertPointsFromHomogeneous(bMat.t(), to);
                    // points in rows
                    to = to.reshape(1);
                    debug(1, "to", to);
                    // System.out.println(to.type());
                    // System.out.println(to.size());
                    // System.out.println(to.checkVector(3));

                    final double one_over_n = 1. / count;
                    debug(1, "one over n", one_over_n);

                    // yields a 3x1 vector (row) containing the means of each column
                    final Function<Mat, Mat> colwise_mean = /* [one_over_n] */(final Mat m) -> {
                        Mat my = new Mat();
                        Core.reduce(m, my, 0, Core.REDUCE_SUM, CvType.CV_64F);
                        // return my * one_over_n;
                        return my.mul(Mat.ones(my.size(), my.type()), one_over_n);
                    };

                    // just subtracts the "mean" from the rows in A.
                    final BinaryOperator<Mat> demean = /* [count] */(final Mat A, final Mat mean) -> {
                        Mat A_centered = Mat.zeros(count, 3, CvType.CV_64F);
                        for (int i = 0; i < count; i++) { // i = columns == points
                            Mat foo = new Mat();
                            Core.subtract(A.row(i), mean, foo);
                            for (int j = 0; j < 3; j++) {
                                // A_centered.row(i) = A.row(i) - mean;
                                double d = foo.get(0, j)[0];
                                A_centered.put(i, j, d);
                            }
                        }
                        return A_centered;
                    };

                    // these are the centroids of the actual and projected points
                    Mat from_mean = colwise_mean.apply(from); // 3x1 vector of col means
                    debug(1, "from mean", from_mean);
                    Mat to_mean = colwise_mean.apply(to); // 3x1 vector of col means
                    debug(1, "to mean", to_mean);

                    // translate the from and to vectors so mean is zero
                    Mat from_centered = demean.apply(from, from_mean);
                    debug(1, "from centered", from_centered);
                    Mat to_centered = demean.apply(to, to_mean);
                    debug(1, "to centered", to_centered);

                    Mat cov = new Mat();
                    Core.gemm(to_centered.t(), from_centered, one_over_n, new Mat(), 0.0, cov);
                    debug(1, "cov", cov);

                    // try reprojecting
                    Mat reproj = new Mat();
                    Mat eye = Mat.eye(3, 3, CvType.CV_64F);
                    Core.gemm(eye, from_centered.t(), 1.0, new Mat(), 0.0, reproj);
                    debug(1, "reproj without rotation", reproj.t());
                    debug(1, "to centered", to_centered);

                    double averageAngleDiff = averageAngularError(to_centered, reproj.t());

                    debug(1, "averageAngleDiff", averageAngleDiff);

                    // fix the transform

                    double c = Math.cos(averageAngleDiff);
                    double s = Math.sin(averageAngleDiff);
                    Mat newR = Mat.zeros(3, 3, CvType.CV_64F);
                    newR.put(0, 0,
                            c, 0, -s,
                            0, 1, 0,
                            s, 0, c);

                    // try reprojecting again
                    // Core.gemm(newR, from_centered.t(), 1.0, new Mat(), 0.0, reproj);
                    // debug(1, "reproj with rotation", reproj.t());
                    // debug(1, "to centered", to_centered);

                    ////////////

                    Mat rmat = newR;
                    Mat new_to = new Mat();
                    Core.gemm(rmat, from_mean.t(), 1.0, new Mat(), 0.0, new_to);

                    Mat transform = Mat.zeros(3, 4, CvType.CV_64F);
                    Mat r_part = transform.submat(0, 3, 0, 3);
                    rmat.copyTo(r_part);
                    // transform.col(3) = to_mean.t() - new_to;
                    Mat t_part = transform.col(3);
                    Mat tmat = new Mat();
                    Core.subtract(to_mean.t(), new_to, tmat);
                    tmat.copyTo(t_part);
                    debug(1, "transform", transform);

                    Mat homogeneousTransform = Mat.zeros(4, 4, CvType.CV_64F);
                    transform.copyTo(homogeneousTransform.submat(0, 3, 0, 4));
                    homogeneousTransform.put(3, 3, 1.0);
                    debug(1, "homogeneousTransform", homogeneousTransform);

                    // Core.gemm(homogeneousTransform, XMat.t(), 1.0, new Mat(), 0.0, reproj);
                    // debug(1, "reproj", reproj);
                    // debug(1, "bMat", bMat);

                    // double err = reprojectionError(bMat, reproj);
                    // debug(1, "err", err);

                    // now find the actual transform

                    double euler = Math.atan2(rmat.get(2, 0)[0], rmat.get(0, 0)[0]);
                    debug(1, "euler", euler);
                    Mat cameraTVec = Mat.zeros(3, 1, CvType.CV_64F);
                    cameraTVec.put(0, 0,
                            transform.get(0, 3)[0],
                            transform.get(1, 3)[0],
                            transform.get(2, 3)[0]);
                    debug(1, "cameraTVec", cameraTVec);
                    Mat pworldTVec = new Mat();
                    debug(1, "rmat", rmat);
                    Core.gemm(rmat.t(), cameraTVec, -1.0, new Mat(), 0.0, pworldTVec);
                    debug(1, "pWorldTVec", pworldTVec);





                    double pxPos = pworldTVec.get(0, 0)[0];
                    double pyPos = pworldTVec.get(1, 0)[0];
                    double pzPos = pworldTVec.get(2, 0)[0];
                    double ppan = euler;

                    double pNavBearing = Math.atan2(pxPos, -pzPos);
                    double pRelativeBearing = pNavBearing + ppan;
                    double pRange = Math.sqrt(pxPos * pxPos + pzPos * pzPos);

                    double panErr = pan - ppan;
                    double xErr = xPos - pxPos;
                    double zErr = zPos - pzPos;
                    double relativeBearingErr = relativeBearing - pRelativeBearing;
                    double rangeErr = range - pRange;

                    panErrSquareSum += panErr * panErr;
                    xErrSquareSum += xErr * xErr;
                    zErrSquareSum += zErr * zErr;
                    relativeBearingErrSquareSum += relativeBearingErr * relativeBearingErr;
                    rangeErrSquareSum += rangeErr * rangeErr;

                    System.out.printf(
                            "%d, %5.2f, %5.2f, %5.2f, %5.2f, %5.2f, %5.2f, %5.2f, %5.2f, %5.2f, %5.2f, %5.2f, %5.2f, %5.2f, %5.2f, %5.2f, %7.4f, %5.2f\n",
                            idx, pan, xPos, yPos, zPos, relativeBearing, range, ppan, pxPos, pyPos, pzPos,
                            pRelativeBearing, pRange,
                            panErr, xErr, zErr, relativeBearingErr, rangeErr);

                }
            }
        }

        double panRMSE = Math.sqrt(panErrSquareSum / idx);
        double xRMSE = Math.sqrt(xErrSquareSum / idx);
        double zRMSE = Math.sqrt(zErrSquareSum / idx);
        double relativeBearingRMSE = Math.sqrt(relativeBearingErrSquareSum / idx);
        double rangeRMSE = Math.sqrt(rangeErrSquareSum / idx);
        System.out.printf("panRMSE %f\n", panRMSE);
        System.out.printf("xRMSE %f\n", xRMSE);
        System.out.printf("zRMSE %f\n", zRMSE);
        System.out.printf("relativeBearingRMSE %f\n", relativeBearingRMSE);
        System.out.printf("rangeRMSE %f\n", rangeRMSE);

        long endTime = System.currentTimeMillis();
        System.out.println(endTime - startTime);

    }

    static double averageAngularError(Mat a, Mat b) {
        // a and b are centered around the origin and the same size
        debug(1, "a", a);
        debug(1, "b", b);
        // System.out.println(a.size());
        // System.out.println(b.size());
        assertEquals(a.size().height, b.size().height, DELTA);
        assertEquals(a.size().width, b.size().width, DELTA);
        for (int i = 0; i < a.rows(); ++i) {
            Mat avec = a.row(i);
            Mat bvec = b.row(i);
            debug(1, "avec", avec);
            debug(1, "bvec", bvec);
        }
        Mat ax = a.col(0);
        Mat az = a.col(2);
        debug(1, "ax", ax);
        debug(1, "az", az);

        Mat amag = new Mat();
        Mat aang = new Mat();
        Core.cartToPolar(ax, az, amag, aang);
        debug(1, "a magnitude", amag);
        debug(1, "a angle", aang);

        Mat bx = b.col(0);
        Mat bz = b.col(2);
        debug(1, "bx", bx);
        debug(1, "bz", bz);

        Mat bmag = new Mat();
        Mat bang = new Mat();
        Core.cartToPolar(bx, bz, bmag, bang);
        debug(1, "b magnitude", bmag);
        debug(1, "b angle", bang);

        Mat diff = new Mat();
        Core.subtract(aang, bang, diff);
        debug(1, "diff", diff);
        for (int i = 0; i < diff.rows(); ++i) {
            double val = diff.get(i, 0)[0];
            if (val > Math.PI) {
                val -= 2 * Math.PI;
            } else if (val < -Math.PI) {
                val += 2 * Math.PI;
            }
            diff.put(i, 0, val);
        }
        debug(1, "diff", diff);
        return Core.mean(diff).val[0];
    }

    static double reprojectionError(Mat a, Mat b) {
        Mat c = new Mat();
        Core.absdiff(a, b, c);
        return Core.sumElems(c).val[0];
    }

    /**
     * this just returns a homogeneous version of the argument, note points are in
     * rows in both input and output.
     */
    static Mat makeXMat3d(MatOfPoint3f targetPointsMultiplied) {
        Mat dst = new Mat();
        Calib3d.convertPointsToHomogeneous(targetPointsMultiplied, dst);
        dst = dst.reshape(1);
        dst.convertTo(dst, CvType.CV_64F);
        return dst;
    }

    /**
     * for horizontal binocular, v is the same in both eyes, so use (u, u',
     * (v+v')/2, 1).
     * Note inputs (leftPts) have points in rows; this method returns points in
     * columns.
     */
    static Mat makeUMat3d(MatOfPoint2f leftPts, MatOfPoint2f rightPts) {
        debug(1, "left", leftPts);
        debug(1, "right", rightPts);
        // System.out.println(leftPts.size());
        Mat uMat = Mat.zeros(4, leftPts.toList().size(), CvType.CV_64F);
        for (int i = 0; i < leftPts.toList().size(); ++i) {
            double u = leftPts.get(i, 0)[0];
            double u1 = rightPts.get(i, 0)[0];
            double v = leftPts.get(i, 0)[1];
            double v1 = rightPts.get(i, 0)[1];

            uMat.put(0, i, u);
            uMat.put(1, i, u1);
            uMat.put(2, i, (v + v1) / 2);
            uMat.put(3, i, 1.0);
        }
        debug(0, "uMat", uMat);
        return uMat;
    }

    /**
     * inverse camera matrix with two u rows for two eyes
     */
    static Mat makeMInv3d(double f, double cx, double cy) {
        Mat M = Mat.zeros(4, 4, CvType.CV_64F);
        M.put(0, 0,
                f, 0, 0, cx,
                0, f, 0, cx,
                0, 0, f, cy,
                0, 0, 0, 1);
        debug(0, "M", M);
        Mat Minv = M.inv();
        debug(0, "Minv", Minv);
        return Minv;
    }

    /**
     * inverse translation and projection for two eyes (horizontal)
     */
    static Mat makeTInv3d(double b) {
        Mat T = Mat.zeros(4, 4, CvType.CV_64F);
        T.put(0, 0,
                1, 0, 0, b / 2,
                1, 0, 0, -b / 2,
                0, 1, 0, 0,
                0, 0, 1, 0);
        debug(0, "T", T);
        Mat Tinv = T.inv();
        debug(0, "Tinv", Tinv);
        return Tinv;
    }

    /**
     * Scale so that each column looks like a homogeneous 3d vector, i.e. with 1 in
     * the last place.
     */
    static void normalize3d(Mat m) {
        for (int col = 0; col < m.cols(); ++col) {
            double xval = m.get(0, col)[0];
            double yval = m.get(1, col)[0];
            double zval = m.get(2, col)[0];
            double scaleVal = m.get(3, col)[0];
            m.put(0, col, xval / scaleVal);
            m.put(1, col, yval / scaleVal);
            m.put(2, col, zval / scaleVal);
            m.put(3, col, 1.0);
        }
    }

    static Mat makeBMat3d(MatOfPoint2f leftPts, MatOfPoint2f rightPts, double f, double cx, double cy, double b) {
        // To solve Ax=b triangulation (Ax=M-1T-1u), first make u: (u,u',v,1):
        Mat uMat = makeUMat3d(leftPts, rightPts);

        // and the inverse transforms we're going to apply:
        Mat Minv = makeMInv3d(f, cx, cy);
        Mat Tinv = makeTInv3d(b);

        // apply the inverses to the observations (the "u") in the correct order:
        Mat MinvU = new Mat();
        Core.gemm(Minv, uMat, 1.0, new Mat(), 0.0, MinvU);
        Mat bMat = new Mat();
        Core.gemm(Tinv, MinvU, 1.0, new Mat(), 0.0, bMat);
        normalize3d(bMat);
        debug(0, "bMat normalized", bMat);
        return bMat;
    }

    public static void debug(int level, String msg, Mat m) {
        if (!DEBUG)
            return;
        if (level < LEVEL)
            return;
        System.out.println(msg);
        System.out.println(m.dump());
    }

    public static void debug(int level, String msg, double d) {
        if (!DEBUG)
            return;
        if (level < LEVEL)
            return;
        System.out.println(msg);
        System.out.println(d);
    }
}
