/*
 * CAN LOGGER
 *
 * Log received messages.  This seems to be able to read 800kbps.
 *
 * TODO: write to SD.
 */
#include "FlexCAN_T4.h"
#include "SdFat.h"
#include "RingBuf.h"

// Use Teensy SDIO
#define SD_CONFIG  SdioConfig(FIFO_SDIO)

// Size to log 10 byte lines at 25 kHz for more than ten minutes.
#define LOG_FILE_SIZE 10*20000*600  // 150,000,000 bytes.

// Space to hold more than 800 ms of data for 10 byte lines at 25 ksps.
#define RING_BUF_CAPACITY 400*512
#define LOG_FILENAME "SdioLogger.csv"  // TODO: unique filenames

bool ledState = false;
FlexCAN_T4<CAN0, RX_SIZE_256, TX_SIZE_16> can0;
int packets = 0;

SdFs sd;
FsFile file;

// RingBuf for File type FsFile.
RingBuf<FsFile, RING_BUF_CAPACITY> rb;

void setup(void) {
  Serial.begin(9600);
  pinMode(LED_BUILTIN, OUTPUT);
  delay(1000);
  Serial.println(F("*************************** CAN LOGGER ***************************"));
  can0.begin();
  can0.setBaudRate(1000000); // roborio speed is 1mbps
  can0.disableFIFO();
  can0.onReceive(canSniff);
  can0.enableMBInterrupts();
  can0.mailboxStatus();
}

void loop(void) {
  can0.events();
}

void canSniff(const CAN_message_t &msg) {
  packets++;
  if (packets % 10 != 0)  // too much printing overflows the rx buffer
    return;
  Serial.printf("ct %d buf %d MB %d  OVERRUN: %d  LEN: %d EXT: %d TS: %d ID: %x",
                packets, can0.getRXQueueCount(), msg.mb, msg.flags.overrun, msg.len,
                msg.flags.extended, msg.timestamp, msg.id);
  Serial.print(" Buffer: ");
  for (uint8_t i = 0; i < msg.len; i++) {
    Serial.printf("%x ", msg.buf[i]);
  }
  Serial.println();
  digitalWrite(LED_BUILTIN, ledState);
  ledState ^= 1;
}