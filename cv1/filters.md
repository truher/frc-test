# Filters

Our goal is to illuminate the target with a narrow spectrum, and to filter out everything else from the camera.  The monochromatic LED illuminator
spectrum is pretty narrow, about 40nm, see the [emitters](emitters.md) doc for details.  Here's a typical emitter spectrum:

<p align=center><img src="https://www.apogeeinstruments.com/content/quantum-sensors/par-explained/green-LED-524nm.jpg" width=640/></p>

Ideally we'd like a filter that matches that narrow band, called a "bandpass" filter, like this.  The scales are a little different,
but you can still see how the LED peak from 500 to 550 nm just fits nicely inside the passband spanning the same range.

<p align=center><img src="https://www.laser2000.de/52866-thickbox_default/525-50-bandpassfilter.jpg" width=640/></p>

For blue, we could use a "shortpass" filter, since there's not much background below blue anyway:

<p align=center><img src="https://www.edmundoptics.com/contentassets/7cd89292769c4a63ab9d4fa9be9ed5a5/fig-1a-cbf.gif" width=640/></p>

Similarly, for red, we could use a "longpass" filter:

<p align=center><img src="https://www.andovercorp.com/media/cms_page_media/214/Screen%20Shot%202015-06-10%20at%203.20.32%20PM.png" width=640/></p>

So what specific filters are available?

# Available options

There are many possibilities for filtering blue, green, or red illumination, and they vary enormously in cost.  There are two main types:

1. The least expensive color filter is a [theater gel](https://en.wikipedia.org/wiki/Color_gel).  These work using dyes to __absorb__
the unwanted spectrum; they're intended to filter broad-spectrum illuminators into narrower bands, but they also work just as well
on the detector end.  Most gels are not particularly precise in spectrum terms, and many of them don't transmit very well in the passband.
2. The most precise filter is a "dichroic" (Greek meaning "two color") filter. These work using layers of thin films to __reflect__
the unwanted spectrum (thus the name, they split one "color" from the other).  There are two sources for dichroic filters, industrial
optics manufacturers (designed for scientific purposes like fluorescence spectroscopy), and, again, theater lighting (dichroics are superior to dye
gels because they don't bleach and they don't absorb heat).

# Gels

There are __many__ gels to choose from, you can poke through them
[here](https://us.rosco.com/en/mycolor).  Many of them have low passband transparency, very gradual cutoffs, and pass multiple bands (e.g. it is
common for filters that appear blue to actually also pass red).  These are some examples of pretty good gels I found:

[Roscolux 27, "Medium Red"](https://www.bhphotovideo.com/c/product/43960-REG/Rosco_RS2711_27_Filter_Medium.html) $8 for almost 4 square feet.  This
gel has pretty good passband transparency, >85%, and a cut-off wavelength of about 650nm.

<p align=center><img src="https://cdn11.bigcommerce.com/s-nnyoihm3uz/images/stencil/1280x1280/products/8017/9367/R27__10331__01255.1522033566.jpg" width=500/></p>

[Roscolux 389, "Chroma Green"](https://www.bhphotovideo.com/c/product/44326-REG/Rosco_RS38911_Lighting_Filter_389_Chroma.html) $8 for almost 4 square feet.  This filter has OK transmission in the passband, about 80% peak, with FWHM of about 480-560 nm,
and it's not entirely blocking in the yellow-red region.

<p align=center><img src="https://cdn11.bigcommerce.com/s-nnyoihm3uz/images/stencil/1280x1280/products/8114/9477/R389__23639__94742.1522033597.jpg" width=500/></p>

[Roscolux 74 "Night Blue"](https://www.bhphotovideo.com/c/product/44490-REG/Rosco_RS7411_74_Filter_Night.html) $8 for almost 4 square feet.  Notice
the low transparency, about 50%, in the passband, centered around 470 nm, FWHM of about 440-500 nm.

<p align=center><img src="https://cdn11.bigcommerce.com/s-nnyoihm3uz/images/stencil/1280x1280/products/8155/9518/R74__32392__07317.1522033610.jpg" width=500/></p>

# Dichroics

[Permacolor 6500 "Primary Red"](https://www.bhphotovideo.com/c/product/107002-REG/Rosco_120365007508_Permacolor_Primary_Red.html), $16 for a 2 inch
square.

<p align=center><img src="https://www.gobosource.com/gos/images/6500.jpg" width=500/></p>
