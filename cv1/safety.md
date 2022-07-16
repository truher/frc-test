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

# Safety Standards

The main safety standard for light sources is [IEC 62471, "Photobiological Safety of Lamps and Lamp Systems."](https://cie.co.at/publications/photobiological-safety-lamps-and-lamp-systems-s-curit-photobiologique-des-lampes-et-des),
also expressed as [ANSI/IESNA RP-27, "Recommended Practice for Photobiological Safety for
Lamps and Lamp Systems-Measurement Techniques"](https://webstore.ansi.org/preview-pages/IESNA/preview_ANSI+IESNA+RP-27.2-00.pdf)

According to [this summary](https://smartvisionlights.com/wp-content/uploads/IEC_62471_summary.pdf), there are eight
hazards to evaluate:

1. [Actinic](https://www.ncbi.nlm.nih.gov/books/NBK401580/) UV, 200-400 nm
2. UVA, 315-400 nm
3. Blue light, 300-700 nm
4. Blue light small source, 300-700 nm
5. Thermal, 380-1400 nm
6. Thermal invisible, 780-1400 nm
7. Infrared 780-3000 nm
8. Thermal skin, 380-3000 nm

For each of these hazards, an emitter may be classified into one of four groups:

1. __Exempt__: no hazard
2. __Group 1 (low risk)__: no hazard assuming normal human behavior
3. __Group 2 (moderate risk)__: no hazard due to aversion response
4. __Group 3 (high risk)__: hazardous even for momentary exposure

We will be designing for group 1: no hazard at all, for all hazards, because we can't restrict or police the population of bystanders to be sure
their behavior is "normal."

We'll be working through the math specified [guidelines](https://www.icnirp.org/cms/upload/publications/ICNIRPbroadband.pdf)
and the special [statement on LEDs](https://www.icnirp.org/cms/upload/publications/ICNIRPled2020.pdf)
provided by the International Commission on Non-Ionizing Radiation Protection (ICNIRP).

## Blue light hazard

Given recent attention to blue light hazard, the CIE clarified that blue light is
[not a hazard for everyday situations](https://cie.co.at/publications/position-statement-blue-light-hazard-april-23-2019)
but also pointed out that, for people who might not behave "normally" (i.e. a child who might stare at a blue light for a long 
time because they find it fascinating), the (non-exempt) exposure limits should be __reduced by a factor of ten.__  
