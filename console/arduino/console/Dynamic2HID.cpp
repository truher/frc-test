#include "Dynamic2HID.h"

DynamicHID_& DynamicHID() {
  static DynamicHID_ obj;
  return obj;
}

int DynamicHID_::getInterface(uint8_t* interfaceCount) {
  *interfaceCount += 1;  // uses 1
  DYNAMIC_HIDDescriptor hidInterface = {
    D_INTERFACE(pluggedInterface, 1, USB_DEVICE_CLASS_HUMAN_INTERFACE, DYNAMIC_HID_SUBCLASS_NONE, DYNAMIC_HID_PROTOCOL_NONE),
  //   D_INTERFACE(pluggedInterface, 2, USB_DEVICE_CLASS_HUMAN_INTERFACE, DYNAMIC_HID_SUBCLASS_NONE, DYNAMIC_HID_PROTOCOL_NONE),

    D_HIDREPORT(descriptorSize),
    D_ENDPOINT(USB_ENDPOINT_IN(pluggedEndpoint), USB_ENDPOINT_TYPE_INTERRUPT, USB_EP_SIZE, 10)//,
    // # new!
  //  D_ENDPOINT(USB_ENDPOINT_OUT(pluggedEndpoint+1), USB_ENDPOINT_TYPE_INTERRUPT, USB_EP_SIZE, 10)
  };
  return USB_SendControl(0, &hidInterface, sizeof(hidInterface));
}

int DynamicHID_::getDescriptor(USBSetup& setup) {
  // Check if this is a HID Class Descriptor request
  if (setup.bmRequestType != REQUEST_DEVICETOHOST_STANDARD_INTERFACE) { return 0; }
  if (setup.wValueH != DYNAMIC_HID_REPORT_DESCRIPTOR_TYPE) { return 0; }

  // In a HID Class Descriptor wIndex cointains the interface number
  if (setup.wIndex != pluggedInterface) { return 0; }

// TODO REMOVE THIS there's only ever one descriptor
  int total = 0;
  DynamicHIDSubDescriptor* node;
  for (node = rootNode; node; node = node->next) {
    int res = USB_SendControl((node->inProgMem ? TRANSFER_PGM : 0), node->data, node->length);
    if (res == -1)
      return -1;
    total += res;
  }

  // Reset the protocol on reenumeration. Normally the host should not assume the state of the protocol
  // due to the USB specs, but Windows and Linux just assumes its in report mode.
  protocol = DYNAMIC_HID_REPORT_PROTOCOL;

  return total;
}

uint8_t DynamicHID_::getShortName(char* name) {
  name[0] = 'H';
  name[1] = 'I';
  name[2] = 'D';
  name[3] = 'A' + (descriptorSize & 0x0F);
  name[4] = 'A' + ((descriptorSize >> 4) & 0x0F);
  return 5;
}

void DynamicHID_::AppendDescriptor(DynamicHIDSubDescriptor* node) {
  if (!rootNode) {
    rootNode = node;
  } else {
    DynamicHIDSubDescriptor* current = rootNode;
    while (current->next) {
      current = current->next;
    }
    current->next = node;
  }
  descriptorSize += node->length;
}

int DynamicHID_::available() {
  if (dataAvailable < 0) {
    return 0;
  }
  return dataAvailable;
}

int DynamicHID_::read() {
  if (dataAvailable <=0) {
    return -1;
  }
  return data[dataLength - dataAvailable--];
}

int DynamicHID_::SendReport(uint8_t id, const void* data, int len) {
  uint8_t p[len + 1];
  p[0] = id;
  memcpy(&p[1], data, len);
  int sent =  USB_Send(pluggedEndpoint | TRANSFER_RELEASE, p, len + 1);
  return sent;
}

//int DynamicHID_::RecvReport(uint8_t* outbuf, uint8_t len) {
 // if (len > 64) len = 64; // max len is 64
 // int bytesAvailable = USB_Available(pluggedEndpoint + 1);
 // if (bytesAvailable == 0) return 0;
 // //Serial.println("bytes available");
  ////Serial.println(bytesAvailable, DEC);  // TODO REMOVE

//  uint8_t buf[len + 1] = {}; // zeros
//  int lenRead = USB_Recv(pluggedEndpoint + 1, buf, len + 1);

  //Serial.println("lenRead");
  //Serial.println(lenRead, DEC);  // TODO REMOVE
  //Serial.println("buf");
  //Serial.println((uint16_t) *buf, DEC);  // TODO REMOVE

//  if (lenRead == 0) return 0;

//  if (lenRead > len) lenRead = len;

 // memcpy(outbuf, buf+1, lenRead);
 // //Serial.println("out");
 // //Serial.println((uint16_t) *outbuf, DEC);  // TODO REMOVE
 // return lenRead;
//}

bool DynamicHID_::setup(USBSetup& setup) {
  if (pluggedInterface != setup.wIndex) {
    return false;
  }

  uint8_t request = setup.bRequest;
  uint8_t requestType = setup.bmRequestType;

  if (requestType == REQUEST_DEVICETOHOST_CLASS_INTERFACE) {
    if (request == DYNAMIC_HID_GET_REPORT) {
      // TODO: DYNAMIC_HID_GetReport();
      return true;
    }
    if (request == DYNAMIC_HID_GET_PROTOCOL) {
      // TODO: Send8(protocol);
      return true;
    }
    if (request == DYNAMIC_HID_GET_IDLE) {
      // TODO: Send8(idle);
    }
  }

  if (requestType == REQUEST_HOSTTODEVICE_CLASS_INTERFACE) {
    if (request == DYNAMIC_HID_SET_PROTOCOL) {
      // The USB Host tells us if we are in boot or report mode.
      // This only works with a real boot compatible device.
      protocol = setup.wValueL;
      return true;
    }
    if (request == DYNAMIC_HID_SET_IDLE) {
      idle = setup.wValueL;
      return true;
    }
    if (request == DYNAMIC_HID_SET_REPORT) {
 Serial.println("holy shit");
      uint8_t reportType = setup.wValueH;
      int length = setup.wLength;
      if (reportType == DYNAMIC_HID_REPORT_TYPE_FEATURE) {
        return true; // not this
      }
      if (reportType == DYNAMIC_HID_REPORT_TYPE_OUTPUT) {
        Serial.println("holy shit");
        USB_RecvControl(data + dataLength - length, length);
        dataAvailable = length;
        return true;
      }
      //uint8_t reportID = setup.wValueL;
      //uint16_t length = setup.wLength;
      //uint8_t data[length];
      // Make sure to not read more data than USB_EP_SIZE.
      // You can read multiple times through a loop.
      // The first byte (may!) contain the reportID on a multreport.
      //USB_RecvControl(data, length);
    }
  }

  return false;
}
DynamicHID_::DynamicHID_(void) : PluggableUSBModule(1, 1, epType),
   // DynamicHID_::DynamicHID_(void) : PluggableUSBModule(2, 1, epType),
    rootNode(NULL), descriptorSize(0),
    protocol(DYNAMIC_HID_REPORT_PROTOCOL), idle(1) {
  epType[0] = EP_TYPE_INTERRUPT_IN; // maybe bulk?
  // ## new!
 // epType[1] = EP_TYPE_INTERRUPT_OUT;
  PluggableUSB().plug(this);
  
}

int DynamicHID_::begin(void) {

  return 0;
}