#include <Joystick.h>  // see https://github.com/MHeironimus/ArduinoJoystickLibrary

const int ledPin = 13;  // the built-in LED

// Arduino pin numbers, up to 32 pins
const int buttonPins[] = { 12, 12, 12, 12, 12, 12, 12, 12,
                           12, 12, 12, 12, 12, 12, 12, 12,
                           12, 12, 12, 12, 12, 12, 12, 12,
                           12, 12, 12, 12, 12, 12, 12, 12 };
const int buttonCount = sizeof(buttonPins) / sizeof(buttonPins[0]);  // max 32
const int reportId = 0x03;                                           // this is the default

bool buttonStates[buttonCount] = {};
int axisSetting = 0;

// The class name really has a trailing underscore.  :-(
// while 11 channels are possible here, only 9 are allowed by the driver station.
// note that the names used here don't necessarily correspond to the names in the DS.
Joystick_ m_joystick(reportId,
                     JOYSTICK_TYPE_JOYSTICK,
                     buttonCount,
                     2,       // hatswitches, 2 is max
                     true,    // x
                     true,    // y
                     true,    // z
                     true,    // rx
                     true,    // ry
                     true,    // rz
                     true,    // rudder
                     true,    // throttle
                     true,    // accel
                     false,   // brake
                     false);  // steer

void setup() {
  pinMode(ledPin, OUTPUT);  // indicate updates
  for (int i = 0; i < buttonCount; ++i) {
    pinMode(buttonPins[i], INPUT);
  }
  m_joystick.begin(false);  // batch updates
}

void loop() {
  bool needUpdate = false;
  for (int i = 0; i < buttonCount; ++i) {
    bool desiredButtonState = digitalRead(buttonPins[i]);
    if (desiredButtonState != buttonStates[i]) {
      buttonStates[i] = desiredButtonState;
      m_joystick.setButton(i, desiredButtonState);
      axisSetting = axisSetting + 10; // exercise the axes
      if (axisSetting > 1023) {
        axisSetting = 0;
      }
      m_joystick.setXAxis(axisSetting);
      m_joystick.setYAxis(axisSetting);
      m_joystick.setZAxis(axisSetting);
      m_joystick.setRxAxis(axisSetting);
      m_joystick.setRyAxis(axisSetting);
      m_joystick.setRzAxis(axisSetting);
      m_joystick.setThrottle(axisSetting);
      m_joystick.setRudder(axisSetting);
      m_joystick.setSteering(axisSetting);
      m_joystick.setAccelerator(axisSetting);
      m_joystick.setBrake(axisSetting);
      needUpdate = true;
    }
  }
  if (needUpdate) {
    digitalWrite(ledPin, HIGH);
    m_joystick.sendState();
    digitalWrite(ledPin, LOW);
  }
}