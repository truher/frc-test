# CV1: Vision experiments

The general idea is to use cameras, low-tech OpenCV code, and conventional FRC known-location
retroreflective targets to generate pose estimates appropriate for correcting a Kalman filter
used for navigation.

It would be best if the pose estimates were accurate enough for trajectory planning and
correction appropriate to typical FRC games, which comprise two distinct scenarios:

* Navigating to within a few centimeters for close targets.
  Think of the hatch-panel targets in 2019, they could be used all the way to end-effector location.
* Location-finding to within a meter or so, for targeting projectiles.
  Think of the 2022 "hub" target or the 2013 frisbee target.

The FRC game is a much simpler vision challenge than most folk work on: the camera tilt is usually
fixed, the target geometry is known, the robot has only three degrees of freedom (x, y, azimuth).

I tried several approaches, all using WPI-Lib OpenCV Java API, which is OpenCV version 4.5.2 at the
moment.

# 1. Monocular PnP.

This was disappointing, for geometric reasons: even at unrealistically high resolution, viewing a
target head-on yields only range; there's no way to get precision in the parallel direction, or
in azimuth.  This solution also involves way too many degrees of freedom, and uses them to introduce
errors (e.g. robot below the floor).

TODO: add some example code pointers.

# 2. Binocular triangulation, followed by solve.

Complain here

# 3. Binocular affine transform estimation

The 4.5.4 version of OpenCV includes Omeyama's algorithm for 3d rigid-body transform estimation, which
imposes some constraints on the general problem, e.g. rotations only, unit scaling.  Ultimately it
is simply SVD underneath.  :-)  I ported the OpenCV code to Java and tried it; it does improve on the
general triangulation solution, but it's still not great.  It still involves six degrees of freedom,
but the physical problem only involves three.

# 4. Binocular 2d solver

I tried dropping Y data, conditioning the input, and using the OpenCV SVD.

# 5. Binocular 2d Omeyama method

I also tried a 2d adaptation of the Omeyama method, which yields the same result as the 2d solver, above, 
but requires a giant multiplication.  Does the solve method do that?



TODO: some math.

$$
\begin{bmatrix}
1 & 2 & 3\\
a & b & c
\end{bmatrix}
$$
