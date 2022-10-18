#ifndef INDICATOR_H
#define INDICATOR_H
#include "Data.h"

/**
 * Expresses physical state: lights, sounds, etc.
 */
class Indicator {
public:
  Data& data_;
  Indicator::Indicator(Data& data)
    : data_(data) {
    pinMode(LED_BUILTIN, OUTPUT);
  }

  /** 
   * Indicating reasserts everything.
   * TODO: only some of them
   * TODO: more outputs
   */
  void Indicator::indicate() {
    digitalWrite(LED_BUILTIN, data_.reportRx_.i1);
  }
};

#endif  // INDICATOR_H