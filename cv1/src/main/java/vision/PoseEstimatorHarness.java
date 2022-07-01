package vision;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Random;

import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfDouble;
import org.opencv.core.MatOfPoint2f;
import org.opencv.core.MatOfPoint3f;
import org.opencv.core.Rect;
import org.opencv.core.Size;
import org.opencv.imgcodecs.Imgcodecs;

/**
 * Evaluate a bunch of pose estimators, do parameter studies, etc.
 */
public class PoseEstimatorHarness {
    static final boolean DEBUG = false;
    static final int LEVEL = 1;
    public final List<PoseEstimator> poseEstimators;
    final boolean showGrid = false;

    public PoseEstimatorHarness() {
        // these are ranked worst to best

        poseEstimators = new ArrayList<PoseEstimator>();

        // does nothing
        // poseEstimators.add(new ConstantPoseEstimator());

        // pretty good close up, not good in x far away
        // poseEstimators.add(new MonocularPoseEstimator(false));

        // works ok
        poseEstimators.add(new BinocularConstrainedPoseEstimator(false));

        // ok close up
        poseEstimators.add(new Binocular2dSVDPoseEstimator(false));

        // works pretty well within a few meters.
        poseEstimators.add(new Binocular2dUmeyamaPoseEstimator(false));

        // good, sensitive in z, unsurprisingly
        poseEstimators.add(new MonocularPoseEstimator(true));

        // pretty awesome
        poseEstimators.add(new Binocular2dSVDPoseEstimator(true));

        // awesome
        poseEstimators.add(new Binocular2dUmeyamaPoseEstimator(true));

        // awesome
        poseEstimators.add(new BinocularConstrainedPoseEstimator(true));
    }

    public void run() {
        System.out.println("stderr for each estimator...");
        System.out.printf("%40s %10s %10s %10s %10s %10s %10s\n",
                "name", "heading", "X", "Z", "bearing", "range", "rate");

        for (PoseEstimator e : poseEstimators) {
            Random rand = new Random(42);
            final String name = e.getName();
            // final String description = e.getDescription();
            final Mat[] kMat = e.getIntrinsicMatrices();
            final MatOfDouble[] dMat = e.getDistortionMatrices();
            final double[] b = e.getXOffsets();
            final Size[] sizes = e.getSizes();
            {
                Objects.requireNonNull(kMat);
                if (kMat.length < 1)
                    throw new IllegalArgumentException();

                Objects.requireNonNull(dMat);
                if (dMat.length < 1)
                    throw new IllegalArgumentException();
                if (dMat.length != kMat.length)
                    throw new IllegalArgumentException();

                Objects.requireNonNull(b);
                if (b.length < 1)
                    throw new IllegalArgumentException();
                if (b.length != dMat.length)
                    throw new IllegalArgumentException();

            }

            MatOfPoint3f targetGeometryMeters = VisionUtil.makeTargetGeometry3f(0.5, 0.5);
            // 50fps video => 10hz output = 5x averaging
            int pointMultiplier = 1;
            double noisePixels = 2;
            // pigeon/navx claim 1.5 degrees for fused output
            // final double gyroNoise = 0.025;
            // LIS3MDL claims about 1% thermal noise at 80hz
            // average 8 samples = 1/sqrt(8)
            double gyroNoise = 0.0035; //
            // MatOfPoint3f targetPointsMultiplied =
            // VisionUtil.duplicatePoints(targetGeometryMeters, pointMultiplier);

            int idx = 0;
            double yPos = 0;
            // double tilt = 0;

            double panErrSquareSum = 0.0;
            double xErrSquareSum = 0.0;
            double zErrSquareSum = 0.0;
            double relativeBearingErrSquareSum = 0.0;
            double rangeErrSquareSum = 0.0;
            long workTime = 0;

            if (showGrid)
                System.out.println(
                        "idx, pan, xpos, ypos, zpos, rbear, range, ppan, pxpos, pypos, pzpos, prbear, prange, panErr, xErr, zErr, relativeBearingErr, rangeErr");
            for (double pan = -3 * Math.PI / 8; pan <= 3 * Math.PI / 8; pan += Math.PI / 8) {
                for (double zPos = -10.0; zPos <= -1.0; zPos += 1.0) {
                    pose: for (double xPos = -5; xPos <= 5; xPos += 1.0) {
                        // for (double pan = 0; pan <= 0; pan += Math.PI / 8) {
                        // for (double zPos = -5.0; zPos <= -5.0; zPos += 1.0) {
                        // pose: for (double xPos = 0; xPos <= 0; xPos += 1.0) {
                        double navBearing = Math.atan2(xPos, -zPos);
                        double relativeBearing = navBearing + pan;
                        double range = Math.sqrt(xPos * xPos + zPos * zPos);

                        // don't bother with oblique angles, the projection is wrong for these cases.
                        if (Math.abs(relativeBearing) > Math.PI / 2)
                            continue;

                        // these are the calculated points
                        // MatOfPoint2f[] idealImagePoints = new MatOfPoint2f[kMat.length];
                        Mat[] images = new Mat[kMat.length];
                        for (int cameraIdx = 0; cameraIdx < kMat.length; ++cameraIdx) {
                            // make transform from world origin to camera center
                            Mat worldToCameraCenterHomogeneous = VisionUtil.makeWorldToCameraHomogeneous(pan, xPos,
                                    yPos,
                                    zPos);
                            Mat worldToEye = VisionUtil.translateX(worldToCameraCenterHomogeneous, b[cameraIdx]);

                            // make the points the camera sees
                            MatOfPoint2f pts = VisionUtil.imagePoints(kMat[cameraIdx], dMat[cameraIdx],
                                    targetGeometryMeters,
                                    worldToEye,
                                    pointMultiplier,
                                    noisePixels, rand);
                            Size size = sizes[cameraIdx];
                            final Rect viewport = new Rect(0, 0, (int) size.width, (int) size.height);
                            if (!VisionUtil.inViewport(pts, viewport)) {
                                continue pose;
                            }
                            VisionUtil.writePng(pts, (int) size.width, (int) size.height,
                                    String.format("C:\\Users\\joelt\\Desktop\\pics\\img-%s-%d-%d.png", name, idx,
                                            cameraIdx));
                            // idealImagePoints[cameraIdx] = pts;

                            // also make an image
                            // Mat cameraView = VisionUtil.makeImage(xPos, yPos, zPos, tilt, pan,
                            // kMat[cameraIdx],
                            // dMat[cameraIdx], targetGeometryMeters, size);

                            Mat cameraView = VisionUtil.renderImage(size, targetGeometryMeters, pts);

                            if (cameraView == null) {
                                continue pose;
                            }
                            Imgcodecs.imwrite(
                                    String.format("C:\\Users\\joelt\\Desktop\\pics\\target-%s-%d-%d-distorted.png",
                                            name, idx, cameraIdx),
                                    cameraView);
                            images[cameraIdx] = cameraView;

                        }

                        // if the target isn't in the viewport, skip

                        ++idx;

                        double gyro = pan + (gyroNoise * rand.nextGaussian());

                        long startTime = System.currentTimeMillis();
                        // Mat transform = e.getPose(gyro, targetPointsMultiplied, idealImagePoints);
                        Mat transform = e.getPose(idx, gyro, targetGeometryMeters, images);

                        workTime += (System.currentTimeMillis() - startTime);
                        if (transform == null) {
                            continue pose;
                        }
                        Mat rmat = transform.submat(0, 3, 0, 3);

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

                        if (showGrid)
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

            if (showGrid)
                System.out.println("===========================");
            System.out.printf("%40s %10.4f %10.4f %10.4f %10.4f %10.4f %10.4f\n",
                    name, panRMSE, xRMSE, zRMSE, relativeBearingRMSE, rangeRMSE, 1000.0 * idx / workTime);
            if (showGrid)
                System.out.println("===========================");
        }

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
