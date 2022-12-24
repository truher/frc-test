// CAN Logger
//
// cribbed from
// ArduinoPcap/examples/esp32_pcap_sd
// SparkFun_CAN-Bus_Arduino_Library/examples/CAN_Read_Demo

#include <SPI.h>
#include <SdFat.h>

#include <Canbus.h>
#include <defaults.h>
#include <global.h>
#include <mcp2515.h>
#include <mcp2515_defs.h>

#include <PCAP.h>

#include <TimeLib.h>
#define SD_FAT_TYPE 1  // FAT16/FAT32

#define CHIP_SELECT 9
#define SAVE_INTERVAL 30
#define FILENAME "canbus"

unsigned long lastTime = 0;
unsigned long lastChannelChange = 0;
bool fileOpen = false;
int counter = 0;

PCAP pcap = PCAP();
SdFat32 sd;

/* opens a new file */
void openFile() {

  //searches for the next non-existent file name
  int c = 0;
  String filename = "/" + (String)FILENAME + ".pcap";
  while (sd.open(filename)) {
    filename = "/" + (String)FILENAME + "_" + (String)c + ".pcap";
    c++;
  }

  //set filename and open the file
  pcap.filename = filename;
  fileOpen = pcap.openFile(sd);

  //reset counter (counter for saving every X seconds)
  counter = 0;
}

void setup() {
  pinMode(CHIP_SELECT, OUTPUT);
  if (!sd.begin(CHIP_SELECT)) {
    // TODO: complain somehow, e.g. blink
    return;
  }
  if (!Canbus.init(CAN_1000KBPS)) {
    return;
  }

  openFile();
}

void loop() {
  unsigned long currentTime = millis();

  tCAN message;
  if (mcp2515_check_message()) {
    if (mcp2515_get_message(&message)) {
      uint32_t timestamp = now();                                                      //current timestamp
      uint32_t microseconds = (unsigned int)(micros() - millis() * 1000);              //micro seconds offset (0 - 999)
      pcap.newPacketSD(timestamp, microseconds, message.header.length, message.data);  //write packet to file
    }
  }

  if (fileOpen && currentTime - lastTime > 1000) {
    pcap.flushFile();        //save file
    lastTime = currentTime;  //update time
    counter++;               //add 1 to counter
  }

  /* when counter > 30s interval */
  if (fileOpen && counter > SAVE_INTERVAL) {
    pcap.closeFile();  //save & close the file
    fileOpen = false;  //update flag
    openFile();        //open new file
  }
}
