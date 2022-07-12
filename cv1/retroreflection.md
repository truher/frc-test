# Retroreflection

The key features perceived by the vision system are the retroreflective tape targets provided in the FRC field.  In order
to understand how to best build a system around them, we should first understand a bit about retroreflection.

First, what is it exactly?  The [game manual](https://firstfrc.blob.core.windows.net/frc2022/Manual/2022FRCGameManual.pdf) tells us
that the tape used on the field is [3M 8830 Scotchlite™ Reflective Material](https://www.3m.com/3M/en_US/p/d/v000312582/), which is more fully
described in the [datasheet](https://multimedia.3m.com/mws/media/662425O/reflective-psa-technical-data-sheet.pdf).  It's a polyester film
topped with microscopic glass spheres:

<p align=center><img src="https://upload.wikimedia.org/wikipedia/commons/thumb/1/19/Reflective_leg_band_in_Scanning_Electron_Microscope%2C_15x.GIF/600px-Reflective_leg_band_in_Scanning_Electron_Microscope%2C_15x.GIF" width=640/></p>

Each sphere is about 75 micrometers (&micro;m, "micron") in diameter.

What do these spheres do?

They reflect light back the way it came:

<p align=center><img src="https://illumin.usc.edu/wp-content/uploads/2022/02/image9-1-768x768.png" width=640/></p>

So when you hold a flashlight near your eye and shine it at the retrorefletor, it appears quite bright.  The flashlight
needs to be pretty close to your eye for this to work.  Try it and see.



This is $R_A$ by entrance angle:

<p align=center><img src="https://docs.google.com/spreadsheets/d/e/2PACX-1vS9LfUmscBJBon-H0nxE4GovaWxXLS5JhTjmkOymTXKV8SPol-0FX14R36codUMJB_g8UcuQVn0SOpA/pubchart?oid=836368983&format=image" width=640/></p>

This is $R_A$ by observer angle

<p align=center><img src="https://docs.google.com/spreadsheets/d/e/2PACX-1vS9LfUmscBJBon-H0nxE4GovaWxXLS5JhTjmkOymTXKV8SPol-0FX14R36codUMJB_g8UcuQVn0SOpA/pubchart?oid=1624612283&format=image" width=640/></p>

The spreadsheet containing these charts is [here](https://docs.google.com/spreadsheets/d/1KAr34-RtiIM0Fbr4D6O5UhFfduAykVAWz9x5DW8REuw/edit#gid=0)
