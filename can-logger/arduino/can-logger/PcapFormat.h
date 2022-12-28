#ifndef PCAPFORMAT_H
#define PCAPFORMAT_H

#include <TimeLib.h>

/*
 * PCAP Format
 *
 * Supplies buffers in pcap format, as defined by Wireshark.
 *
 * See https://wiki.wireshark.org/Development/LibpcapFileFormat
 * See https://docs.wpilib.org/en/stable/docs/software/can-devices/can-addressing.html
 */
class PcapFormat {
  struct GlobalHeader {
    uint32_t magic_number = 0xa1b2c3d4;
    uint16_t version_major = 2;
    uint16_t version_minor = 4;
    int32_t thiszone = 0;
    uint32_t sigfigs = 0;
    // max length of captured packets, in octets
    // TODO: make this reasonable
    uint32_t snaplen = 65535;
    // CAN 2.0b (29-bit), see libpcap/pcap/dlt.h:782
    // (Roborio CAN is 29-bit)
    uint32_t network = 190;
  };

  static const GlobalHeader GLOBAL_HEADER;

  struct RecordHeader {
    uint32_t ts_sec;
    uint32_t ts_usec;
    uint32_t incl_len;
    uint32_t orig_len;
  };

  RecordHeader recordHeader_;

  /*
   * Returns the (static) global header as a 24-byte buffer.
   */
  static uint8_t* getGlobalHeader() {
    return (const void*)&GLOBAL_HEADER;
  }

  /*
  * Returns a record header as a 16-byte buffer.
  * Note there's just one of these, so don't hang on to it.
  *
  * data_len: bytes in the packet
  */
  uint8_t* newRecordHeader(uint32_t data_len) {
    uint32_t timestamp = now();
    uint32_t microseconds = (unsigned int)(micros() - millis() * 1000);
    recordHeader_ = { timestamp, microseconds, data_len, data_len };
    return (const void*)&recordHeader_;
  }
};

#endif  // PCAPFORMAT_H
