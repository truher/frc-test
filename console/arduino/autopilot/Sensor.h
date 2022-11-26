#ifndef SENSOR_H
#define SENSOR_H
#include "Data.h"
#include "DacTone.h"
#include <Arduino.h>
// see https://github.com/sparkfun/SparkFun_TPA2016D2_Arduino_Library
#include <SparkFun_TPA2016D2_Arduino_Library.h>

class Sensor {
public:
  void initialize() {
    DacTone::begin();
    Wire.begin();
    if (amp.begin() == false) {
      while (1)
        ;
    }
    // for now just turn it on
    // TODO: adjust gain with knob
    amp.enableRightSpeaker();
    amp.enableLeftSpeaker();
    amp.disableLimiter();
    amp.disableNoiseGate();
    amp.writeRelease(1);
    amp.writeAttack(1);
    amp.writeFixedGain(15);  // half
    stop();
  }

  bool sense(ReportTx& reportTx) {}

  void indicate(ReportRx rpt) {
    if (rpt.i1) {
      beep(600, 300);
    } else if (rpt.i2) {
      steady(1600);
    } else {
      stop();
    }
  }
private:
  TPA2016D2 amp;
  uint32_t beep_timeout_ms;
  bool beeping;

  void beep(uint16_t frequency_hz, uint16_t duration_ms) {
    uint32_t now_ms = millis();
    if (now_ms > beep_timeout_ms) {  // flip state
      if (beeping) {
        DacTone::stop();
      } else {
        DacTone::start(frequency_hz);
      }
      beep_timeout_ms = now_ms + duration_ms;
      beeping ^= 1;
    }
  }

  void steady(uint16_t frequency_hz) {
    beep_timeout_ms = 0;
    beeping = false;
    DacTone::start(frequency_hz);
  }

  void stop() {
    DacTone::stop();
    beep_timeout_ms = 0;
    beeping = false;
  }
};
#endif  // SENSOR_H