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

There are several kinds of diffusers available, ranging from frosted glass (cheap, moderate scattering angle)
to advanced holographic films (expensive, high scattering angle).

We'll start with the former.  How should we design the diffuser geometry?

