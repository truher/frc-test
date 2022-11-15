#include "Data.h"
#include "Transceiver.h"
#include "Sensor.h"

ReportRx reportRx_;  // transceiver writes received data here, indicator displays it.
ReportTx reportTx_;  // sensor writes data here, transceiver sends it.
Transceiver transceiver_(Transceiver::SubConsole::PILOT, reportRx_);
Sensor sensor_;

void setup() {
  Serial.begin(115200);
while (! Serial) delay(10);
  Serial.println("init");
  sensor_.initialize();
  Serial.println("init done");
  sensor_.splash();
 pinMode(17, OUTPUT);
}


void loop() {
  // Serial.println("loop");
  // delay(100);
  //   digitalWrite(17, HIGH);   // turn the LED on (HIGH is the voltage level)
  // delay(1000);                       // wait for a second
  // digitalWrite(17, LOW);    // turn the LED off by making the voltage LOW
  // delay(1000); 
  sensor_.sense(reportTx_);
  transceiver_.send(reportTx_);
  ///////////////////////
  // for this demo we loop back.
  // TODO: take this out, do it in the RIO.
  ///////////////////////
 // reportRx_ = *(ReportRx*)((char*)(&reportTx_) + 16);
  ///////////////////////
  // TODO: remove the above line
  ///////////////////////
 // sensor_.indicate(reportRx_);
}