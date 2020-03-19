/*
 * WaterS --------------------------------------------------------------------------------------
     SigFox Configuration on MKRFox1200
*/


#include <OneWire.h>
#include <DallasTemperature.h>
#include <SigFox.h>
#include <ArduinoLowPower.h>
#include <TinyGPS++.h>

#define ONE_WIRE_BUS 5
#define SIGFOX_FRAME_LENGTH 12

#define SENDING_FRAME_INTERVAL_MS 1000
#define LOW_POWER_MODE_INTERVAL_MS 3585000

#define PACKET_SENS_FLAG_VALUE 0
#define PACKET_GPS_FLAG_VALUE 1
#define VERBOSE_MODE_OFF 0
#define VERBOSE_MODE_ON 1

#define DEBUG 0

OneWire oneWire(ONE_WIRE_BUS);
DallasTemperature sensors(&oneWire);
TinyGPSPlus gps;

int ph_pin = A3; //This is the pin number connected to Po - PH value

typedef struct __attribute__ ((packed)) sigfox_message_one {   //struct of sigfox's package sensors data.
  float temperatureWater; 
  int16_t phValue;
  int16_t turbidity; 
  int8_t flag;
} SigfoxMessageSens;


typedef struct __attribute__ ((packed)) sigfox_message_two {   //struct of sigfox's package position data.
  
  float latitude;
  float longitude;
  int8_t flag;
  
} SigfoxMessagePos;


SigfoxMessageSens frameSens;   //frame that will be send to sigfox's backend
SigfoxMessagePos framePos;   //frame that will be send to sigfox's backend
uint8_t verboseValue;


void setup() {
  
  Serial.begin(9600);
  Serial1.begin(9600);  //RX serial for gps transmission
    
  if (!SigFox.begin())
  {
    Serial.println("Shield error or not present!");
    return;
  }
  
  SigFox.debug();

  String version = SigFox.SigVersion();
  String ID = SigFox.ID();
  String PAC = SigFox.PAC();

  /* Display module informations */
  Serial.println("MKRFox1200 Sigfox configuration");
  Serial.println("SigFox FW version " + version);
  Serial.println("ID  = " + ID);
  Serial.println("PAC = " + PAC);

  Serial.println("");
  
  /* Outdoor Temperature */  
  Serial.print("Module temperature: ");
  Serial.println(SigFox.internalTemperature());
  Serial.println("");

  /* Set the verbose mode ON */
  verboseValue = VERBOSE_MODE_ON;

  delay(100);

  /* Send the module to the deepest sleep */
  SigFox.end();

}

void loop()
{  
  /* ------------------ GPS Check ------------------ */

  while (Serial1.available())  
  {
    if (gps.encode(Serial1.read()))
    {
      startScanning();
    }
  }
}

void startScanning()
{

  /* ------------------ VERBOSE CHECK  ------------------ */

  if(verboseValue == VERBOSE_MODE_ON)
  {
    verbodeMode();
  }
    
  
  /* ------------------ FRAME FILLING ------------------ */
  
  frameSens.flag = PACKET_SENS_FLAG_VALUE;
  framePos.flag = PACKET_GPS_FLAG_VALUE;

  frameSens.temperatureWater = sensors.getTempCByIndex(0); 
  frameSens.turbidity = (analogRead(A1) * (5.0 / 1024.0)); //conversion to volt range[0-5]
  frameSens.phValue = Po;
   
  framePos.latitude = gps.location.lat(); 
  framePos.longitude = gps.location.lng();
   
   
  /* ------------------ FRAME SENDING ------------------ */
  
  sendFrameAndGetResponse(frameSens);
  
  delay(SENDING_FRAME_INTERVAL_MS);
  
  if(gps.location.lat() != 0.0 && gps.location.lng() != 0.0)
  {
    sendFramePosAndGetResponse(framePos);
  }
     
  delay(LOW_POWER_MODE_INTERVAL_MS);

}

void verboseMode()
{
  /* ------------------ GPS DisplayInfo ------------------ */
  
  Serial.println(" ---GPS info--- ");
  
  if (gps.location.isValid())
  {
    Serial.print("Latitude: ");
    Serial.println(gps.location.lat(), 6);
    Serial.print("Longitude: ");
    Serial.println(gps.location.lng(), 6); 
    Serial.println(""); 
  }
  else
  {
    Serial.print(F("INVALID"));
    Serial.println("");
    Serial.println("");
  }

  /* ------------------ PH DisplayInfo ------------------ */
 
  Serial.println(" --- PH info--- ");
  int measure = analogRead(ph_pin);
  Serial.print("Measure: ");
  Serial.print(measure);

  double voltage = 5 / 1024.0 * measure; //classic digital to voltage conversion
  Serial.print("\tVoltage: ");
  Serial.print(voltage, 3);


  float Po = 15 + ((2.5 - voltage) / 0.18);
  Serial.print("\tPH: ");
  Serial.print(Po, 3);
  Serial.println("");
  Serial.println("");
      
  /* ------------------ WATER TEMPERATURE DisplayInfo ------------------ */
  
  Serial.println("--- WATER temperature info---");
  sensors.requestTemperatures();
  Serial.print("temp: ");
  String waterTemp = String(sensors.getTempCByIndex(0)); 
  Serial.println(sensors.getTempCByIndex(0)); 
  Serial.println("");

  /* ------------------ TURBIDITY DisplayInfo ------------------ */
  
  String sensorValue = String(analogRead(A1));   
  Serial.println("--- TURBIDITY info---");
  Serial.print("turb: ");
  Serial.println((analogRead(A1) * (5.0 / 1024.0)));
  Serial.println("");

}

void sendFrameAndGetResponse(SigfoxMessageSens frame) 
{
  Serial.println("Sending messages to the server ...");
  
  // Start the module
  SigFox.begin();
  
  // Wait at least 100 ms after first configuration
  delay(100);
  
  // Clears all pending interrupts
  SigFox.status();
  delay(1);

  SigFox.beginPacket();
  SigFox.write((uint8_t*)&frame,12);

  int ret = SigFox.endPacket(true);  // send buffer to SIGFOX network and wait for a response
  
  if (ret > 0) 
  {
    Serial.println("No transmission");
  } else 
  {
    Serial.println("Transmission ok");
  }

  Serial.println(SigFox.status(SIGFOX));
  Serial.println(SigFox.status(ATMEL));

  if (SigFox.parsePacket()) 
  {
    Serial.println("Response from server:");
    
    while (SigFox.available()) 
    {
      Serial.print("0x");
      Serial.println(SigFox.read(), HEX);
    }
  } 
  else 
  {
    Serial.println("Could not get any response from the server");
    Serial.println("Check the SigFox coverage in your area");
    Serial.println("If you are indoor, check the 20dB coverage or move near a window");
  }
  
  Serial.println();
  SigFox.end();
}

void sendFramePosAndGetResponse(SigfoxMessagePos frame) 
{
  Serial.println("Sending messages to the server ...");
  
  // Start the module
  SigFox.begin();
  
  // Wait at least 100 ms after first configuration
  delay(100);
  
  // Clears all pending interrupts
  SigFox.status();
  
  delay(1);

  SigFox.beginPacket();
  SigFox.write((uint8_t*)&frame,12);

  int ret = SigFox.endPacket(true);  // send buffer to SIGFOX network and wait for a response
  
  if (ret > 0) 
  {
    Serial.println("No transmission");
  } 
  else
  {
    Serial.println("Transmission ok");
  }

  Serial.println(SigFox.status(SIGFOX));
  Serial.println(SigFox.status(ATMEL));

  if (SigFox.parsePacket())
  {
    Serial.println("Response from server:");
    
    while (SigFox.available())
    {
      Serial.print("0x");
      Serial.println(SigFox.read(), HEX);
    }
  } 
  else 
  {
    Serial.println("Could not get any response from the server");
    Serial.println("Check the SigFox coverage in your area");
    Serial.println("If you are indoor, check the 20dB coverage or move near a window");
  }
  
  Serial.println();
  SigFox.end();
}
