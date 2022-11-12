#include "Data.h"
#include "Transceiver.h"
#include "Sensor.h"

ReportRx reportRx_; // transceiver writes received data here, indicator displays it.
ReportTx reportTx_; // sensor writes data here, transceiver sends it.
Sensor sensor_;
Transceiver transceiver_(Transceiver::SubConsole::PILOT, reportRx_);

void setup() {
  sensor_.initialize();
  sensor_.splash();
  transceiver_.send(reportTx_);  // send a baseline report (of zeroes)
}

void loop() {
  sensor_.sense(reportTx_);
  transceiver_.send(reportTx_);
  ///////////////////////
  // for this demo we loop back.  take this out, do it in the RIO.
  ///////////////////////
  reportRx_ = *(ReportRx*)((char*)(&reportTx_) + 16);
  ///////////////////////
  // remove the above line
  ///////////////////////
  sensor_.indicate(reportRx_);
}