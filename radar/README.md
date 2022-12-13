# Radar

NT4 app that shows camera telemetry.  Run it as a DS dashboard.

## Gradle

The hardest part of the demo was learning enough about Gradle and the WPI Gradle plugins
to build the app.  I cribbed from Shuffleboard, and I'm still not sure it's right, but
it does work.

I use the vscode "Gradle for Java" extension.  To run a build, use Tasks>shadow>shadowJar.
This will produce a "fat jar" (i.e. one that incorporates its dependencies, like static
linking) in $projecthome/build/libs.

## Native libraries

Another hard part was understanding how to load the required native libraries.  It's not
conceptually hard: the "shadowJar" task makes a jar with various dll's in it, and to use them,
you read them out of the jar, write them somewhere convenient, and ask the OS to load them.
WPI provides a few ways to do that, but not every method works the same; in particular,
you have to enumerate the dependent libraries in the gradle, and also in the application-level
loader code.  There are also loaders in static initializers but they seem not to work
correctly, so I disabled them.

## What the demo does

The real point of the demo is to show how you might pass structured telemetry through
Network Tables using the new MessagePack support.  To make it interesting and useful, the
demo models visual targets (e.g. AprilTags or other robots), and also includes a very
basic visualizer that can be run from the Driver Station.

## Running the demo

Build the jar ("shadowJar" in vscode) and then find it
in $projecthome/build/libs.  You'll run it two times, one for the producer and
one for the consumer.

First the producer:

'''
java -jar Radar-winx64.jar
'''

This will run an NT4 server locally, and also connect a client to it, publishing fake
telemetry.

Now run the consumer:

'''
java -jar Radar-winx64.jar map targets
'''

This should result in two windows, side by side.  The left side shows
the "north up" view, where the robot moves and the world is fixed.
The right side shows the "head up" view, where the robot is fixed in the
center facing up, and the world pivots around it.

## Running the dashboard from the driver station

The driver station can execute any command as the "dashboard," specified
in this file:

'''
"C:\Users\Public\Documents\FRC\FRC DS Data Storage.ini"
'''

Edit the line starting with "DashboardCmdLine" to run the jar; on my
machine this line looks like this:

'''
DashboardCmdLine = "java -jar "C:\\Users\\joelt\\FRC\\frc-test\\radar\\build\\libs\\Radar-winx64.jar" map targets"
'''

You'll have to modify this line to point at the actual jar.
Note the double backslashes; a single backslash is a special character.