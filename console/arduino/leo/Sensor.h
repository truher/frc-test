#ifndef SENSOR_H
#define SENSOR_H
#include "Data.h"
#include <Arduino.h>
#include <Wire.h>

#define BUTTON_1 0
#define BUTTON_2 1

/**
 * Reads physical state: buttons, joysticks, etc. 
 */
class Sensor {
public:
  Data& data_;
  Sensor::Sensor(Data& data)
    : data_(data) {
    pinMode(BUTTON_1, INPUT_PULLUP);
    pinMode(BUTTON_2, INPUT_PULLUP);
  }
  /** 
   * Updates state with inputs, return true if anything changed.
   * TODO: more channels
   */
  bool Sensor::sense() {
    bool updated = false;

    bool readValue = !(bool)digitalRead(BUTTON_1);
    if (readValue != data_.workingReportTx_.b1) {
      data_.workingReportTx_.b1 = readValue;
      updated = true;
    }

    readValue = !(bool)digitalRead(BUTTON_2);
    if (readValue != data_.workingReportTx_.b2) {
      data_.workingReportTx_.b2 = readValue;
      updated = true;
    }
    return updated;
  }
};
#endif  // SENSOR_H