#ifndef DACTONE_H
#define DACTONE_H
#include <Arduino.h>
#include <Adafruit_MCP4725.h>

/**
 * Cribbed from arduino/Tone.cpp, just for the ATmega32U4, using the 16-bit timer3.
 * Aiming for simple and lightweight, not too much DAC traffic, not a full synthesizer/sequencer.
 *
 * See https://cdn.sparkfun.com/datasheets/Dev/Arduino/Boards/ATMega32U4.pdf
 * See https://github.com/adafruit/Adafruit_MCP4725
 */
class DacTone {
public:
  static void begin() {
    dac.begin(0x62);      // TODO: correct address
    TCCR3A = 0b00000000;  // no output pin, normal waveform (count up)
    TCCR3B = 0b00001010;  // CS31 = clk_io/8 prescaled, and WGM32 = clear timer on match
    stop();
  }

  static void start(uint16_t frequency_hz) {
    OCR3A = (F_CPU >> 4) / frequency_hz;  // 8:1 scale * 2 for toggling, ok for 20-20khz
    TIMSK3 = 0b00000010;                  // OCIE3A = enable interrupt "A" for output compare match
  }

  static void stop() {
    TIMSK3 = 0b00000000;       // disable all interrupts
    dac.setVoltage(0, false);  // zero the output
  }

  static void DacToneISR() {
    dac.setVoltage(state ? 4095 : 0, false);  // 12b
    state ^= 1;
  }

private:
  inline static Adafruit_MCP4725 dac;
  inline static volatile bool state;
};

ISR(TIMER3_COMPA_vect) {
  DacTone::DacToneISR();
};
#endif  // DACTONE_H