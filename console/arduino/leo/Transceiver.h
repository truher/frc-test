#ifndef TRANSCEIVER_H
#define TRANSCEIVER_H

#include <stdint.h>
#include <Arduino.h>
#include "PluggableUSB.h"
#include "Data.h"

/**
 * HID Report Descriptor
 *
 * This layout must match the struct in Data.
 * TODO: add reportid?
 */
// this is the one that works:
// TODO note that 0x81 input includes a bit for "wrap"
static const uint8_t HIDReportDescriptor[] = {
  0x05, 0x01,                    // Usage Page: Generic Desktop Controls (0x01)
  0x09, 0x04,                    // Usage: Joystick (0x04)
  0xa1, 0x01,                    // Collection type: Application (0x01)
                                 // Buttons
  0x05, 0x09,                    // ....Usage Page: Button (0x09)
  0x19, 0x01,                    // ....Usage minimum: 0x01
  0x29, 0x20,                    // ....Usage maximum: 0x20
  0x15, 0x00,                    // ....Logical minimum: 0
  0x25, 0x01,                    // ....Logical maximum: 1
  0x75, 0x01,                    // ....Report size: 1
  0x95, 0x20,                    // ....Report count: 32
  0x55, 0x00,                    // ....Unit exponent: 0 (TODO remove)
  0x65, 0x00,                    // ....Unit (0x00) (TDOO remove)
  0x81, 0x02,                    // ....Input (Data,Var,Abs)
  0x05, 0x01,                    // ....Usage Page: Generic Desktop Controls (0x01)
                                 // Joysticks
  0x09, 0x01,                    // ....Usage: Pointer (0x01)
  0x15, 0x00,                    // ....Logical minimum: 0
  0x27, 0xff, 0xff, 0x00, 0x00,  // Logical maximum: 65535 TODO fix this
  0x75, 0x10,                    // ....Report size: 16
  0x95, 0x06,                    // ....Report count: 6
  0xa1, 0x00,                    // ....Collection type: Physical (0x00)
  0x09, 0x30,                    // ........Usage: X (0x30)
  0x09, 0x31,                    // ........Usage: Y (0x31)
  0x09, 0x32,                    // ........Usage: Z (0x32)
  0x09, 0x33,                    // ........Usage: Rx (0x33)
  0x09, 0x34,                    // ........Usage: Ry (0x34)
  0x09, 0x35,                    // ........Usage: Rz (0x35)
  0x81, 0x02,                    // ........Input (Data,Var,Abs)
  0xc0,                          // ....End Collection (0xc) 
                                 // Outputs
  0xa1, 0x01,                    // ....Collection type: Application (0x01)
  0x05, 0x09,                    // ........Usage Page: Button (0x09)
  0x19, 0x01,                    // ........Usage minimum: 0x01
  0x29, 0x10,                    // ........Usage maximum: 16 (32 won't be populated)
  0x15, 0x00,                    // ........Logical minimum: 0
  0x25, 0x01,                    // ........Logical maximum: 1
  0x75, 0x01,                    // ........Report size: 1
  0x95, 0x10,                    // ........Report count: 16 (32 does not work)
  0x91, 0x02,                    // ........Output (Data,Var,Abs)
  0xc0,                          // ....End Collection (0xc)
  0xc0,                          // End Collection (0xc)

};



// TODO convert to using this one

static const uint8_t HIDReportDescriptor2[] = {
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
  0x26, 0xFF, 0xFF,  // ........LOGICAL_MAXIMUM (65535) (TODO make this match ADC)
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
  0x05, 0x09,  // ....USAGE_PAGE (Button, like msp)
  //0x09, 0x01,  // try this crazy thing i have no idea what it does.  "vendor usage?"
  0x19, 0x01,  // ....USAGE_MINIMUM (LED 1)
  0x29, 0x10,  // ....USAGE_MAXIMUM (LED 32) ... was 0x20
  0x15, 0x00,  // ....LOGICAL_MINIMUM (0)
  0x25, 0x01,  // ....LOGICAL_MAXIMUM (1) (msp has physical min/max here)
  0x75, 0x01,  // ....REPORT_SIZE (1) (bit)
  0x95, 0x10,  // ....REPORT_COUNT (# of buttons) ... was 0x20
  0x91, 0x02,  // ....OUTPUT (Data,Var,Abs)
  0xc0,        // ....END_COLLECTION (Physical)
  0xc0         // END_COLLECTION
};

/** Sends and receives data via USB. */
class Transceiver : public PluggableUSBModule {
  // class Transceiver {
public:

  Transceiver::Transceiver(Data &data)
    : PluggableUSBModule(1, 1, epType),  // epType, below
      data_(data),
      protocol(1),  // HID report
      idle(1) {
    epType[0] = 0xc1;  // EP_TYPE_INTERRUPT_IN (EPTYPE1 | EPTYPE0 | EPDIR)
    dataAvailable = 0;
    PluggableUSB().plug(this);
    send();  // send a baseline report
  }


  void Transceiver::send() {
    SendReport((const void *)&data_.workingReportTx_, sizeof(data_.workingReportTx_));
  }

  int Transceiver::SendReport(const void *data, int len) {
    USB_Send(pluggedEndpoint | TRANSFER_RELEASE, data, len);
  }

  // turn this off for now
  bool Transceiver::recv() {
    // todo actually compare new values to old values?
    if (dataAvailable) {
      dataAvailable = 0;
      return true;
    }
    return false;
  }

protected:
  Data &data_;
  int dataAvailable;

  // override
  bool Transceiver::setup(USBSetup &setup) {
    if (pluggedInterface != setup.wIndex) {
      return false;
    }

    uint8_t request = setup.bRequest;
    uint8_t requestType = setup.bmRequestType;

    if (requestType == 0xa1) {  //  DEVICETOHOST	0x80 | CLASS 0x20 | INTERFACE	0x01
      if (request == 0x01) {    // HID_GET_REPORT == ?
        // unsupported
        // TODO: HID_GetReport();
        return true;
      }
      if (request == 0x03) {  // HID_GET_PROTOCOL == ?
        // unsupported
        // TODO: Send8(protocol);
        return true;
      }
      if (request == 0x02) {  // HID_GET_IDLE == how often the device resends unchanged data
        // unsupported
        // TODO: Send8(idle);
        return false;
      }
    }

    if (requestType == 0x21) {  // HOSTTODEVICE 00 CLASS 20 INTERFACE 01
      if (request == 0x0B) {    // HID_SET_PROTOCOL
        // The USB Host tells us if we are in boot or report mode.
        // This only works with a real boot compatible device.
        // this doesn't actually do anything
        protocol = setup.wValueL;
        return true;
      }
      if (request == 0x0A) {  // HID_SET_IDLE == how often the device resends unchanged data
                              // this doesn't actually do anything
        idle = setup.wValueL;
        return true;
      }
      if (request == 0x09) {  // HID_SET_REPORT
        uint8_t reportType = setup.wValueH;
        int length = setup.wLength;
        // HID Request Type HID1.11 Page 51 7.2.1 Get_Report Request
        if (reportType == 0x03) {  // HID_REPORT_TYPE_FEATURE
          // unsupported
          return true;
        }
        if (reportType == 0x02) {  // HID_REPORT_TYPE_OUTPUT
          // accept an output report
          // writes from the end, thus this addition/subtraction to put the write in the right place.
          // of course i *think* it should only ever produce the correct length, how could it do anything else?
          if (length > sizeof(data_.reportRx_)) {
            length = sizeof(data_.reportRx_);
          }
          USB_RecvControl((uint8_t *)&(data_.reportRx_) + sizeof(data_.reportRx_) - length, length);
          dataAvailable = length;
          return true;
        }
      }
    }

    return false;
  }

  /**
  * Supplies the HID details to the control channel.
  * Uses pluggedInterface and pluggedEndpoint, which are
  * assigned in PluggableUSB().plug(), called in ctor.
  */
  // override
  int Transceiver::getInterface(uint8_t *interfaceCount) {
    *interfaceCount += 1;  // uses 1, this just tells the caller how many records to expect.

    const uint8_t interfaceDescriptor[] = {
      // INTERFACE DESCRIPTOR (2.0): class HID
      0x09,              // bLength: 9
      0x04,              // bDescriptorType: 0x04 (INTERFACE)
      pluggedInterface,  // bInterfaceNumber, 2 in the example  <== when is this initialized?
      0x00,              // bAlternateSetting: 0
      0x01,              // bNumEndpoints: 1
      0x03,              // bInterfaceClass: HID (0x03)
      0x00,              // bInterfaceSubClass: No Subclass (0x00)
      0x00,              // bInterfaceProtocol: 0x00
      0x00,              // iInterface: 0
      // HID DESCRIPTOR
      // this doesn't match the struct in HID.h (?)
      0x09,                                  // bLength: 9
      0x21,                                  // bDescriptorType: 0x21 (HID)
      0x01, 0x01,                            // bcdHID: 0x0101 (21)
      0x00,                                  // bCountryCode: Not Supported (0x00)
      0x01,                                  // bNumDescriptors: 1
      0x22,                                  // bDescriptorType: HID Report (0x22)
      lowByte(sizeof(HIDReportDescriptor)),  // wDescriptorLength: (144 in the example)
      highByte(sizeof(HIDReportDescriptor)),
      // ENDPOINT DESCRIPTOR
      0x07,                    // bLength: 7
      0x05,                    // bDescriptorType: 0x05 (ENDPOINT)
      pluggedEndpoint | 0x80,  //bEndpointAddress: (0x84  IN  Endpoint:4 in example)
      0x03,                    // bmAttributes: 0x03 (Transfertype: Interrupt-Transfer (0x3))
      0x40, 0x00,              // wMaxPacketSize: 64, (little-endian = L first)
      0x01,                    // bInterval: 1

    };
    return USB_SendControl(0, interfaceDescriptor, sizeof(interfaceDescriptor));
  }


  /**Returns bytes sent*/
  // override
  int Transceiver::getDescriptor(USBSetup &setup) {
    // Check if this is a HID Class Descriptor request
    if (setup.bmRequestType != 0x81) {  // DEVICETOHOST 80 | STANRDARD 00 | INTERFACE 01
      return 0;
    }
    if (setup.wValueH != 0x22) {  // HID_REPORT_DESCRIPTOR_TYPE
      return 0;
    }

    // In a HID Class Descriptor wIndex cointains the interface number
    if (setup.wIndex != pluggedInterface) {
      return 0;
    }

    int total = 0;
    int res = USB_SendControl(0, HIDReportDescriptor, sizeof(HIDReportDescriptor));
    if (res == -1)
      return -1;
    total += res;
    // Normal or bios protocol (Keyboard/Mouse) HID1.11 Page 54 7.2.5 Get_Protocol Request
    // "protocol" variable is used for this purpose.
    protocol = 0x01;  // HID_REPORT_PROTOCOL;
    return total;
  }


private:
  uint8_t epType[1];
  // TODO remove these
  uint8_t protocol;
  uint8_t idle;
};
#endif  // TRANSCEIVER_H