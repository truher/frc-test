#include "Data.h"
#include "Transceiver.h"
#include "Sensor.h"

ReportRx reportRx;  // transceiver writes received data here, indicator displays it.
ReportTx reportTx;  // sensor writes data here, transceiver sends it.
Transceiver transceiver(Transceiver::SubConsole::AUTOPILOT, reportRx);
Sensor sensor;

void setup() {
  sensor.initialize();
}

void loop() {
  sensor.sense(reportTx);
  transceiver.send(reportTx);
  ///////////////////////
  // for this demo we loop back.
  // TODO: take this out, do it in the RIO.
  ///////////////////////
  reportRx = *(ReportRx*)((char*)(&reportTx) + 16);
  ///////////////////////
  // TODO: remove the above line
  ///////////////////////
  sensor.indicate(reportRx);
}