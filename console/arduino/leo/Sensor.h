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
    if (readValue != data_.reportTx_.b1) {
      data_.reportTx_.b1 = readValue;
      // fake an axis
      data_.reportTx_.x =  -15000;
      data_.reportTx_.y =  -10000;
      data_.reportTx_.z =   -5000;
      data_.reportTx_.rx =   5000;
      data_.reportTx_.ry =  15000;
      data_.reportTx_.rz =  25000;
      updated = true;
    }

    readValue = !(bool)digitalRead(BUTTON_2);
    if (readValue != data_.reportTx_.b2) {
      data_.reportTx_.b2 = readValue;
      data_.reportTx_.x =  0;
      data_.reportTx_.y =  0;
      data_.reportTx_.z =  0;
      data_.reportTx_.rx = 0;
      data_.reportTx_.ry = 0;
      data_.reportTx_.rz = 0;
      updated = true;
    }
    return updated;
  }
};
#endif  // SENSOR_H