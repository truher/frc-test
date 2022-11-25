#include "Data.h"
#include "Transceiver.h"
#include "Sensor.h"
#include "DacTone.h"

// see https://github.com/end2endzone/NonBlockingRTTTL
#include <NonBlockingRtttl.h>

// see https://github.com/sparkfun/SparkFun_TPA2016D2_Arduino_Library
#include <SparkFun_TPA2016D2_Arduino_Library.h>

// see https://github.com/adafruit/Adafruit_MCP4725
#include <Adafruit_MCP4725.h>

#define BUZZER_PIN 8
// for now just demonstrate the sound part.
// TODO: play useful sounds
const char * tetris = "tetris:d=4,o=5,b=160:e6,8b,8c6,8d6,16e6,16d6,8c6,8b,a,8a,8c6,e6,8d6,8c6,b,8b,8c6,d6,e6,c6,a,2a,8p,d6,8f6,a6,8g6,8f6,e6,8e6,8c6,e6,8d6,8c6,b,8b,8c6,d6,e6,c6,a,a";

TPA2016D2 amp;

Adafruit_MCP4725 dac;

ReportRx reportRx;  // transceiver writes received data here, indicator displays it.
ReportTx reportTx;  // sensor writes data here, transceiver sends it.
Transceiver transceiver(Transceiver::SubConsole::AUTOPILOT, reportRx);
Sensor sensor;

void setup() {
  pinMode(BUZZER_PIN, OUTPUT);

  sensor.initialize();

  Wire.begin();
  if (amp.begin() == false) {
    while (1)
      ;
  }
  // for now just turn it on
  // TODO: adjust gain with knob
  amp.enableRightSpeaker();
  amp.enableLeftSpeaker();
  amp.disableLimiter();
  amp.disableNoiseGate();
  amp.writeRelease(1);
  amp.writeAttack(1);

  amp.writeFixedGain(15);  // half

  dac.begin(0x62); // TODO: correct address
}

void loop() {
  if (rtttl::isPlaying()) {
    rtttl::play();
  } else {
    rtttl::begin(BUZZER_PIN, tetris);
  }
  sensor.sense(reportTx);
  transceiver.send(reportTx);
  sensor.indicate(reportRx);

  dac.setVoltage(0, false); // TODO: interesting output
}