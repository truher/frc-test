//#include <Joystick.h>

//#include <Joystick.h>  // see https://github.com/MHeironimus/ArduinoJoystickLibrary
#include "Joystick2.h"
//#include "PluggableUSB.h"
//#include "HID.h"
const int ledPin = 13;  // the built-in LED

// Arduino pin numbers, up to 32 pins
const int buttonPins[] = { 12, 12, 12, 12, 12, 12, 12, 12,
                           12, 12, 12, 12, 12, 12, 12, 12,
                           12, 12, 12, 12, 12, 12, 12, 12,
                           12, 12, 12, 12, 12, 12, 12, 12 };
const int buttonCount = sizeof(buttonPins) / sizeof(buttonPins[0]);  // max 32
const int reportId = 0x03;                                           // this is the default

bool buttonStates[buttonCount] = {};
uint16_t axisSetting = 0;

//Joystick2 mm;

// The class name really has a trailing underscore.  :-(
// while 11 channels are possible here, only 9 are allowed by the driver station.
// note that the names used here don't necessarily correspond to the names in the DS.
// now it has 32 buttons no matter what
Joystick_ m_joystick;

void setup() {
  Serial.begin(9600);
  pinMode(ledPin, OUTPUT);  // indicate updates
  for (int i = 0; i < buttonCount; ++i) {
    pinMode(buttonPins[i], INPUT);
  }
  m_joystick.begin();
  //DynamicHID().prepareOutput(void *report, int length);
}

void loop() {
  int availableData = DynamicHID().available();
  uint8_t firstByte = 0;
  if (availableData > 0) {
    uint8_t firstByte = DynamicHID().read();
    for (int i = 0; i < availableData-1;i++) {
      Serial.println("holy shit");
      DynamicHID().read();
    }
    if (m_joystick.reportRx.indicators > 0) {
      digitalWrite(ledPin, HIGH);
    } else {
      digitalWrite(ledPin, LOW);
    }
    // if (bitRead(firstByte, 0)) {
    //   digitalWrite(ledPin, HIGH);
    // } else {
    //   digitalWrite(ledPin, LOW);
    // }
  }
 // int readval = m_joystick.read();
 // if (readval > 0) {
 //   // something changed, so reassert everything
 //   //Serial.println("indicators");
 //   //Serial.println(m_joystick.reportRx.indicators, BIN);
 //   if (m_joystick.reportRx.indicators > 0) {
 //     digitalWrite(ledPin, HIGH);
 //   } else {
 //     digitalWrite(ledPin, LOW);
 //   }
 // }

  // handle writes
  bool needUpdate = false;
  for (int i = 0; i < buttonCount; ++i) {
    bool desiredButtonState = digitalRead(buttonPins[i]);
    if (desiredButtonState != buttonStates[i]) {
      buttonStates[i] = desiredButtonState;
      m_joystick.setButton(i, desiredButtonState);
      axisSetting = axisSetting + 100;  // exercise the axes
      if (axisSetting > 65535) {
        axisSetting = 0;
      }
      // #### try writing the thing we just read

      m_joystick.reportTx.x = firstByte;
      m_joystick.reportTx.y = axisSetting;
      m_joystick.reportTx.z = axisSetting;
      m_joystick.reportTx.rx = axisSetting;
      m_joystick.reportTx.ry = axisSetting;
      m_joystick.reportTx.rz = axisSetting;
      needUpdate = true;
    }
  }
  if (needUpdate) {
    digitalWrite(ledPin, HIGH);
    m_joystick.sendState();
    digitalWrite(ledPin, LOW);
  }
}