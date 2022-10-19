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
 * Configuring the 9th axis ("wheel") confuses the DS, so skip it.
 *
 * See hut1_3_0.pdf section 4 for details
 */
static const uint8_t HIDReportDescriptor[] = {
  0x05, 0x01,        // Usage Page: Generic Desktop Controls (0x01)
  0x09, 0x04,        // Usage: Joystick (0x04)
  0xa1, 0x01,        // Collection type: Application (0x01)
                     // Joysticks
  0x05, 0x01,        // ....Usage Page: Generic Desktop Controls (0x01)
  0x09, 0x01,        // ....Usage: Pointer (0x01)v
  0x16, 0x00, 0x80,  // ....Logical minimum: -32768
  0x26, 0xff, 0x7f,  // ....Logical maximum: 32767
  0x75, 0x10,        // ....Report size: 16
  0x95, 0x08,        // ....Report count: 8
  0xa1, 0x00,        // ....Collection type: Physical (0x00)
  0x09, 0x30,        // ........Usage: X (0x30)
  0x09, 0x31,        // ........Usage: Y (0x31)
  0x09, 0x32,        // ........Usage: Z (0x32)
  0x09, 0x33,        // ........Usage: Rx (0x33)
  0x09, 0x34,        // ........Usage: Ry (0x34)
  0x09, 0x35,        // ........Usage: Rz (0x35)
  0x09, 0x36,        // ........Usage: Slider (0x36)
  0x09, 0x37,        // ........Usage: Dial (0x37)
  0x81, 0x02,        // ........Input (Data,Var,Abs)
  0xc0,              // ....End Collection (0xc)
                     // Buttons
  0x05, 0x09,        // ....Usage Page: Button (0x09)
  0x19, 0x01,        // ....Usage minimum: 0x01
  0x29, 0x20,        // ....Usage maximum: 0x20
  0x15, 0x00,        // ....Logical minimum: 0
  0x25, 0x01,        // ....Logical maximum: 1
  0x75, 0x01,        // ....Report size: 1
  0x95, 0x20,        // ....Report count: 32
  0x81, 0x02,        // ....Input (Data,Var,Abs)
                     // Outputs (the DS appears to support 32 bits but only works with 16.)
  0x05, 0x08,        // ....Usage Page: LED (0x08)
  0x19, 0x01,        // ....Usage minimum: 0x01
  0x29, 0x10,        // ....Usage maximum: 16 (32 won't be populated)
  0x15, 0x00,        // ....Logical minimum: 0
  0x25, 0x01,        // ....Logical maximum: 1
  0x75, 0x01,        // ....Report size: 1
  0x95, 0x10,        // ....Report count: 16 (32 does not work)
  0x91, 0x02,        // ....Output (Data,Var,Abs)
  0xc0,              // End Collection (0xc)
};
static const int descLen = sizeof(HIDReportDescriptor);

/**
 * Sends and receives data via USB.
*/
class Transceiver : public PluggableUSBModule {
public:
  Transceiver::Transceiver(Data &data)
    : PluggableUSBModule(1, 1, epType), data_(data) {
    epType[0] = 0xc1;  // endpoint type = interrupt in
    dataAvailable = 0;
    PluggableUSB().plug(this);
    send();  // send a baseline report (of zeroes)
  }

  /** 
   * Sends the data as a HID Report.
   */
  void Transceiver::send() {
    SendReport((const void *)&data_.reportTx_, sizeof(data_.reportTx_));
  }

  /**
   * Checks to see if the data has been updated.
   *
   * TODO: actually compare new values to old values?
   */
  bool Transceiver::recv() {
    if (dataAvailable) {
      dataAvailable = 0;
      return true;
    }
    return false;
  }

protected:
  Data &data_;
  int dataAvailable;

  /**
  * Handles USB Class-specific requests using the Default pipe.
  *
  * Ignores all DeviceToHost requests (GetReport, GetIdle, GetProtocol).
  * Only the Interrupt In pipe is used for device-to-host messages.
  *
  * Ignores all HostToDevice requests (e.g. SetIdle, SetProtocol, SetFeature) except
  * for one, SetReport, which contains output data.  This method is intended for
  * higher-latency outputs compared to the Interrupt Out pipe, which is not used by the DS.
  *
  * Returns true if the request is handled, false if ignored.  Maybe that produces
  * a NAK?
  *
  * See hid1_1.pdf section 7.2 for details.
  */
  bool Transceiver::setup(USBSetup &setup) {
    if (setup.bmRequestType == 0x21) {             // request type = host to device
      if (setup.bRequest == 0x09) {                // request = SET_REPORT
        if (setup.wValueH == 0x02) {               // report type = OUTPUT
          if (setup.wIndex == pluggedInterface) {  // The message is addressed to this interface.
            int length = setup.wLength;
            // Writes from the end, thus this addition/subtraction to put the write in the right place.
            // I *think* it should only ever produce the correct length, how could it do anything else?
            if (length > sizeof(data_.reportRx_)) {
              length = sizeof(data_.reportRx_);
            }
            USB_RecvControl((uint8_t *)&(data_.reportRx_) + sizeof(data_.reportRx_) - length, length);
            dataAvailable = length;
            return true;
          }
        }
      }
    }
    // Returning false indicates we're not listening, doesn't seem to hurt anything.
    return false;
  }

  /**
   * Supplies the HID details to the control channel.
   *
   * Uses pluggedInterface and pluggedEndpoint, which are
   * assigned in PluggableUSB().plug(), called in ctor.
   */
  int Transceiver::getInterface(uint8_t *interfaceCount) {
    *interfaceCount += 1;

    const uint8_t interfaceDescriptor[] = {
      // INTERFACE DESCRIPTOR (2.0): class HID
      0x09,              // bLength: 9
      0x04,              // bDescriptorType: 0x04 (INTERFACE)
      pluggedInterface,  // bInterfaceNumber
      0x00,              // bAlternateSetting: 0
      0x01,              // bNumEndpoints: 1
      0x03,              // bInterfaceClass: HID (0x03)
      0x00,              // bInterfaceSubClass: No Subclass (0x00)
      0x00,              // bInterfaceProtocol: 0x00
      0x00,              // iInterface: 0
      // HID DESCRIPTOR
      // TODO: why this doesn't match the struct in HID.h?
      0x09,              // bLength: 9
      0x21,              // bDescriptorType: 0x21 (HID)
      0x01, 0x01,        // bcdHID: 0x0101 (21)
      0x00,              // bCountryCode: Not Supported (0x00)
      0x01,              // bNumDescriptors: 1
      0x22,              // bDescriptorType: HID Report (0x22)
      lowByte(descLen),  // wDescriptorLength
      highByte(descLen),
      // ENDPOINT DESCRIPTOR
      0x07,                    // bLength: 7
      0x05,                    // bDescriptorType: 0x05 (ENDPOINT)
      pluggedEndpoint | 0x80,  // bEndpointAddress
      0x03,                    // bmAttributes: 0x03 (Transfertype: Interrupt-Transfer (0x3))
      0x40, 0x00,              // wMaxPacketSize: 64
      0x01,                    // bInterval: 1

    };
    return USB_SendControl(0, interfaceDescriptor, sizeof(interfaceDescriptor));
  }

  /**
   * Handles GetDescriptor requests.
   *
   * Returns the number of bytes sent.
   *
   * See hid1_1.pdf section 7.1 for details.
   */
  int Transceiver::getDescriptor(USBSetup &setup) {
    if (setup.bmRequestType == 0x81) {           // request type = HID class descriptor
      if (setup.wValueH == 0x22) {               // descriptor type = report
        if (setup.wIndex == pluggedInterface) {  // The message is addressed to this interface.
          int total = 0;
          int res = USB_SendControl(0, HIDReportDescriptor, descLen);
          if (res == -1)
            return -1;
          total += res;
          return total;
        }
      }
    }
    return 0;
  }

private:
  uint8_t epType[1];

  int Transceiver::SendReport(const void *data, int len) {
    USB_Send(pluggedEndpoint | TRANSFER_RELEASE, data, len);
  }
};
#endif  // TRANSCEIVER_H