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

/**
 * Evaluate a bunch of pose estimators, do parameter studies, etc.
 */
public class PoseEstimatorHarness {
    static final boolean DEBUG = false;
    static final int LEVEL = 1;
    public final List<PoseEstimator> poseEstimators;

    public PoseEstimatorHarness() {
        poseEstimators = new ArrayList<PoseEstimator>();
        poseEstimators.add(new BadPoseEstimator());
        poseEstimators.add(new BinocularConstrainedPoseEstimator());
    }

    public void run() {
        for (PoseEstimator e : poseEstimators) {
            Random rand = new Random(42);
            final String name = e.getName();
            final String description = e.getDescription();
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

            MatOfPoint3f targetGeometryMeters = VisionUtil.makeTarget(-0.25, 0, 0.25, -0.5);
            int pointMultiplier = 1;
            double noisePixels = 1;
            MatOfPoint3f targetPointsMultiplied = VisionUtil.duplicatePoints(targetGeometryMeters, pointMultiplier);

            long startTime = System.currentTimeMillis();
            int idx = 0;
            double yPos = 0;

            double panErrSquareSum = 0.0;
            double xErrSquareSum = 0.0;
            double zErrSquareSum = 0.0;
            double relativeBearingErrSquareSum = 0.0;
            double rangeErrSquareSum = 0.0;

            System.out.println(
                    "idx, pan, xpos, ypos, zpos, rbear, range, ppan, pxpos, pypos, pzpos, prbear, prange, panErr, xErr, zErr, relativeBearingErr, rangeErr");
            for (double pan = -3 * Math.PI / 8; pan <= 3 * Math.PI / 8; pan += Math.PI / 8) {
                for (double zPos = -10.0; zPos <= -1.0; zPos += 1.0) {
                    pose: for (double xPos = -5; xPos <= 5; xPos += 1.0) {
                        double navBearing = Math.atan2(xPos, -zPos);
                        double relativeBearing = navBearing + pan;
                        double range = Math.sqrt(xPos * xPos + zPos * zPos);

                        // don't bother with oblique angles, the projection is wrong for these cases.
                        if (Math.abs(relativeBearing) > Math.PI / 2)
                            continue;

                        MatOfPoint2f[] imagePoints = new MatOfPoint2f[kMat.length];
                        for (int cameraIdx = 0; cameraIdx < kMat.length; ++cameraIdx) {
                            // make transform from world origin to camera center
                            Mat worldToCameraHomogeneous = VisionUtil.makeWorldToCameraHomogeneous(pan, xPos, yPos,
                                    zPos);
                            Mat worldToLeftEye = VisionUtil.translateX(worldToCameraHomogeneous, b[cameraIdx]);
                            MatOfPoint2f leftPts = VisionUtil.imagePoints(kMat[cameraIdx], dMat[cameraIdx],
                                    targetGeometryMeters,
                                    worldToLeftEye,
                                    pointMultiplier,
                                    noisePixels, rand);
                            Size size = sizes[cameraIdx];
                            final Rect viewport = new Rect(0, 0, (int) size.width, (int) size.height);
                            if (!VisionUtil.inViewport(leftPts, viewport))
                                continue pose;
                            VisionUtil.writePng(leftPts, (int) size.width, (int) size.height,
                                    String.format("C:\\Users\\joelt\\Desktop\\pics\\img-%s-%d-%d.png", name, idx,
                                            cameraIdx));
                            imagePoints[cameraIdx] = leftPts;

                        }

                        // if the target isn't in the viewport, skip

                        ++idx;

                        Mat transform = e.getPose(pan, targetPointsMultiplied, imagePoints);

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
            System.out.println("===========================");
            System.out.println(name);
            System.out.println(description);
            System.out.printf("panRMSE %f\n", panRMSE);
            System.out.printf("xRMSE %f\n", xRMSE);
            System.out.printf("zRMSE %f\n", zRMSE);
            System.out.printf("relativeBearingRMSE %f\n", relativeBearingRMSE);
            System.out.printf("rangeRMSE %f\n", rangeRMSE);

            long endTime = System.currentTimeMillis();
            long runTimeMs = endTime - startTime;
            System.out.printf("runtime ms %d\n", runTimeMs);
            double runTimePerRowMs = ((double) runTimeMs) / idx;
            System.out.printf("runtime per row %f\n", runTimePerRowMs);
            System.out.printf("rate hz %f\n", 1000 / runTimePerRowMs);
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
