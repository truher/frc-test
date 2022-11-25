#ifndef DACTONE_H
#define DACTONE_H

// cribbed from arduino Tone lib

// i kinda like real classes but the tone lib is, like, not a class
// and interrupts need to be global/static.

// to make "private static" fields, make them global to this file?
// nope, it's a header file.

// the timer itself is a singleton, so the rest of the state is too?

#include <Arduino.h>

// see https://github.com/adafruit/Adafruit_MCP4725
#include <Adafruit_MCP4725.h>

class DacTone {
public:
  static void begin() {
    dac.begin(0x62);  // TODO: correct address
    dacstate = false;
    TCCR3A = 0;
    TCCR3B = 0;
    bitWrite(TCCR3B, WGM32, 1);
    bitWrite(TCCR3B, CS30, 1);
  }

  static void tone() {
    uint8_t prescalarbits = 0b001;
    long toggle_count = 0;
    uint32_t ocr = 0;
    int8_t _timer;
    TCCR3B = (TCCR3B & 0b11111000) | prescalarbits;
    OCR3A = ocr;
    timer3_toggle_count = toggle_count;
    bitWrite(TIMSK3, OCIE3A, 1);
  }

  static void noTone() {
    bitWrite(TIMSK3, OCIE3A, 0);  // stop the timer
  }

  static void DacToneISR() {
    if (timer3_toggle_count == 0) {
      bitWrite(TIMSK3, OCIE3A, 0);  // stop the timer
      dac.setVoltage(0, false);     // zero the output
      return;
    }
    dacstate = not dacstate;
    dac.setVoltage(dacstate ? 4095 : 0, false);
    if (timer3_toggle_count > 0)
      timer3_toggle_count--;
  }
private:
  inline static volatile long timer3_toggle_count;
  inline static Adafruit_MCP4725 dac;
  inline static volatile bool dacstate;  // TODO: multiple voices, waveforms
};

ISR(TIMER3_COMPA_vect) {
  DacTone::DacToneISR();
};
#endif  // DACTONE_H