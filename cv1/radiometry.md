# Radiometry

The goal is to maximize the contrast of the target compared to the rest of the scene: ideally the target would be white, and the
rest of the scene would be black.  To do that, we'll illuminate the target with a narrow spectrum, and then filter the reflected light
to select just that illumination prior to the camera.
So what wavelength is best?  How bright should the light be?  What will the image look like in the camera?

We should pick a wavelength with these characteristics:

1. low ambient intensity, to maximize contrast
2. high emission efficiency, so that the emitter can be bright without getting hot
3. high filter selectivity, to minimize off-spectrum input
4. high camera sensitivity, to minimize noise
5. no possibility of injury to skin or eyes
6. avoids reduction of resolution caused by diffraction

There is some option value in choosing a wavelength that matches one of the colors in the typical Bayer mosaic; see below for details.

## Ambient intensity

FRC events are held in indoor basketball arenas and venues like the [Houston convention center](https://www.grbhouston.com/).  All these
locations are illuminated using overhead lighting, and some are also lit at an angle from the side, and/or (less commonly)
via windows to indirect sunlight.  It is not unheard of for gyms to allow __direct__ sunlight through a diffuser.

You can see the overhead fixtures below, circled in red, and the side-facing lights hanging from a truss, in green.

<img src="path904.png" width=500/>

The picture shows that the overhead lights are "cool white" whereas the side-facing lights are "warm white" -- either or both might be
used in any specific venue.

The most common lighting technologies for this purpose would be [metal halide](https://en.wikipedia.org/wiki/Metal-halide_lamp)
or [white LEDs](https://en.wikipedia.org/wiki/LED_lamp#White_light_LEDs).  Since LEDs are more efficient, they are increasingly
common.

A metal halide lamp is a type of arc lamp that works by exciting metal ions, which then radiate in specific wavelengths.
The spectrum for metal halide varies by manufacturer and with the age of the bulb, but tends to look something like this:

<img src="https://upload.wikimedia.org/wikipedia/commons/thumb/6/67/Metal_Halide_Rainbow.png/640px-Metal_Halide_Rainbow.png" width=500/>

Note the peaks in yellow and green, and the broad but low background that extends into infrared and near ultraviolet.

A white LED is a blue LED coated with one or more [phosphors](https://en.wikipedia.org/wiki/Phosphor) that absorb much of the blue light and radiate
a yellowish color.  A typical white LED spectrum looks like this:

<img src="https://upload.wikimedia.org/wikipedia/commons/thumb/4/48/White_LED.png/640px-White_LED.png" width=500/>

Note the large, narrow blue peak, the much broader yellow peak, and the near-total absence of infrared.

The spectrum of solar radiation looks like this:

<img src="https://upload.wikimedia.org/wikipedia/commons/thumb/2/27/Spectrum_of_Sunlight_en.svg/640px-Spectrum_of_Sunlight_en.svg.png" width=640/>

The indirect spectrum is primarily blue, like the blue sky, and the direct spectrum contains an enormous amount of infrared: you can feel it
as heat.  Perhaps the gyms with windows also use infrared-blocking film, to reduce the energy used for air conditioning.  Let's assume
that's true.  See below for mitigations in case we're wrong.

To maximize contrast, we should choose a source wavelength that matches the minimum of the spectra above. The reasonable choices might be:

* About 500nm, corresponding to the dip between the blue and yellow peaks in the white LED spectrum -- this isn't the best choice, because the
dip is still half the yellow intensity, not near zero.
* Something short, like 420nm.  the problem with this choice is safety: bright blue and UV sources are hazardous, see below for details.
* Over 650nm.  all illuminators (except the sun) aim to minimize this, and all longer wavelengths.  This is the best choice.

## Emitter efficiency

Using the [Cree XP-E2](https://cree-led.com/media/documents/XLampXPE2.pdf) as a guide, there is a range of emitter efficiencies to choose from:

## Filter selectivity



## Camera sensitivity


## Safety


## Bayer mosaic

Rather than using a monochrome camera, we could use an RGB camera and choose an illuminator that corresponds to one of the colors in
the usual [Bayer mosaic](https://en.wikipedia.org/wiki/Bayer_filter).  Here's the spectral response of for each of the
channels in the Sony IMX287 detector
in the [Flir Blackfly S](https://www.flir.com/products/blackfly-s-usb3/), a popular ($500) machine vision camera.

<p align=center><img src="http://softwareservices.flir.com/BFS-U3-244S8/latest/EMVA/BFS-U3-244S8_2103.0000.628.000_color_emva_psd_624x312.png" width=640/></p>

The overlapping, gently sloping filters mimic the overlapping, gently sloping response of the human eye: for example, the ratio of blue to green
response is how the wavelengths between blue and green are differentiated.  If the RGB channels were sharp bandpass filters, the
camera wouldn't produce anything useful at all: a cyan signal would be read as either completely blue or completely green depending on its
relationship to the blue/green divider.

To extract a high-contrast monochromatic signal from this detector, we could pick any color from
the normal result of [demosaicing](https://en.wikipedia.org/wiki/Demosaicing), at the cost of resolution
and the usual demosaicing artifacts.  Alternatively, we could illuminate with 570nm, where green is sensitive but neither red nor blue is, and
use the raw signal from the green channel.  We could also use the demosaiced signal for other purposes (e.g. object detection),
perhaps excluding the band around 570nm if it proves to be a distraction.

## Mitigating sunlight


