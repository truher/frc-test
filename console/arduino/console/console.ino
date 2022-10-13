#include <Joystick.h>  // see https://github.com/MHeironimus/ArduinoJoystickLibrary

const int ledPin = 13;                                               // the built-in LED
const int buttonPins[] = { 12, 12, 12, 12 };                         // Arduino pin numbers, up to 32 pins
const int buttonCount = sizeof(buttonPins) / sizeof(buttonPins[0]);  // max 32
const int reportId = 0x03;                                           // this is the default

bool buttonStates[buttonCount] = {};
int axisSetting = 0;

// The class name really has a trailing underscore.  :-(
Joystick_ m_joystick(reportId, JOYSTICK_TYPE_JOYSTICK, buttonCount);

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
      axisSetting = axisSetting + 10;
      if (axisSetting > 1023) {
        axisSetting = 0;
      }
      // the axes have names instead of numbers.  :-(
      m_joystick.setXAxis(axisSetting);        // axis 0
      m_joystick.setYAxis(axisSetting);        // axis 1
      m_joystick.setZAxis(axisSetting);        // axis 2
      m_joystick.setRxAxis(axisSetting);       // axis 3
      m_joystick.setRyAxis(axisSetting);       // axis 4
      m_joystick.setRzAxis(axisSetting);       // axis 5
      m_joystick.setThrottle(axisSetting);     // axis 6
      m_joystick.setRudder(axisSetting);       // axis 7
      m_joystick.setSteering(axisSetting);     // axis 8
      m_joystick.setAccelerator(axisSetting);  // axis 9
      m_joystick.setBrake(axisSetting);        // axis 10
      needUpdate = true;
    }
  }
  if (needUpdate) {
    digitalWrite(ledPin, HIGH);
    m_joystick.sendState();
    digitalWrite(ledPin, LOW);
  }
}