# Diffuser

Each LED emitter is very bright and very small: it has a high "radiance".  This can be a hazard if the illuminator is held very close to the eye.

To eliminate this hazard, we'll use a diffuser, which spreads light into a cone:

<p align=center>
  <img src="https://docs.google.com/drawings/d/e/2PACX-1vRzMv7DFHx8q7GT_LPcLccZZjwsd5Ez-po3DOq37rKZu5QbQm-8mGMsIHn2A6SYdF9ee5vnxvJZgzP2/pub?w=500" width=500>
</p>

The important parameters of diffusers are

1. efficiency: how much of the incident light comes out?  Some diffusers are very lossy, absorbing more than half the incident light.
Because we want maximum intensity, we want maximum efficiency.
2. scattering angle: some diffusers spread the incident light into a wide beam, some into a narrower beam.  The wider the beam,
the better the reduction in radiance.  There is a limit to the useful scattering angle: we'd like to keep the bulk of the output within
the camera FOV.

There are several kinds of diffusers available:

1. sandblasted glass: cheap ($15), low scattering angle (eight degrees)
3. [Spectralon film](https://sphereoptics.de/en/product/zenith-polymer-diffusers/): lambertian (!), low transmission (40%)
4. [Lumen XT film](https://www.modernplastics.com/wp-content/uploads/2015/05/PDS112_Lumen.pdf): 86&deg; scattering, 82% transmission (!) in LW7 grade.  LW3 grade is 38&deg;, 93% transmission.  available in 4x8 foot sheets for $250, but we only need about $1 worth.  ([More detail](https://www.curbellplastics.com/Research-Solutions/Technical-Resources/Technical-Resources/Plastic-Diffuser-Solutions-for-LED-Lighting))
5. [Edmund TAC film](https://www.edmundoptics.com/p/200-x-200mm-Light-Diffusing-Film/47383), about 20&deg; scattering, 36% transmission (low).
2. advanced holographic films: very expensive ($150), high scattering angle (up to 80&deg;)

We'll start with the former.  How should we design the diffuser geometry?

