#ifndef SENSOR_H
#define SENSOR_H
#include "Data.h"
#include <Arduino.h>
// see https://github.com/sparkfun/SparkFun_TPA2016D2_Arduino_Library
#include <SparkFun_TPA2016D2_Arduino_Library.h>

class Sensor {
public:
  void initialize() {
    pinMode(RXLED, OUTPUT);
    pinMode(SOUNDPIN, OUTPUT);
    Wire.begin();
    if (amp.begin() == false) {
      while (1) {
        delay(1000);
        Serial.println("amp begin failed");
      }
    } else {
      Serial.println("amp begin succeeded");
    }
    amp.disableShutdown();
    amp.enableSpeakers();
    amp.disableLimiter();
    amp.disableNoiseGate();
    amp.writeAttack(0);
    amp.writeHold(0);
    amp.writeRelease(0);
    amp.writeFixedGain(0);
    amp.writeMaxGain(0);
    stop();
  }

  bool sense(ReportTx& reportTx) {}

  void indicate(ReportRx rpt) {


    beep(600, 150);
    //steady(1600);

    // TODO: hook up the keys and use this
    // if (rpt.i1) {
    //   beep(600, 150);
    // } else if (rpt.i2) {
    //   steady(1600);
    // } else {
    //   stop();
    // }
  }
private:
  TPA2016D2 amp;
  uint32_t beep_timeout_ms;
  bool beeping;
  static const uint8_t RXLED = 17;
  static const uint8_t SOUNDPIN = 9;


  void beep(uint16_t frequency_hz, uint16_t duration_ms) {
    uint32_t now_ms = millis();
    if (now_ms > beep_timeout_ms) {  // flip state
      if (beeping) {
        digitalWrite(RXLED, LOW);
        noTone(SOUNDPIN);
      } else {
        digitalWrite(RXLED, HIGH);
        tone(SOUNDPIN, frequency_hz);
      }
      beep_timeout_ms = now_ms + duration_ms;
      beeping ^= 1;
    }
  }

  void steady(uint16_t frequency_hz) {
    beep_timeout_ms = 0;
    beeping = false;
    tone(SOUNDPIN, frequency_hz);
  }

  void stop() {
    analogWrite(SOUNDPIN, 0);
    beep_timeout_ms = 0;
    beeping = false;
  }
};
#endif  // SENSOR_H