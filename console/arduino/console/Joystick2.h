#ifndef JOYSTICK2_h
#define JOYSTICK2_h

#include "Dynamic2HID.h"

class Joystick_ {
public:

  // 16 byte report
  typedef struct {
    uint16_t x;
    uint16_t y;
    uint16_t z;
    uint16_t rx;
    uint16_t ry;
    uint16_t rz;
    uint32_t buttons;
  } ReportTx;
  volatile ReportTx reportTx;

  // 2 byte report
  typedef struct {
    uint16_t indicators;
  } ReportRx;
  volatile ReportRx reportRx;

  Joystick_();
  void begin();
  void end();
  void setButton(uint8_t button, uint8_t value);
  void pressButton(uint8_t button);
  void releaseButton(uint8_t button);
  void sendState();
  // return bytes read
 // int read();
};

#endif  // JOYSTICK2_h