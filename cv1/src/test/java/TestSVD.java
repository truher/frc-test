import java.util.ArrayList;
import java.util.List;
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
import org.opencv.core.Point;
import org.opencv.core.Point3;
import org.opencv.core.Rect;
import org.opencv.core.Size;
import vision.VisionUtil;

/**
 * playing with SVD using a two-dimensional representation to avoid the kinds of
 * errors that seem to happen where freedom in Y yields errors in the other
 * axes.
 */

public class TestSVD {
    static {
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
    }
    static final boolean DEBUG = false;
    static final int LEVEL = 1;

    static final Random rand = new Random(42);
    // this is the arducam ov9281 thing
    static final double f = 914;
    static final int height = 800;
    static final int width = 1280;
    static final Size dsize = new Size(width, height);
    static final Mat kMat = VisionUtil.makeIntrinsicMatrix(f, dsize);
    static final MatOfDouble dMat = new MatOfDouble(Mat.zeros(4, 1, CvType.CV_64F));
    static final int pointMultiplier = 1;
    static final double noisePixels = 1;
    static final int cx = width / 2;
    static final Rect viewport = new Rect(0, 0, width, height);

    MatOfPoint3f targetGeometryMeters;
    MatOfPoint3f targetPointsMultiplied;
    // Mat homogeneousTarget;

    public TestSVD() {
        debug(0, "kMat", kMat);
        debug(0, "dMat", dMat);

    }

    static void normalize(Mat TinvMinvBmat) {
        for (int col = 0; col < TinvMinvBmat.cols(); ++col) {
            double xval = TinvMinvBmat.get(0, col)[0];
            double zval = TinvMinvBmat.get(1, col)[0];
            double scaleVal = TinvMinvBmat.get(2, col)[0];
            TinvMinvBmat.put(0, col, xval / scaleVal);
            TinvMinvBmat.put(1, col, zval / scaleVal);
            TinvMinvBmat.put(2, col, 1.0);
        }
        debug(0, "TinvMinvBmat (scaled)", TinvMinvBmat);
    }

    // @Test
    public void testNothing() {
    }

    /**
     * this is for the arducam OV9281 default M12 lens
     */
    // @Test
    public void testFOV() {
        final double myF = 914;
        final int myHeight = 800;
        final int myWidth = 1280;
        final Size mySize = new Size(myWidth, myHeight);
        Mat myKMat = VisionUtil.makeIntrinsicMatrix(myF, mySize);
        double[] fovx = new double[1];
        double[] fovy = new double[1];
        double[] focalLength = new double[1];
        Point principalPoint = new Point();
        double[] aspectRatio = new double[1];

        // this is the 1/2.7" OV9281 sensor size in mm
        double apertureWidth = 5.37;
        double apertureHeight = 4.04;
        Calib3d.calibrationMatrixValues(myKMat, mySize, apertureWidth, apertureHeight, fovx, fovy, focalLength,
                principalPoint, aspectRatio);
        System.out.printf("fovx %f, fovy %f, focalLength %f, principalPoint x %f y %f, aspectRatio %f\n",
                fovx[0], fovy[0], focalLength[0], principalPoint.x, principalPoint.y, aspectRatio[0]);

    }

    // @Test
    public void testSolve() {
        targetGeometryMeters = VisionUtil.makeTarget(-0.2, -0.1, 0.2, 0.0);
        targetPointsMultiplied = duplicatePoints(targetGeometryMeters, pointMultiplier);
        // homogeneousTarget = homogenize(targetPointsMultiplied);
        // A "big" robot is ~0.8m wide, cameras can't be wider than that
        // Later: test sensitivity of width
        final double b = 0.8;
        // camera doesn't move vertically.
        final double yPos = 0;

        long startTime = System.currentTimeMillis();
        int idx = 0;

        double panErrSquareSum = 0.0;
        double xErrSquareSum = 0.0;
        double zErrSquareSum = 0.0;
        double relativeBearingErrSquareSum = 0.0;
        double rangeErrSquareSum = 0.0;

        System.out.println(
                "idx, pan, xpos, ypos, zpos, rbear, range, ppan, pxpos, pypos, pzpos, prbear, prange, panErr, xErr, zErr, relativeBearingErr, rangeErr");
        // pan is a world transformation i.e. positive means turning the camera to the
        // right
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
                            pointMultiplier, noisePixels);
                    MatOfPoint2f rightPts = VisionUtil.imagePoints(kMat, dMat, targetGeometryMeters, worldToRightEye,
                            pointMultiplier, noisePixels);
                    if (!VisionUtil.inViewport(leftPts, viewport) || !VisionUtil.inViewport(rightPts, viewport))
                        continue;

                    ++idx;
                    VisionUtil.writePng(leftPts, width, height,
                            String.format("C:\\Users\\joelt\\Desktop\\pics\\svd-%d-left.png", idx));
                    VisionUtil.writePng(rightPts, width, height,
                            String.format("C:\\Users\\joelt\\Desktop\\pics\\svd-%d-right.png", idx));

                    // To solve Ax=b triangulation, first make b:
                    Mat bMat = makeBMat(leftPts, rightPts, b);

                    // ... and x: (X, Z, 1):
                    Mat XMat = makeXMat();
                    debug(0, "XMat (x data)", XMat);

                    // so now Ax=b where X is the world geometry and b is as prepared.
                    Mat AA = solve(XMat, bMat);

                    // ah this is the reverse transform so to get the world coords
                    // i need to transform back.
                    Mat rmat = AA.submat(0, 2, 0, 2);
                    debug(1, "rmat", rmat);
                    // this often returns nonrotations so fix it? hm.
                    double euler = VisionUtil.rotm2euler2d(rmat);
                    debug(0, "euler", euler);
                    rmat.put(0, 0,
                            Math.cos(euler), -Math.sin(euler),
                            Math.sin(euler), Math.cos(euler));
                    debug(1, "rmat repaired", rmat);

                    // Mat worldRvec = new Mat();
                    // Calib3d.Rodrigues(rmat, worldRvec);
                    Mat cameraTVec = Mat.zeros(2, 1, CvType.CV_64F);
                    cameraTVec.put(0, 0, AA.get(0, 2)[0], AA.get(1, 2)[0]);
                    debug(1, "cameraTVec", cameraTVec);
                    Mat pworldTVec = new Mat();

                    Core.gemm(rmat.t(), cameraTVec, -1.0, new Mat(), 0.0, pworldTVec);
                    debug(1, "pWorldTVec", pworldTVec);

                    double pxPos = pworldTVec.get(0, 0)[0];
                    // double pxPos = cameraTVec.get(0, 0)[0];
                    double pyPos = yPos;
                    double pzPos = pworldTVec.get(1, 0)[0];
                    // double pzPos = cameraTVec.get(1, 0)[0];
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

    @Test
    public void testUmeyama() {
        targetGeometryMeters = VisionUtil.makeTarget(-0.2, -0.1, 0.2, 0.0);
        targetPointsMultiplied = duplicatePoints(targetGeometryMeters, pointMultiplier);
        // homogeneousTarget = homogenize(targetPointsMultiplied);
        // A "big" robot is ~0.8m wide, cameras can't be wider than that
        // Later: test sensitivity of width
        final double b = 0.8;
        // camera doesn't move vertically.
        final double yPos = 0;
        long startTime = System.currentTimeMillis();
        int idx = 0;

        double panErrSquareSum = 0.0;
        double xErrSquareSum = 0.0;
        double zErrSquareSum = 0.0;
        double relativeBearingErrSquareSum = 0.0;
        double rangeErrSquareSum = 0.0;

        // System.out.println("idx, pan, xpos, ypos, zpos, ppan, pxpos, pypos, pzpos");
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
                            pointMultiplier, noisePixels);
                    MatOfPoint2f rightPts = VisionUtil.imagePoints(kMat, dMat, targetGeometryMeters, worldToRightEye,
                            pointMultiplier, noisePixels);
                    if (!VisionUtil.inViewport(leftPts, viewport) || !VisionUtil.inViewport(rightPts, viewport))
                        continue;

                    ++idx;
                    VisionUtil.writePng(leftPts, width, height,
                            String.format("C:\\Users\\joelt\\Desktop\\pics\\svd-%d-left.png", idx));
                    VisionUtil.writePng(rightPts, width, height,
                            String.format("C:\\Users\\joelt\\Desktop\\pics\\svd-%d-right.png", idx));

                    // To solve Ax=b triangulation, first make b:
                    Mat bMat = makeBMat(leftPts, rightPts, b);

                    // ... and x: (X, Z, 1):
                    Mat XMat = makeXMat();
                    debug(0, "XMat (x data)", XMat);

                    //
                    //
                    //
                    //
                    //
                    // alternatively i could do this:
                    //
                    // A = Tinv * Minv * b * xinv
                    //
                    // and then decompose A.

                    // so with noise this produces non-rigid transforms.
                    // instead try the Omeyama way, adapted to 2d.

                    Mat from = Mat.zeros(2, XMat.cols(), CvType.CV_64F);
                    for (int col = 0; col < XMat.cols(); ++col) {
                        from.put(0, col, XMat.get(0, col)[0]);
                        from.put(1, col, XMat.get(1, col)[0]);
                    }
                    from = from.t();
                    debug(0, "from", from);
                    // Mat from = targetPointsMultipliedXZHomogeneousMat.t();

                    Mat to = Mat.zeros(2, bMat.cols(), CvType.CV_64F);
                    for (int col = 0; col < bMat.cols(); ++col) {
                        to.put(0, col, bMat.get(0, col)[0]);
                        to.put(1, col, bMat.get(1, col)[0]);
                    }
                    to = to.t();
                    debug(0, "to", to);

                    // Mat to = TinvMinvBmat.t();

                    final int count = from.checkVector(2);
                    if (count < 3)
                        throw new IllegalArgumentException(
                                "Umeyama algorithm needs at least 3 points for affine transformation estimation.");
                    if (to.checkVector(2) != count)
                        throw new IllegalArgumentException("Point sets need to have the same size");
                    final double one_over_n = 1. / count;

                    // yields a 3x1 vector (row) containing the means of each column
                    final Function<Mat, Mat> colwise_mean = /* [one_over_n] */(final Mat m) -> {
                        Mat my = new Mat();
                        Core.reduce(m, my, 0, Core.REDUCE_SUM, CvType.CV_64F);
                        // return my * one_over_n;
                        return my.mul(Mat.ones(my.size(), my.type()), one_over_n);
                    };

                    final BinaryOperator<Mat> demean = /* [count] */(final Mat A, final Mat mean) -> {
                        Mat A_centered = Mat.zeros(count, 2, CvType.CV_64F);
                        for (int i = 0; i < count; i++) { // i = columns == points
                            Mat foo = new Mat();

                            Core.subtract(A.row(i), mean, foo);

                            for (int j = 0; j < 2; j++) {
                                // A_centered.row(i) = A.row(i) - mean;

                                double d = foo.get(0, j)[0];
                                A_centered.put(i, j, d);
                            }
                        }
                        return A_centered;
                    };

                    Mat from_mean = colwise_mean.apply(from); // 3x1 vector of col means

                    Mat to_mean = colwise_mean.apply(to); // 3x1 vector of col means

                    Mat from_centered = demean.apply(from, from_mean);
                    Mat to_centered = demean.apply(to, to_mean);

                    debug(0, "from_centered", from_centered);
                    debug(0, "to_centered", to_centered);

                    // Mat cov = to_centered.t() * from_centered * one_over_n;
                    Mat cov = new Mat();
                    Core.gemm(to_centered.t(), from_centered, one_over_n, new Mat(), 0.0, cov);
                    debug(0, "cov", cov);

                    Mat u = new Mat();
                    Mat d = new Mat();
                    Mat vt = new Mat();
                    Core.SVDecomp(cov, d, u, vt, Core.SVD_MODIFY_A | Core.SVD_FULL_UV);
                    debug(0, "u", u);
                    debug(0, "d", d);
                    debug(0, "vt", vt);

                    // if (Core.countNonZero(d) < 2)
                    // throw new IllegalArgumentException("Points cannot be colinear");

                    Mat S = Mat.eye(2, 2, CvType.CV_64F);
                    if (Core.determinant(u) * Core.determinant(vt) < 0) {
                        // S.at<double>(2, 2) = -1;
                        S.put(1, 1, -1);
                    }

                    Mat rmat = new Mat();
                    Core.gemm(S, vt, 1.0, new Mat(), 0.0, rmat);
                    Core.gemm(u, rmat, 1.0, new Mat(), 0.0, rmat);
                    double scale = 1.0;

                    // Mat new_to = scale * rmat * from_mean.t();
                    Mat new_to = new Mat(); // 3x1 vector
                    Core.gemm(rmat, from_mean.t(), scale, new Mat(), 0.0, new_to);

                    Mat transform = Mat.zeros(2, 3, CvType.CV_64F);
                    Mat r_part = transform.submat(0, 2, 0, 2);
                    rmat.copyTo(r_part);
                    // transform.col(3) = to_mean.t() - new_to;
                    double euler = VisionUtil.rotm2euler2d(rmat);
                    debug(0, "euler", euler);

                    Mat t_part = transform.col(2);
                    Mat tmat = new Mat();
                    Core.subtract(to_mean.t(), new_to, tmat);
                    tmat.copyTo(t_part);
                    debug(0, "transform", transform);

                    //
                    //
                    //

                    // ah this is the reverse transform so to get the world coords
                    // i need to transform back.

                    // Mat worldRvec = new Mat();
                    // Calib3d.Rodrigues(rmat, worldRvec);
                    Mat cameraTVec = Mat.zeros(2, 1, CvType.CV_64F);
                    cameraTVec.put(0, 0, transform.get(0, 2)[0], transform.get(1, 2)[0]);
                    debug(0, "cameraTVec", cameraTVec);
                    Mat pworldTVec = new Mat();
                    debug(0, "rmat", rmat);
                    Core.gemm(rmat.t(), cameraTVec, -1.0, new Mat(), 0.0, pworldTVec);
                    debug(0, "pWorldTVec", pworldTVec);

                    double pxPos = pworldTVec.get(0, 0)[0];
                    double pyPos = yPos;
                    double pzPos = pworldTVec.get(1, 0)[0];
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

    static MatOfPoint3f duplicatePoints(MatOfPoint3f targetGeometryMeters, int pointMultiplier) {
        MatOfPoint3f targetPointsMultiplied = new MatOfPoint3f();
        List<Point3> targetpointlist = new ArrayList<Point3>();
        for (int reps = 0; reps < pointMultiplier; reps++) {
            for (Point3 p : targetGeometryMeters.toList()) {
                targetpointlist.add(p);
            }
        }
        targetPointsMultiplied = new MatOfPoint3f(targetpointlist.toArray(new Point3[0]));
        return targetPointsMultiplied;
    }

    static Mat homogenize(Mat targetPointsMultiplied) {
        Mat homogeneousTarget = new Mat();
        Calib3d.convertPointsToHomogeneous(targetPointsMultiplied, homogeneousTarget);
        homogeneousTarget = homogeneousTarget.reshape(1).t();
        homogeneousTarget.convertTo(homogeneousTarget, CvType.CV_64F);
        debug(0, "homogeneousTarget", homogeneousTarget);
        return homogeneousTarget;
    }

    static Mat makeUMat(MatOfPoint2f leftPts, MatOfPoint2f rightPts) {
        Mat uMat = Mat.zeros(leftPts.toList().size(), 3, CvType.CV_64F);
        for (int i = 0; i < leftPts.toList().size(); ++i) {
            uMat.put(i, 0, leftPts.get(i, 0)[0], rightPts.get(i, 0)[0], 1.0);
        }
        debug(0, "uMat", uMat);
        return uMat;
    }

    Mat makeXMat() {
        List<Point3> targetPointsMultipliedList = targetPointsMultiplied.toList();

        List<Point3> listOfXZ = new ArrayList<Point3>();
        for (Point3 p3 : targetPointsMultipliedList) {
            listOfXZ.add(new Point3(p3.x, p3.z, 1));
        }
        MatOfPoint3f targetPointsMultipliedXZHomogeneous = new MatOfPoint3f(
                listOfXZ.toArray(new Point3[0]));

        Mat targetPointsMultipliedXZHomogeneousMat = targetPointsMultipliedXZHomogeneous.reshape(1).t();

        targetPointsMultipliedXZHomogeneousMat.convertTo(targetPointsMultipliedXZHomogeneousMat,
                CvType.CV_64F);
        return targetPointsMultipliedXZHomogeneousMat;
    }

    static Mat makeMInv() {
        Mat M = Mat.zeros(3, 3, CvType.CV_64F);
        M.put(0, 0,
                f, 0, cx,
                0, f, cx,
                0, 0, 1);
        debug(0, "M", M);
        Mat Minv = M.inv();
        debug(0, "Minv", Minv);
        return Minv;
    }

    static Mat makeTInv(double b) {
        Mat T = Mat.zeros(3, 3, CvType.CV_64F);
        T.put(0, 0,
                1, 0, b / 2,
                1, 0, -b / 2,
                0, 1, 0);
        debug(0, "T", T);
        Mat Tinv = T.inv();
        debug(0, "Tinv", Tinv);
        return Tinv;
    }

    static Mat makeBMat(MatOfPoint2f leftPts, MatOfPoint2f rightPts, double b) {
        // To solve Ax=b triangulation (Ax=M-1T-1u), first make u: (u,u',1):
        Mat uMat = makeUMat(leftPts, rightPts);

        // and the inverse transforms we're going to apply:
        Mat Minv = makeMInv();
        Mat Tinv = makeTInv(b);

        // apply the inverses to the observations (the "u") in the correct order:
        Mat MinvUmat = new Mat();
        Core.gemm(Minv, uMat.t(), 1.0, new Mat(), 0.0, MinvUmat);
        debug(0, "MinvUmat", MinvUmat);

        Mat bMat = new Mat();
        Core.gemm(Tinv, MinvUmat, 1.0, new Mat(), 0.0, bMat);
        debug(0, "bMat", bMat);

        // Make the result look homogeneous
        normalize(bMat);
        debug(0, "bMat normalized", bMat);
        return bMat;
    }

    /**
     * use OpenCV Core.solve(X, b, A, DECOMP_SVD), return A.
     */
    Mat solve(Mat XMat, Mat bMat) {
        // so now Ax=b where X is the world geometry and b is above.
        Mat AA = new Mat();

        // remember the solver likes transposes
        Core.solve(XMat.t(), bMat.t(), AA, Core.DECOMP_SVD);
        // ...and produces a transpose.
        AA = AA.t();
        debug(0, "AA", AA);

        double Ascale = AA.get(2, 2)[0];
        Core.gemm(AA, Mat.eye(3, 3, CvType.CV_64F), 1 / Ascale, new Mat(), 0.0, AA);
        debug(1, "AA scaled", AA);
        return AA;
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
