/*
 * CAN SENDER
 *
 * Send messages for the logger to hear.
 */
#include <FlexCAN_T4.h>
static CAN_message_t txMsg;
FlexCAN_T4<CAN0, RX_SIZE_256, TX_SIZE_16> can0;

void setup(void) {
  Serial.begin(9600);
  delay(1000);
  Serial.println("*************************** CAN SENDER ***************************");
  can0.begin();
  can0.setBaudRate(1000000);  // roborio speed is 1mbps
  can0.disableFIFO();
  can0.mailboxStatus();
  txMsg.id = 0x100;
  txMsg.len = 8;
  txMsg.seq = false;
  txMsg.buf[0] = 10;
  txMsg.buf[1] = 20;
  txMsg.buf[2] = 0;
  txMsg.buf[3] = 100;
  txMsg.buf[4] = 128;
  txMsg.buf[5] = 64;
  txMsg.buf[6] = 32;
  txMsg.buf[7] = 16;
}

void loop(void) {
  can0.events();                   // empty the tx queue a bit
  if (can0.getTXQueueCount() > 0)  // wait until it's empty
    return;
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
}
