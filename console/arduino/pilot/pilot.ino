#include "Data.h"
#include "Transceiver.h"
#include "Sensor.h"

ReportRx reportRx_;  // transceiver writes received data here, indicator displays it.
ReportTx reportTx_;  // sensor writes data here, transceiver sends it.
Transceiver transceiver_(Transceiver::SubConsole::PILOT, reportRx_);
Sensor sensor_;

void setup() {
  sensor_.initialize();
}

/** Ignores outputs, there's nowhere to display them */
void loop() {
  sensor_.sense(reportTx_);
  transceiver_.send(reportTx_);
}