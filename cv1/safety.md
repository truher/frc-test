# Safety

Radiation of any wavelength can dangerous.  

* [Very long-wave radiation](https://www.fcc.gov/engineering-technology/electromagnetic-compatibility-division/radio-frequency-safety/faq/rf-safety)
(e.g. radio waves) can heat parts of the body not accustomed to being heated.
* [Very short-wave radiation](https://www.osha.gov/laws-regs/regulations/standardnumber/1910/1910.1096)
(e.g. x-rays) can directly damage molecules in the body, which can increase cancer risk.
* Ultraviolet light obviously can cause sunburn.
* [Infrared light](https://en.wikipedia.org/wiki/Glassblower%27s_cataract)
can cause cataracts after tens of thousands of hours of exposure.
* Even [visible blue light](https://www.sciencedirect.com/science/article/pii/S0753332220307708)
in high doses can affect the eye through a number of mechanisms, for example retinal inflammation.

When you're building a device that emits radiation of any kind, you need to understand these hazards,
and make sure that your device won't cause injury to any people nearby.

## Safety Standards

There are several equivalent standards (see appendix); we'll be working through the method specified by the
International Commission on Non-Ionizing Radiation Protection (ICNIRP),
[ICNIRP Guidelines on Limits of Exposure to Incoherent Visible and Infrared Radiation."](https://www.icnirp.org/cms/upload/publications/ICNIRPVisible_Infrared2013.pdf).
I encourage you to read and understand the
guidelines in total (it's only 26 pages).
I'll try to limit discussion here to implications of the guidelines, rather than covering everything.
The math is covered in [this python notebook](https://colab.research.google.com/drive/1T6OjM1fbkWaqULhcl7OmBOcejc7mjztV?usp=sharing).

## Types of harm

There are eight hazards to evaluate (TODO: use the ordering from ICNIRP)

1. [Actinic](https://www.ncbi.nlm.nih.gov/books/NBK401580/) UV, 200-400 nm (irradiance)
2. UVA, 315-400 nm (irradiance)
3. Blue light, 300-700 nm (radiance)
4. Blue light small source, 300-700 nm (irradiance)
5. Thermal, 380-1400 nm (radiance)
6. Thermal invisible, 780-1400 nm (radiance)
7. Infrared 780-3000 nm (irradiance)
8. Thermal skin, 380-3000 nm (irradiance)

Some of the hazards involve harm to the surface of the body (e.g. cornea, skin), and these depend on the
["irradiance"](https://en.wikipedia.org/wiki/Irradiance) of that body surface, which means the total amount of light per area.
Other hazards involve harm to the retina, where the light is focused, and these generally depend on 
["radiance"](https://en.wikipedia.org/wiki/Radiance) (loosely, apparent "brightness") of the source.

## Classification of risk

For each of the hazards above, an emitter may be classified into one of four groups according to the IEC standard:

1. __Exempt__: no hazard
2. __Group 1 (low risk)__: no hazard assuming normal human behavior
3. __Group 2 (moderate risk)__: no hazard due to aversion response
4. __Group 3 (high risk)__: hazardous even for momentary exposure

We will be designing for group 1: no hazard at all, for all hazards, because we can't restrict or police the population of bystanders to be sure
their behavior is "normal."

## The design

For the purposes of this safety analysis, the illuminator design has several parameters:

* wavelength
* strobe duration and duty cycle
* surface brightness per emitter
* total output

## Retinal thermal hazards (380&ndash;1,400 nm)

Some retinal hazards vary with wavelength, as described in the "hazard functions", table 2 and figure 5 in the doc, duplicated here:

<p align=center><img src="https://drive.google.com/uc?export=view&id=1ZXQJZHQRQGgV23JVudl0XwG051RoYHag" width=640/></p>

For thermal hazards, weigh the spectral radiance by the "Thermal" hazard function above and integrate, to get the __effective retinal thermal radiance__,
$L_R$ (W m<sup>-2</sup> sr<sup>-1</sup>).  For any source we might use, the thermal hazard function is 1.0, so the source radiance
can be used directly.  For the [Cree XP-E2 LEDs](https://cree-led.com/media/documents/XLampXPE2.pdf) we have in mind to use,
the radiant flux is specified at 350mA, between 350 and 675 mW, depending on color.  The correction for maximum continuous current (1 amp)
is about 275%, i.e. the output is close to linear with current, the 350&ndash;675 mW output might be 1000&ndash;2000mW.
The [overdriving guidance](https://cree-led.com/media/documents/XLampPulsedCurrent.pdf)
says that the luminous efficiency at 2.5X maximum current is something like 60% of the 1X maximum; extrapolating, we can estimate that
the efficiency at 3X is half the 1X efficiency.  Increased forward voltage is one of the drivers of the drop in efficiency; extrapolating
slightly from the "typical" table, we find a 24% increase from 1X to 3X current.

## Blue light hazard

Given recent attention to blue light hazard, the CIE clarified that blue light is
[not a hazard for everyday situations](https://cie.co.at/publications/position-statement-blue-light-hazard-april-23-2019)
but also pointed out that, for people who might not behave "normally" (i.e. a child who might stare at a blue light for a long 
time because they find it fascinating), the (non-exempt) exposure limits should be __reduced by a factor of ten.__  

<hr>

## Appendix

* ICNIRP provided a special [statement on LEDs](https://www.icnirp.org/cms/upload/publications/ICNIRPled2020.pdf)
which reviews some of the hazards specific to LEDs and recommends that LEDs be analyzed using the incoherent broadband standards used above.
* ICNIRP first published guidelines in 1997,
[Guidelines on Limits of Exposure to Broad-band Incoherent Optical Radiation (0.38 to 3 &micro;m)](https://www.icnirp.org/cms/upload/publications/ICNIRPbroadband.pdf)
* The same calculations are also available from IEC/CIE as [IEC 62471, "Photobiological Safety of Lamps and Lamp Systems."](https://cie.co.at/publications/photobiological-safety-lamps-and-lamp-systems-s-curit-photobiologique-des-lampes-et-des),
which is [summarized here](https://smartvisionlights.com/wp-content/uploads/IEC_62471_summary.pdf).
* Another version of the same method is available from ANSI as [ANSI/IESNA RP-27, "Recommended Practice for Photobiological Safety for
Lamps and Lamp Systems-Measurement Techniques"](https://webstore.ansi.org/preview-pages/IESNA/preview_ANSI+IESNA+RP-27.2-00.pdf)
