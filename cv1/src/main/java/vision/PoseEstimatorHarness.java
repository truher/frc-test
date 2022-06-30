package vision;

import java.util.ArrayList;
import java.util.List;

import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;

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
    }

    public void run() {
        for (PoseEstimator e : poseEstimators) {
            Mat[] kMat = e.getIntrinsicMatrices();
            Mat[] dMat = e.getDistortionMatrices();

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
                    for (double xPos = -5; xPos <= 5; xPos += 1.0) {

                        double navBearing = Math.atan2(xPos, -zPos);
                        double relativeBearing = navBearing + pan;
                        double range = Math.sqrt(xPos * xPos + zPos * zPos);

                        Mat transform = e.getPose(0, new Mat[0]);

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
            System.out.printf("panRMSE %f\n", panRMSE);
            System.out.printf("xRMSE %f\n", xRMSE);
            System.out.printf("zRMSE %f\n", zRMSE);
            System.out.printf("relativeBearingRMSE %f\n", relativeBearingRMSE);
            System.out.printf("rangeRMSE %f\n", rangeRMSE);

            long endTime = System.currentTimeMillis();
            System.out.println(endTime - startTime);
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
