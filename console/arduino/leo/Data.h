#ifndef DATA_H
#define DATA_H

/** 
 * Contains state suitable for direct USB I/O.
 *
 * This layout must match the HID Report Descriptor in Transceiver.
 */
class Data {
public:
  typedef struct {
    uint16_t x : 16;
    uint16_t y : 16;
    uint16_t z : 16;
    uint16_t rx : 16;
    uint16_t ry : 16;
    uint16_t rz : 16;
    bool b1 : 1;
    bool b2 : 1;
    bool b3 : 1;
    bool b4 : 1;
    bool b5 : 1;
    bool b6 : 1;
    bool b7 : 1;
    bool b8 : 1;
    bool b9 : 1;
    bool b10 : 1;
    bool b11 : 1;
    bool b12 : 1;
    bool b13 : 1;
    bool b14 : 1;
    bool b15 : 1;
    bool b16 : 1;
    bool b17 : 1;
    bool b18 : 1;
    bool b19 : 1;
    bool b20 : 1;
    bool b21 : 1;
    bool b22 : 1;
    bool b23 : 1;
    bool b24 : 1;
    bool b25 : 1;
    bool b26 : 1;
    bool b27 : 1;
    bool b28 : 1;
    bool b29 : 1;
    bool b30 : 1;
    bool b31 : 1;
    bool b32 : 1;
  } WorkingReportTx;
  volatile WorkingReportTx workingReportTx_;


  // 16 byte report
  typedef struct {
    uint16_t x : 16;
    uint16_t y : 16;
    uint16_t z : 16;
    uint16_t rx : 16;
    uint16_t ry : 16;
    uint16_t rz : 16;
    bool b1 : 1;
    bool b2 : 1;
    bool b3 : 1;
    bool b4 : 1;
    bool b5 : 1;
    bool b6 : 1;
    bool b7 : 1;
    bool b8 : 1;
    bool b9 : 1;
    bool b10 : 1;
    bool b11 : 1;
    bool b12 : 1;
    bool b13 : 1;
    bool b14 : 1;
    bool b15 : 1;
    bool b16 : 1;
  } ReportTx;
  volatile ReportTx reportTx_;


  /**
 * Represents the RoboRIO's 16 bits of output.  The RIO API implies that there are 32 bits, but
 * if you use a 32 bit HID report, the RIO produces nothing.
*/
  typedef struct {
    bool i1 : 1;
    bool i2 : 1;
    bool i3 : 1;
    bool i4 : 1;
    bool i5 : 1;
    bool i6 : 1;
    bool i7 : 1;
    bool i8 : 1;
    bool i9 : 1;
    bool i10 : 1;
    bool i11 : 1;
    bool i12 : 1;
    bool i13 : 1;
    bool i14 : 1;
    bool i15 : 1;
    bool i16 : 1;
  } ReportRx;
  volatile ReportRx reportRx_;
};

#endif  // DATA_H