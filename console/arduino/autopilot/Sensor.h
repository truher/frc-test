#ifndef SENSOR_H
#define SENSOR_H
#include "Data.h"
#include <Arduino.h>

class Sensor {
public:
  void initialize() {}
  bool sense(ReportTx& reportTx) {}
  void indicate(ReportRx rpt) {}
};
#endif  // SENSOR_H