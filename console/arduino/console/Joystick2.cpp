#include "Joystick2.h"

Joystick_::Joystick_() {

  uint8_t axisCount = 6;

  static const uint8_t customHidReportDescriptor[] = {
    0x05, 0x01,        // USAGE_PAGE (Generic Desktop)
    0x09, 0x04,        // USAGE (Joystick: 0x04)    
    0xa1, 0x01,        // COLLECTION (Application)
    0x85, 0x03,        // ....REPORT_ID (Default: 3 hidReportId) (required part of app collection)
    0x05, 0x01,        // ....USAGE_PAGE (Generic Desktop)
    0x09, 0x01,        // ....USAGE (Pointer)
    0xA1, 0x00,        // ....COLLECTION (Physical)
    0x09, 0x30,        // ........USAGE (X)
    0x09, 0x31,        // ........USAGE (Y)
    0x09, 0x32,        // ........USAGE (Z)
    0x09, 0x33,        // ........USAGE (Rx)
    0x09, 0x34,        // ........USAGE (Ry)
    0x09, 0x35,        // ........USAGE (Rz) (TODO add three more here)
    0x15, 0x00,        // ........LOGICAL_MINIMUM (0)
    0x26, 0XFF, 0XFF,  // ........LOGICAL_MAXIMUM (65535) (TODO make this match ADC)
    0x75, 0x10,        // ........REPORT_SIZE (16)
    0x95, 0x06,        // ........REPORT_COUNT (axisCount) (TODO more axes)
    0x81, 0x02,        // ........INPUT (Data,Var,Abs)
    0x05, 0x09,        // ....USAGE_PAGE (Button)
    0x19, 0x01,        // ....USAGE_MINIMUM (Button 1)
    0x29, 0x20,        // ....USAGE_MAXIMUM (Button 32)
    0x15, 0x00,        // ....LOGICAL_MINIMUM (0)
    0x25, 0x01,        // ....LOGICAL_MAXIMUM (1) (msp has physical min/max below this line)
    0x75, 0x01,        // ....REPORT_SIZE (1)
    0x95, 0x20,        // ....REPORT_COUNT (# of buttons)
    0x81, 0x02,        // ....INPUT (Data,Var,Abs)
    //0x05, 0x08,  // ....USAGE_PAGE (LEDs) (msp uses *buttons* as outputs.  huh. )  ###
    0x05, 0x09,        // ....USAGE_PAGE (Button, like msp)
    //0x09, 0x01,  // try this crazy thing i have no idea what it does.  "vendor usage?"
    0x19, 0x01,        // ....USAGE_MINIMUM (LED 1)
    0x29, 0x10,        // ....USAGE_MAXIMUM (LED 32) ... was 0x20
    0x15, 0x00,        // ....LOGICAL_MINIMUM (0)
    0x25, 0x01,        // ....LOGICAL_MAXIMUM (1) (msp has physical min/max here)
    0x75, 0x01,        // ....REPORT_SIZE (1) (bit)
    0x95, 0x10,        // ....REPORT_COUNT (# of buttons) ... was 0x20
    0x91, 0x02,        // ....OUTPUT (Data,Var,Abs)
    0xc0,              // ....END_COLLECTION (Physical) 
    0xc0               // END_COLLECTION
  };
//change "false" to true and move this to progmem.
  DynamicHIDSubDescriptor *node = new DynamicHIDSubDescriptor(customHidReportDescriptor, sizeof(customHidReportDescriptor), false);
  DynamicHID().AppendDescriptor(node);

  DynamicHID().prepareOutput((uint8_t *) & reportRx,  2);
}

void Joystick_::begin() {
  sendState();
}

void Joystick_::end() {
}

void Joystick_::setButton(uint8_t button, uint8_t value) {
  if (value == 0) {
    releaseButton(button);
  } else {
    pressButton(button);
  }
}

void Joystick_::pressButton(uint8_t button) {
  if (button >= 32) return;
  bitSet(reportTx.buttons, button);
}

void Joystick_::releaseButton(uint8_t button) {
  if (button >= 32) return;
  bitClear(reportTx.buttons, button);
}

//int Joystick_::read() {
 // // TODO this should always be 2
//  return DynamicHID().RecvReport((uint8_t *) &reportRx, 2);
//}

int setval(int32_t value, uint8_t dataLocation[]) {
  dataLocation[0] = (uint8_t)(value & 0x00FF);
  dataLocation[1] = (uint8_t)(value >> 8);
  return 2;
}

void Joystick_::sendState() {
  DynamicHID().SendReport(3, (const uint8_t *) &reportTx, 16);
}