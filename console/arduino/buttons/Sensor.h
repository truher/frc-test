#ifndef SENSOR_H
#define SENSOR_H
#include "Data.h"

#include <Arduino.h>
#include <Wire.h>

#include "Adafruit_NeoKey_1x4.h"
#include "seesaw_neopixel.h"

#define RED 0xFF0000
#define GREEN 0x00FF00
#define BLUE 0x0000FF
#define WHITE 0xFFFFFF

/**
 * Reads physical state: buttons, joysticks, etc. 
 * TODO: more channels
 *
 * See https://github.com/adafruit/Adafruit_Seesaw
 */
class Sensor {
public:
  /* Unpacks the uint32_t from digitalReadBulk without bit twiddling.  :-) */
  struct Keys {
    uint8_t : 4;
    bool a : 1;
    bool b : 1;
    bool c : 1;
    bool d : 1;
    uint32_t : 24;
  };

  Adafruit_NeoKey_1x4 neokey0;
  Adafruit_NeoKey_1x4 neokey1;
  Adafruit_NeoKey_1x4 neokey2;

  ReportRx prev;

  bool initialized{};

  void initialize() {
    if (!neokey0.begin(0x30)) {
      return;
    }
    if (!neokey1.begin(0x31)) {
      return;
    }
    if (!neokey2.begin(0x32)) {
      return;
    }

    initialized = true;
  }

  void lite(Adafruit_NeoKey_1x4& key, int i, uint32_t color, bool state, bool previousState) {
    if (state == previousState) {
      return;
    }
    key.pixels.setPixelColor(i, state ? color : 0x000000);
  }

  void indicate(ReportRx rpt) {
    if (rpt == prev) {  // speed up the common case
      return;
    }
    lite(neokey0, 0, 0x00ffff, rpt.i12, prev.i12);  // climb low
    lite(neokey0, 1, 0x00ffff, rpt.i15, prev.i15);  // down
    lite(neokey0, 2, 0x00ffff, rpt.i16, prev.i16);  // next
    lite(neokey0, 3, 0x00ffff, rpt.i1, prev.i1);    // lock

    lite(neokey1, 0, 0x0000ff, rpt.i11, prev.i11);  // empty
    lite(neokey1, 1, 0x00ff00, rpt.i13, prev.i13);  // shoot low
    lite(neokey1, 2, 0x00ff00, rpt.i14, prev.i14);  // high
    lite(neokey1, 3, 0x00ff00, rpt.i5, prev.i5);    // eject

    lite(neokey2, 0, 0x0000ff, rpt.i10, prev.i10);  // empty
    lite(neokey2, 1, 0x0000ff, rpt.i9, prev.i9);    // empty
    lite(neokey2, 2, 0x00ff00, rpt.i2, prev.i2);    // index
    lite(neokey2, 3, 0xff0000, rpt.i4, prev.i4);    // stop

    neokey0.pixels.show();
    neokey1.pixels.show();
    neokey2.pixels.show();
    prev = rpt;
  }

  void sense(ReportTx& reportTx) {
    uint32_t buttons0 = ~neokey0.digitalReadBulk(NEOKEY_1X4_BUTTONMASK);
    Keys k0 = *(Keys*)&buttons0;
    uint32_t buttons1 = ~neokey1.digitalReadBulk(NEOKEY_1X4_BUTTONMASK);
    Keys k1 = *(Keys*)&buttons1;
    uint32_t buttons2 = ~neokey2.digitalReadBulk(NEOKEY_1X4_BUTTONMASK);
    Keys k2 = *(Keys*)&buttons2;
    reportTx.b12 = k0.a;  // climb low
    reportTx.b15 = k0.b;  // down
    reportTx.b16 = k0.c;  // next
    reportTx.b1 = k0.d;   // lock

    reportTx.b11 = k1.a;  // empty
    reportTx.b13 = k1.b;  // shoot low
    reportTx.b14 = k1.c;  // high
    reportTx.b5 = k1.d;   // eject

    reportTx.b10 = k2.a;  // empty
    reportTx.b9 = k2.b;   // empty
    reportTx.b2 = k2.c;   // index
    reportTx.b4 = k2.d;   // stop
  }

  /* A little light show to show it's working. */
  void splash() {
    for (int i = 0; i < 4; i++) {
      neokey0.pixels.setPixelColor(i, 0xffffff);
      neokey0.pixels.show();
      delay(25);
    }
    for (int i = 0; i < 4; i++) {
      neokey1.pixels.setPixelColor(i, 0xffffff);
      neokey1.pixels.show();
      delay(25);
    }
    for (int i = 0; i < 4; i++) {
      neokey2.pixels.setPixelColor(i, 0xffffff);
      neokey2.pixels.show();
      delay(25);
    }
    for (int i = 0; i < 4; i++) {
      neokey0.pixels.setPixelColor(i, 0x000000);
      neokey0.pixels.show();
      delay(25);
    }
    for (int i = 0; i < 4; i++) {
      neokey1.pixels.setPixelColor(i, 0x000000);
      neokey1.pixels.show();
      delay(25);
    }
    for (int i = 0; i < 4; i++) {
      neokey2.pixels.setPixelColor(i, 0x000000);
      neokey2.pixels.show();
      delay(25);
    }
    for (int i = 0; i < 256; i += 16) {
      for (int k = 0; k < 4; ++k) {
        neokey0.pixels.setPixelColor(k, i, 0, 0);
        neokey1.pixels.setPixelColor(k, i, 0, 0);
        neokey2.pixels.setPixelColor(k, i, 0, 0);
      }
      neokey0.pixels.show();
      neokey1.pixels.show();
      neokey2.pixels.show();
      delay(25);
    }
    delay(750);
    for (int i = 0; i < 4; i++) {
      neokey0.pixels.setPixelColor(i, 0x000000);
      neokey1.pixels.setPixelColor(i, 0x000000);
      neokey2.pixels.setPixelColor(i, 0x000000);
    }
    neokey0.pixels.show();
    neokey1.pixels.show();
    neokey2.pixels.show();
  }
};
#endif  // SENSOR_H