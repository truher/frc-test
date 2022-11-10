#ifndef SENSOR_H
#define SENSOR_H
#include "Data.h"

#include <Arduino.h>
#include <Wire.h>

#include "Adafruit_NeoKey_1x4.h"
#include "seesaw_neopixel.h"

/**
 * Reads physical state: buttons, joysticks, etc. 
 * TODO: more channels
 *
 * See https://github.com/adafruit/Adafruit_Seesaw
 */
class Sensor {
public:
  Adafruit_NeoKey_1x4 neokey_array[3];
  Data& data_;

  Sensor(Data& data)
    : neokey_array{
        Adafruit_NeoKey_1x4(0x30),
        Adafruit_NeoKey_1x4(0x31),
        Adafruit_NeoKey_1x4(0x32)
      },
      data_(data) {

    for (int i = 0; i < 3; ++i) {
      if (neokey_array[i].begin()) {
        while (1) delay(10);  // TODO: complain via USB
      }
    }

    // Pulse all the LEDs on to show we're working
    for (int j = 0; j < 3; ++j) {
      for (uint16_t i = 0; i < 4; i++) {
        neokey_array[j].pixels.setPixelColor(i, 0xffffff);
        neokey_array[j].pixels.show();
        delay(50);
      }
    }
    for (int j = 0; j < 3; ++j) {
      for (uint16_t i = 0; i < 4; i++) {
        neokey_array[j].pixels.setPixelColor(i, 0x000000);
        neokey_array[j].pixels.show();
        delay(50);
      }
    }
  }

  /** 
   * Updates state with inputs, return true if anything changed.
   */
  bool sense() {

    bool updated = false;
    bool readValue = false;
    uint32_t buttons = 0;

    buttons = neokey_array[0].digitalReadBulk(NEOKEY_1X4_BUTTONMASK);
    buttons ^= NEOKEY_1X4_BUTTONMASK;

    readValue = buttons & 0x00010000;
    if (readValue != data_.reportTx_.b1) {
      data_.reportTx_.b1 = readValue;
      updated = true;
    }
    readValue = buttons & 0x00100000;
    if (readValue != data_.reportTx_.b2) {
      data_.reportTx_.b2 = readValue;
      updated = true;
    }
    readValue = buttons & 0x01000000;
    if (readValue != data_.reportTx_.b3) {
      data_.reportTx_.b3 = readValue;
      updated = true;
    }
    readValue = buttons & 0x10000000;
    if (readValue != data_.reportTx_.b4) {
      data_.reportTx_.b4 = readValue;
      updated = true;
    }

    buttons = neokey_array[1].digitalReadBulk(NEOKEY_1X4_BUTTONMASK);
    buttons ^= NEOKEY_1X4_BUTTONMASK;
 
    readValue = buttons & 0x00010000;
    if (readValue != data_.reportTx_.b5) {
      data_.reportTx_.b5 = readValue;
      updated = true;
    }
    readValue = buttons & 0x00100000;
    if (readValue != data_.reportTx_.b6) {
      data_.reportTx_.b6 = readValue;
      updated = true;
    }
    readValue = buttons & 0x01000000;
    if (readValue != data_.reportTx_.b7) {
      data_.reportTx_.b7 = readValue;
      updated = true;
    }
    readValue = buttons & 0x10000000;
    if (readValue != data_.reportTx_.b8) {
      data_.reportTx_.b8 = readValue;
      updated = true;
    }

    buttons = neokey_array[2].digitalReadBulk(NEOKEY_1X4_BUTTONMASK);
    buttons ^= NEOKEY_1X4_BUTTONMASK;

    readValue = buttons & 0x00010000;
    if (readValue != data_.reportTx_.b9) {
      data_.reportTx_.b9 = readValue;
      updated = true;
    }
    readValue = buttons & 0x00100000;
    if (readValue != data_.reportTx_.b10) {
      data_.reportTx_.b10 = readValue;
      updated = true;
    }
    readValue = buttons & 0x01000000;
    if (readValue != data_.reportTx_.b11) {
      data_.reportTx_.b11 = readValue;
      updated = true;
    }
    readValue = buttons & 0x10000000;
    if (readValue != data_.reportTx_.b12) {
      data_.reportTx_.b12 = readValue;
      updated = true;
    }
    return updated;
  }
};
#endif  // SENSOR_H