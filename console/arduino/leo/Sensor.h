#ifndef SENSOR_H
#define SENSOR_H
#include "Data.h"

/**
 * Reads physical state: buttons, joysticks, etc. 
 */
class Sensor {
public:
  Data& data_;
  Sensor::Sensor(Data& data)
    : data_(data) {
    pinMode(1, INPUT_PULLUP);  // this is the one i happen to be using
  }
  /** 
   * Updates state with inputs, return true if anything changed.
   * TODO: more channels
   */
  bool Sensor::sense() {
    bool updated = false;
    bool readValue = (bool)digitalRead(1);
    if (readValue != data_.workingReportTx_.b2) {
      updated = true;
      data_.workingReportTx_.b2 = readValue;
    }
    return updated;
  }
};
#endif  // SENSOR_H