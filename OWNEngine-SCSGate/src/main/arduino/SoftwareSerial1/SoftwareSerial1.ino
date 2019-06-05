/*
  Software serial multple serial test
 
 Receives from the hardware serial, sends to software serial.
 Receives from software serial, sends to hardware serial.
 
 The circuit: 
 * RX is digital pin 10 (connect to TX of other device)
 * TX is digital pin 11 (connect to RX of other device)
 
 Note:
 Not all pins on the Mega and Mega 2560 support change interrupts, 
 so only the following can be used for RX: 
 10, 11, 12, 13, 50, 51, 52, 53, 62, 63, 64, 65, 66, 67, 68, 69
 
 Not all pins on the Leonardo support change interrupts, 
 so only the following can be used for RX: 
 8, 9, 10, 11, 14 (MISO), 15 (SCK), 16 (MOSI).
 
 created back in the mists of time
 modified 25 May 2012
 by Tom Igoe
 based on Mikal Hart's example
 
 This example code is in the public domain.
 
 */
#include <SoftwareSerial.h>

byte s, m;
byte strSerial[16];
byte strMySerial[255];

SoftwareSerial mySerial(10, 11); // RX, TX

void setup()  
{
  pinMode(13, OUTPUT); // 13 LED

  // Open serial communications and wait for port to open:
  Serial.begin(115200);
  while (!Serial) {
    ; // wait for serial port to connect. Needed for Leonardo only
  }
  digitalWrite(13, HIGH);    // turn the LED on

  Serial.println("Started!");

  // set the data rate for the SoftwareSerial port
  mySerial.begin(115200);
  mySerial.write("@s");  // set SCSgate/KNXgate slow speed
  delay(100);            // wait 100ms
  mySerial.flush();
  mySerial.end();
  Serial.flush();
  
  // reopen serial port at slow speed
  mySerial.begin(38400);
  mySerial.write("@5");   // setup for 5 volts power supply
  delay(5);
  //mySerial.write("@MA");  // ascii mode
  mySerial.write("@MX");  // bynary mode
  delay(5);
  mySerial.write("@l");   // continuous log request
  delay(5);
  
  mySerial.write("@q");   // query request
  digitalWrite(13, LOW);  // turn the LED off 
}

void loop() // run over and over
{

// USB -> BUS
// ========================================================================================
  m = 0;
  while (mySerial.available())
  {
    strMySerial[m++] = mySerial.read();     // receive from KNXgate/SCSgate
    delayMicroseconds(100);
    digitalWrite(13, HIGH);    // turn the LED on
  }
  s = 0;
  while (m)
  {
    Serial.write(strMySerial[s++]);        // write on serial USB
    m--;
  }
// ========================================================================================

// BUS -> USB
// ========================================================================================
  s = 0;
  while (Serial.available())
  {
    strSerial[s++] = Serial.read();        // receive from serial USB
    delayMicroseconds(100);
    digitalWrite(13, HIGH);    // turn the LED on
  }
  m = 0;
  while (s)
  {
    Serial.write(strSerial[m]);            // write back - local echo
    mySerial.write(strSerial[m++]);        // write on serial KNXgate/SCSgate 
    s--;
    delayMicroseconds(100);
  }
// ========================================================================================

  digitalWrite(13, LOW);    // turn the LED oFF

}
