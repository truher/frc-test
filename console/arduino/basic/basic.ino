#include "Adafruit_NeoKey_1x4.h"
#include "seesaw_neopixel.h"

Adafruit_NeoKey_1x4 neokey;  // Create the NeoKey object

void setup() {
  Serial.begin(115200);
  while (! Serial) delay(10);
   
  if (! neokey.begin(0x30)) {     // begin with I2C address, default is 0x30
    Serial.println("Could not start NeoKey, check wiring?");
    while(1) delay(10);
  }
  
  Serial.println("NeoKey started!");

  // Pulse all the LEDs on to show we're working
  for (uint16_t i=0; i<neokey.pixels.numPixels(); i++) {
    neokey.pixels.setPixelColor(i, 0x808080); // make each LED white
    neokey.pixels.show();
    delay(50);
  }
  for (uint16_t i=0; i<neokey.pixels.numPixels(); i++) {
    neokey.pixels.setPixelColor(i, 0x000000);
    neokey.pixels.show();
    delay(50);
  }
}

void loop() {
  uint8_t buttons = neokey.read();

  // Check each button, if pressed, light the matching neopixel
  
  if (buttons & (1<<0)) {
    neokey.pixels.setPixelColor(0, 0xFF0000); // red
  } else {
    neokey.pixels.setPixelColor(0, 0);
  }

  if (buttons & (1<<1)) {
    neokey.pixels.setPixelColor(1, 0xFFFF00); // yellow
  } else {
    neokey.pixels.setPixelColor(1, 0);
  }
  
  if (buttons & (1<<2)) {
    neokey.pixels.setPixelColor(2, 0x00FF00); // green
  } else {
    neokey.pixels.setPixelColor(2, 0);
  }

  if (buttons & (1<<3)) {
    neokey.pixels.setPixelColor(3, 0x00FFFF); // blue
  } else {
    neokey.pixels.setPixelColor(3, 0);
  }  

  neokey.pixels.show();
}
