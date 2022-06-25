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
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

import vision.VisionUtil;

/**
 * playing with SVD using a two-dimensional representation to avoid the kinds of
 * errors that seem to happen where freedom in Y yields errors in the other
 * axes.
 */

public class TestSVD {
    public static final boolean DEBUG = false;

    public TestSVD() {
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
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

    @Test
    public void testSomething() {
        // global invariants

        Random rand = new Random(42);
        //
        // NOISE
        //
        final double noisePixels = 2;

        final int pointMultiplier = 1;

        final int height = 540;
        // ignore Y data
        // final int cy = height / 2;
        final int width = 960;
        final int cx = width / 2;
        final Size dsize = new Size(width, height);
        Rect viewport = new Rect(10, 10, width - 20, height - 20);
        final double f = 256.0;
        final Mat kMat = VisionUtil.makeIntrinsicMatrix(f, dsize);
        debug("kMat", kMat);
        final MatOfDouble dMat = new MatOfDouble(Mat.zeros(4, 1, CvType.CV_64F));
        debug("dMat", dMat);

        //
        //
        // make target geometries
        //
        //

        final MatOfPoint3f targetGeometryMeters = new MatOfPoint3f(
                new Point3(0.0, 0.0, 0.0),
                new Point3(1.0, 1.0, 0.0),
                new Point3(1.0, -1.0, 0.0),
                new Point3(-1.0, -1.0, 0.0),
                new Point3(-1.0, 1.0, 0.0));
        MatOfPoint3f targetPointsMultiplied = new MatOfPoint3f();
        List<Point3> targetpointlist = new ArrayList<Point3>();
        for (int reps = 0; reps < pointMultiplier; reps++) {
            for (Point3 p : targetGeometryMeters.toList()) {
                targetpointlist.add(p);
            }
        }
        targetPointsMultiplied = new MatOfPoint3f(targetpointlist.toArray(new Point3[0]));

        Mat homogeneousTarget = new Mat();
        Calib3d.convertPointsToHomogeneous(targetPointsMultiplied, homogeneousTarget);
        homogeneousTarget = homogeneousTarget.reshape(1).t();
        homogeneousTarget.convertTo(homogeneousTarget, CvType.CV_64F);
        debug("homogeneousTarget", homogeneousTarget);

        //
        //
        // coordinates
        //
        //

        // 0.8m is the max conceivable
        final double b = 0.8;
        // final double pan = 0.2;
        // final double xPos = 0;
        final double yPos = 0;
        // final double zPos = -3;
        int idx = 1;
        System.out.println("idx, pan, xpos, ypos, zpos, ppan, pxpos, pypos, pzpos");
        for (double pan = -Math.PI / 2; pan <= Math.PI / 2; pan += Math.PI / 8) {
            // field is 16m long, say half the field is relevant
            for (double zPos = -8.0; zPos <= -1.0; zPos += 1.0) {
                // field is 8m wide, so +/- 4m
                point: for (double xPos = -4.0; xPos <= 4.0; xPos += 1.0) {
                    ++idx;

                    //
                    //
                    // camera-to-world transforms
                    //
                    //

                    Mat worldTVec = Mat.zeros(3, 1, CvType.CV_32F);
                    worldTVec.put(0, 0, xPos, yPos, zPos);
                    debug("worldTVec", worldTVec);

                    Mat worldRV = Mat.zeros(3, 1, CvType.CV_32F);
                    worldRV.put(0, 0, 0.0, pan, 0.0);
                    debug("worldRV", worldRV);

                    Mat worldRMat = new Mat();
                    Calib3d.Rodrigues(worldRV, worldRMat);

                    Mat camRMat = worldRMat.t();
                    Mat camRV = new Mat();
                    Calib3d.Rodrigues(camRMat, camRV);
                    debug("camRV", camRV);

                    // now the whole camera->world
                    Mat cameraToWorld = Mat.zeros(4, 4, CvType.CV_32F);
                    cameraToWorld.put(0, 0,
                            worldRMat.get(0, 0)[0], worldRMat.get(0, 1)[0], worldRMat.get(0, 2)[0],
                            worldTVec.get(0, 0)[0],
                            worldRMat.get(1, 0)[0], worldRMat.get(1, 1)[0], worldRMat.get(1, 2)[0],
                            worldTVec.get(1, 0)[0],
                            worldRMat.get(2, 0)[0], worldRMat.get(2, 1)[0], worldRMat.get(2, 2)[0],
                            worldTVec.get(2, 0)[0],
                            0, 0, 0, 1);

                    debug("cameraToWorld", cameraToWorld);

                    // this is inverse(worldT*worldR)
                    // inverse of multiplication is order-reversed multipication of inverses, so
                    // which is worldR.t * -worldT or camR*-worldT
                    Mat camTVec = new Mat();
                    Core.gemm(camRMat, worldTVec, -1.0, new Mat(), 0, camTVec);
                    debug("camTVec", camTVec);

                    // so the final (homogeneous) transform from world to camera
                    Mat worldToCamera = Mat.zeros(4, 4, CvType.CV_32F);
                    worldToCamera.put(0, 0,
                            camRMat.get(0, 0)[0], camRMat.get(0, 1)[0], camRMat.get(0, 2)[0], camTVec.get(0, 0)[0],
                            camRMat.get(1, 0)[0], camRMat.get(1, 1)[0], camRMat.get(1, 2)[0], camTVec.get(1, 0)[0],
                            camRMat.get(2, 0)[0], camRMat.get(2, 1)[0], camRMat.get(2, 2)[0], camTVec.get(2, 0)[0],
                            0, 0, 0, 1);
                    debug("worldToCamera", worldToCamera);

                    // transform from camera center to left eye
                    Mat baseToLeftEye = Mat.zeros(4, 4, CvType.CV_32F);
                    baseToLeftEye.put(0, 0,
                            1, 0, 0, b / 2,
                            0, 1, 0, 0,
                            0, 0, 1, 0,
                            0, 0, 0, 1);
                    debug("baseToLeftEye", baseToLeftEye);

                    Mat worldToLeftEye = new Mat();
                    Core.gemm(baseToLeftEye, worldToCamera, 1.0, new Mat(), 0.0, worldToLeftEye);
                    debug("worldToLeftEye", worldToLeftEye);

                    Mat baseToRightEye = Mat.zeros(4, 4, CvType.CV_32F);
                    baseToRightEye.put(0, 0,
                            1, 0, 0, -b / 2,
                            0, 1, 0, 0,
                            0, 0, 1, 0,
                            0, 0, 0, 1);
                    debug("baseToRightEye", baseToRightEye);

                    Mat worldToRightEye = new Mat();
                    Core.gemm(baseToRightEye, worldToCamera, 1.0, new Mat(), 0.0, worldToRightEye);
                    debug("worldToRightEye", worldToRightEye);

                    //
                    //
                    // make images
                    //
                    //

                    Mat leftCamRV = Mat.zeros(3, 1, CvType.CV_32F);
                    Calib3d.Rodrigues(worldToLeftEye.rowRange(0, 3).colRange(0, 3), leftCamRV);
                    debug("leftCamRV", leftCamRV);
                    Mat leftCamTVec = worldToLeftEye.colRange(3, 4).rowRange(0, 3);
                    debug("leftCamTVec", leftCamTVec);

                    MatOfPoint2f leftPts = new MatOfPoint2f();
                    Mat jacobian = new Mat();
                    // this wants world->camera transformation
                    Calib3d.projectPoints(targetGeometryMeters, leftCamRV, leftCamTVec, kMat, dMat,
                            leftPts, jacobian);
                    debug("leftPts", leftPts);

                    // perturb points
                    List<Point> leftPtsList = new ArrayList<Point>();
                    for (int reps = 0; reps < pointMultiplier; reps++) {
                        for (Point p : leftPts.toList()) {
                            p.x = p.x + rand.nextGaussian() * noisePixels;
                            p.y = p.y + rand.nextGaussian() * noisePixels;
                            leftPtsList.add(p);
                        }
                    }
                    leftPts = new MatOfPoint2f(leftPtsList.toArray(new Point[0]));

                    Scalar green = new Scalar(0, 255, 0);
                    Mat imgLeft = Mat.zeros(height, width, CvType.CV_32FC3);
                    for (Point pt : leftPts.toList()) {
                        if (!viewport.contains(pt))
                            continue point;
                        Imgproc.circle(imgLeft, pt, 6, green, 1);
                    }
                    Imgcodecs.imwrite(String.format("C:\\Users\\joelt\\Desktop\\pics\\svd-%d-left.png",
                            idx), imgLeft);

                    Mat rightCamRV = Mat.zeros(3, 1, CvType.CV_32F);
                    Calib3d.Rodrigues(worldToRightEye.rowRange(0, 3).colRange(0, 3), rightCamRV);
                    debug("rightCamRV", rightCamRV);
                    Mat rightCamTVec = worldToRightEye.colRange(3, 4).rowRange(0, 3);
                    debug("rightCamTVec", rightCamTVec);

                    MatOfPoint2f rightPts = new MatOfPoint2f();

                    // this wants world->camera transformation
                    Calib3d.projectPoints(targetGeometryMeters, rightCamRV, rightCamTVec, kMat, dMat,
                            rightPts, jacobian);
                    debug("rightPts", rightPts);

                    // perturb points
                    List<Point> rightPtsList = new ArrayList<Point>();
                    for (int reps = 0; reps < pointMultiplier; reps++) {
                        for (Point p : rightPts.toList()) {
                            p.x = p.x + rand.nextGaussian() * noisePixels;
                            p.y = p.y + rand.nextGaussian() * noisePixels;
                            rightPtsList.add(p);
                        }
                    }
                    rightPts = new MatOfPoint2f(rightPtsList.toArray(new Point[0]));

                    Mat imgRight = Mat.zeros(height, width, CvType.CV_32FC3);
                    for (Point pt : rightPts.toList()) {
                        if (!viewport.contains(pt))
                            continue point;
                        Imgproc.circle(imgRight, pt, 6, green, 1);
                    }
                    Imgcodecs.imwrite(String.format("C:\\Users\\joelt\\Desktop\\pics\\svd-%d-right.png",
                            idx), imgRight);

                    //
                    //
                    // triangulate and solve
                    //
                    //
                    // just ignore Y and v, use X, u(left) and u'(right).
                    //
                    // so s(u,u',1) = [f 0 cx; 0 f cx; 0 0 1] * [1 0 b/2; 1 0 -b/2; 0 1 0] * [R|t] *
                    // (X Z 1)
                    // i.e. intrinsic matrix + eye transform * change of basis * points.

                    // so call this something like MTAx=b
                    // where M and T are invertible, so:
                    // Ax = Tinv * Minv * b
                    //
                    // i could then solve that with Core.solve()
                    //
                    // alternatively i could do this:
                    //
                    // A = Tinv * Minv * b * xinv
                    //
                    // and then decompose A.
                    //
                    // start with the solver i guess?
                    //
                    // for this formulation the data are not two separate sets of (u,v)left and
                    // (u,v)right, it's (uleft, uright, 1).
                    //
                    Mat bMat = Mat.zeros(leftPtsList.size(), 3, CvType.CV_64F);
                    for (int i = 0; i < leftPtsList.size(); ++i) {
                        bMat.put(i, 0, leftPts.get(i, 0)[0], rightPts.get(i, 0)[0], 1.0);
                    }
                    debug("bMat", bMat);

                    // and the target drops Y
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

                    Mat M = Mat.zeros(3, 3, CvType.CV_64F);
                    M.put(0, 0,
                            f, 0, cx,
                            0, f, cx,
                            0, 0, 1);

                    debug("M", M);
                    Mat Minv = M.inv();
                    debug("Minv", Minv);

                    Mat T = Mat.zeros(3, 3, CvType.CV_64F);
                    T.put(0, 0,
                            1, 0, b / 2,
                            1, 0, -b / 2,
                            0, 1, 0);
                    debug("T", T);
                    Mat Tinv = T.inv();
                    debug("Tinv", Tinv);

                    // apply the inverses to the observations (the "b")
                    Mat MinvBmat = new Mat();
                    Core.gemm(Minv, bMat.t(), 1.0, new Mat(), 0.0, MinvBmat);
                    debug("MinvBmat", MinvBmat);

                    Mat TinvMinvBmat = new Mat();
                    Core.gemm(Tinv, MinvBmat, 1.0, new Mat(), 0.0, TinvMinvBmat);
                    debug("TinvMinvBmat", TinvMinvBmat);

                    // let's normalize that since it's supposed to be homogeneous?
                    // it represents the result of the rotation/translation we're trying to find
                    for (int col = 0; col < TinvMinvBmat.cols(); ++col) {
                        double xval = TinvMinvBmat.get(0, col)[0];
                        double zval = TinvMinvBmat.get(1, col)[0];
                        double scaleVal = TinvMinvBmat.get(2, col)[0];
                        TinvMinvBmat.put(0, col, xval / scaleVal);
                        TinvMinvBmat.put(1, col, zval / scaleVal);
                        TinvMinvBmat.put(2, col, 1.0);
                    }
                    debug("TinvMinvBmat (scaled)", TinvMinvBmat);

                    // so now Ax=b where X is the world geometry and b is above.
                    Mat AA = new Mat();

                    // System.out.println("TinvMinvBmat (b data)");
                    // System.out.println(TinvMinvBmat.type());
                    // System.out.println(TinvMinvBmat.size());
                    // System.out.println(TinvMinvBmat.dump());
                    // System.out.println("targetPointsMultipliedXZHomogeneousMat (x data)");
                    // System.out.println(targetPointsMultipliedXZHomogeneousMat.type());
                    // System.out.println(targetPointsMultipliedXZHomogeneousMat.size());
                    // System.out.println(targetPointsMultipliedXZHomogeneousMat.dump());

                    // remember the solver likes transposes which i find very strange
                    // but it is what it is
                    Core.solve(targetPointsMultipliedXZHomogeneousMat.t(), TinvMinvBmat.t(), AA, Core.DECOMP_SVD);
                    // solver produces transpose.
                    AA = AA.t();
                    debug("AA", AA);

                    // remember where we are:
                    // b = 0.4;
                    // pan = 0;
                    // xPos = 0;
                    // yPos = 0;
                    // zPos = -5;

                    // so we expect A to look like this:
                    // [1 0 0
                    // 0 1 -5
                    // 0 0 1]

                    double Ascale = AA.get(2, 2)[0];
                    Core.gemm(AA, Mat.eye(3, 3, CvType.CV_64F), 1 / Ascale, new Mat(), 0.0, AA);
                    debug("AA scaled", AA);

                    double perhapsRotation = VisionUtil.rotm2euler2d(AA.submat(0, 2, 0, 2));
                    debug("perhapsRotation", perhapsRotation);

                    // so with noise this produces non-rigid transforms.
                    // instead try the Omeyama way, adapted to 2d.

                    Mat from = Mat.zeros(2, targetPointsMultipliedXZHomogeneousMat.cols(), CvType.CV_64F);
                    for (int col = 0; col < targetPointsMultipliedXZHomogeneousMat.cols(); ++col) {
                        from.put(0, col, targetPointsMultipliedXZHomogeneousMat.get(0, col)[0]);
                        from.put(1, col, targetPointsMultipliedXZHomogeneousMat.get(1, col)[0]);
                    }
                    from = from.t();
                    debug("from", from);
                    // Mat from = targetPointsMultipliedXZHomogeneousMat.t();

                    Mat to = Mat.zeros(2, TinvMinvBmat.cols(), CvType.CV_64F);
                    for (int col = 0; col < TinvMinvBmat.cols(); ++col) {
                        to.put(0, col, TinvMinvBmat.get(0, col)[0]);
                        to.put(1, col, TinvMinvBmat.get(1, col)[0]);
                    }
                    to = to.t();
                    debug("to", to);

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

                    debug("from_centered", from_centered);
                    debug("to_centered", to_centered);

                    // Mat cov = to_centered.t() * from_centered * one_over_n;
                    Mat cov = new Mat();
                    Core.gemm(to_centered.t(), from_centered, one_over_n, new Mat(), 0.0, cov);
                    debug("cov", cov);

                    Mat u = new Mat();
                    Mat d = new Mat();
                    Mat vt = new Mat();
                    Core.SVDecomp(cov, d, u, vt, Core.SVD_MODIFY_A | Core.SVD_FULL_UV);
                    debug("u", u);
                    debug("d", d);
                    debug("vt", vt);

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
                    debug("euler", euler);

                    Mat t_part = transform.col(2);
                    Mat tmat = new Mat();
                    Core.subtract(to_mean.t(), new_to, tmat);
                    tmat.copyTo(t_part);
                    debug("transform", transform);

                    // ah this is the reverse transform so to get the world coords
                    // i need to transform back.

                    // Mat worldRvec = new Mat();
                    // Calib3d.Rodrigues(rmat, worldRvec);
                    Mat cameraTVec = Mat.zeros(2, 1, CvType.CV_64F);
                    cameraTVec.put(0, 0, transform.get(0, 2)[0], transform.get(1, 2)[0]);
                    debug("cameraTVec", cameraTVec);
                    Mat pworldTVec = new Mat();
                    debug("rmat", rmat);
                    Core.gemm(rmat.t(), cameraTVec, -1.0, new Mat(), 0.0, pworldTVec);
                    debug("pWorldTVec", pworldTVec);

                    double pxPos = pworldTVec.get(0, 0)[0];
                    double pyPos = yPos;
                    double pzPos = pworldTVec.get(1, 0)[0];
                    double ppan = euler;

                    System.out.printf("%d, %5.2f, %5.2f, %5.2f, %5.2f, %5.2f, %5.2f, %5.2f, %5.2f\n",
                            idx, pan, xPos, yPos, zPos, ppan, pxPos, pyPos, pzPos);

                }
            }
        }
    }
}
