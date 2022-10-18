#ifndef DYNAMIC2_HID_h
#define DYNAMIC2_HID_h

#include <stdint.h>
#include <Arduino.h>
#include "PluggableUSB.h"

// DYNAMIC_HID 'Driver'
#define DYNAMIC_HID_GET_REPORT 0x01
#define DYNAMIC_HID_GET_IDLE 0x02
#define DYNAMIC_HID_GET_PROTOCOL 0x03
#define DYNAMIC_HID_SET_REPORT 0x09
#define DYNAMIC_HID_SET_IDLE 0x0A
#define DYNAMIC_HID_SET_PROTOCOL 0x0B

#define DYNAMIC_HID_DESCRIPTOR_TYPE 0x21
#define DYNAMIC_HID_REPORT_DESCRIPTOR_TYPE 0x22
#define DYNAMIC_HID_PHYSICAL_DESCRIPTOR_TYPE 0x23

// HID subclass HID1.11 Page 8 4.2 Subclass
#define DYNAMIC_HID_SUBCLASS_NONE 0
#define DYNAMIC_HID_SUBCLASS_BOOT_INTERFACE 1

// HID Keyboard/Mouse bios compatible protocols HID1.11 Page 9 4.3 Protocols
#define DYNAMIC_HID_PROTOCOL_NONE 0

// Normal or bios protocol (Keyboard/Mouse) HID1.11 Page 54 7.2.5 Get_Protocol Request
// "protocol" variable is used for this purpose.
#define DYNAMIC_HID_BOOT_PROTOCOL 0
#define DYNAMIC_HID_REPORT_PROTOCOL 1

// HID Request Type HID1.11 Page 51 7.2.1 Get_Report Request
#define DYNAMIC_HID_REPORT_TYPE_INPUT 1
#define DYNAMIC_HID_REPORT_TYPE_OUTPUT 2
#define DYNAMIC_HID_REPORT_TYPE_FEATURE 3

typedef struct
{
  uint8_t len;    // 9
  uint8_t dtype;  // 0x21
  uint8_t addr;
  uint8_t versionL;  // 0x101
  uint8_t versionH;  // 0x101
  uint8_t country;
  uint8_t desctype;  // 0x22 report
  uint8_t descLenL;
  uint8_t descLenH;
} DYNAMIC_HIDDescDescriptor;

typedef struct
{
  InterfaceDescriptor hid;
  DYNAMIC_HIDDescDescriptor desc;
  EndpointDescriptor in;
 // EndpointDescriptor out;  // new!
} DYNAMIC_HIDDescriptor;

class DynamicHIDSubDescriptor {
public:
  DynamicHIDSubDescriptor* next = NULL;
  DynamicHIDSubDescriptor(const void* d, const uint16_t l, const bool ipm = true)
    : data(d), length(l), inProgMem(ipm) {}

  const void* data;
  const uint16_t length;
  const bool inProgMem;
};

class DynamicHID_ : public PluggableUSBModule {
public:
  DynamicHID_(void);
  int begin(void);
  int SendReport(uint8_t id, const void* data, int len);
  void AppendDescriptor(DynamicHIDSubDescriptor* node);
  int available();
  int read();
 // //return bytes read
  //int RecvReport(uint8_t* outbuf, uint8_t len);

  void prepareOutput(void * report, int length) {
    if (length > 0) {
      data = (uint8_t*)report;
      dataLength = length;
      dataAvailable = 0;
    }
  }

protected:
  int getInterface(uint8_t* interfaceCount);
  int getDescriptor(USBSetup& setup);
  bool setup(USBSetup& setup);
  uint8_t getShortName(char* name);

  int dataLength;
  int dataAvailable;
  uint8_t* data;

private:
// ## NEW
  //uint8_t epType[1]; // AH IT IS NOT ONE
    uint8_t epType[2]; // AH IT IS NOT ONE
  DynamicHIDSubDescriptor* rootNode;
  uint16_t descriptorSize;
  uint8_t protocol;
  uint8_t idle;
};

// Replacement for global singleton.
// This function prevents static-initialization-order-fiasco
// https://isocpp.org/wiki/faq/ctors#static-init-order-on-first-use
DynamicHID_& DynamicHID();

#define D_HIDREPORT(length) \
  { 9, 0x21, 0x01, 0x01, 0, 1, 0x22, lowByte(length), highByte(length) }

#endif  // DYNAMIC2_HID_h