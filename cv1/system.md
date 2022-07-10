# System Design for Simultaneous Localization and Mapping ("SLAM")

The overall goal is to produce estimates for pose and target location accurate enough for semi-automated driving.
Instead of the usual "remote-controlled car" approach to FRC movement, the approach can be based on higher-level
commands, for example "go to target" or "return to base".

There are several virtues, vices, and constraints, that affect the design:

* __Maximize learning.__  FRC is a teaching tool.  The reason to build a thing is to teach about design, not to supply a
"mentor-made" solution in place of a COTS one.
* __Minimize complexity.__  FRC is for high school students; the whole thing should be easy to grasp without advanced knowledge.
* __Minimize cost.__  FRC rules stipulate a $600 limit for any single COTS element, but the system should simply be as simple and cheap as possible,
so we can have several of them to play with.
* __Solve a simple problem.__  Rather than solving an modern vision problem, make use of the available retroreflective targets, which have known
locations.  Using this system doesn't preclude __also__ using other vision systems, e.g. "chase the red ball" kinds of things.
* __Maximize the use of COTS parts.__  FRC rules stipulate that unmodified COTS parts may be freely reused, so a complete system can be assembled and refined
in the off-season, and then trivially re-assembled during the build season.  Avoid custom printed circuits, etc.
* __Maximize accuracy.__ To depend on vision-derived position __all the time,__ it needs to be accurate to within a few centimeters, over the entire
8x16-meter field.
* __Maximize flexibility.__ Neither the game dynamics, nor the field layout, are known in advance, so the objective is to get familiar
with the technology, accumulate some prototypes, and be able to deploy them quickly in January.

The system comprises four parts:

1. __Illumination.__  Instead of the usual "green LED" solution, we'll use an [infrared strobe illuminator](illuminator.md).
To maximize the contrast of the vision targets, and to minimize motion blur, use a bright flash in a wavelength
outside the range of background lighting.  Because the background covers the visible spectrum, I chose a near-IR wavelength of 730nm,
which is also one of the most efficient LED emitters available.
2. __Cameras.__  To start with, we'll use [stereoscopic binocular cameras](camera.md).  A binocular approach provides about 4x
the accuracy of estimated target distance, compared with a monocular approach.  To avoid artifacts, choose a global-shutter camera.
If the game includes multiple targets, a multi-camera non-stereoscopic approach might work as well.
4. __Code.__  There are two phases of computation:
    1. Image analysis uses [OpenCV processing on Raspberry Pi](code.md), because it's simple.  It avoids special hardware and magic
    neural nets nobody understands, just use the OpenCV stuff that WPILib comes with, and use the most straightforward
    "network tables" style interface between the RIO and the Pi.  For comparison, the
    Limelight [appears to use](https://www.chiefdelphi.com/t/ever-wondered-what-makes-a-limelight-2-tick/380418) a Raspberry Pi 
    compute module, coupled with a microcontroller (on its own printed circuit board) and a separate board for the illuminator.
    2. The RoboRIO performs a couple of further processing steps:
        1. __IMU Fusion.__  Because both the target size and the binocular interpupillary distance are known, the distance to the target
        is relatively easy to measure, and the __relative__ bearing (angle from camera to target) can be measured very precisely (easily to a tenth
        of a degree) but much farder to measure the absolute bearing (from target to camera).  In other words, if you're directly
        in front of the target, it will look almost exactly the same if you're a five or ten degrees to the left or to the right.
        To resolve this issue, use the IMU bearing instead of the vision-derived bearing.
        2. __Kalman Filter Pose Estimator.__  The RIO maintains a Kalman Filter (probably the EKF or UKF in WPILib) representing
    the robot pose, and corrects the filter periodically with vision- and IMU-derived data, as well as other sources, e.g. wheel odometry.
    
