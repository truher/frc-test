//#include <FlexCAN.h>
#include <FlexCAN_T4.h>

static CAN_message_t txMsg;
static const uint8_t hex[17] = "0123456789abcdef";

FlexCAN_T4<CAN0, RX_SIZE_256, TX_SIZE_16> can0;

static void hexDump(uint8_t dumpLen, uint8_t *bytePtr) {
  uint8_t working;
  while (dumpLen--) {
    working = *bytePtr++;
    Serial.write(hex[working >> 4]);
    Serial.write(hex[working & 15]);
  }
  Serial.write('\r');
  Serial.write('\n');
}

void setup(void) {
  Serial.begin(9600);
  pinMode(LED_BUILTIN, OUTPUT);
  //pinMode(3, OUTPUT);
  delay(1000);
  Serial.println(F("CAN Test."));

  // Can0.begin();
  can0.begin();
  can0.setBaudRate(250000);

  //txMsg.ext = 0;
  txMsg.id = 0x100;
  txMsg.len = 8;
  txMsg.buf[0] = 10;
  txMsg.buf[1] = 20;
  txMsg.buf[2] = 0;
  txMsg.buf[3] = 100;
  txMsg.buf[4] = 128;
  txMsg.buf[5] = 64;
  txMsg.buf[6] = 32;
  txMsg.buf[7] = 16;
}

bool ledState = false;

void loop(void) {
  Serial.println("loop");
  CAN_message_t rxMsg;
  while (can0.read(rxMsg)) {
    Serial.print("CAN bus 0: ");
    hexDump(8, rxMsg.buf);
  }
  // while (Can0.available()) {
  //   Can0.read(rxMsg);
  //   Serial.print("CAN bus 0: ");
  //   hexDump(8, rxMsg.buf);
  // }
  Serial.println("done receiving");

  txMsg.buf[0]++;
  can0.write(txMsg);
  txMsg.buf[0]++;
  can0.write(txMsg);
  txMsg.buf[0]++;
  can0.write(txMsg);
  txMsg.buf[0]++;
  can0.write(txMsg);
  txMsg.buf[0]++;
  can0.write(txMsg);
  Serial.println("done transmitting");
  digitalWrite(LED_BUILTIN, ledState);
  ledState ^= 1;
  //delay(1000);
}