#include <Arduino.h>
#include "WiFiManager.h"
#include "SButton.h"
#include "WiFiClientSecure.h"
#include <WebSocketsClient.h>
#include <ESPmDNS.h>
#include <ArduinoJson.h>

#define BUTTON_PIN 0
#define USE_SERIAL Serial
#define SERVICE "_dev_nordix_yt"
#define PROTOCOL "tcp"
#define WS_PATH "/ws"

#define SINGLE_CLICK "dev.nordix.yt.domain.model.ButtonAction.ButtonClick"
#define DOUBLE_CLICK "dev.nordix.yt.domain.model.ButtonAction.ButtonDoubleClick"
#define LONG_CLICK "dev.nordix.yt.domain.model.ButtonAction.ButtonLongClick"
#define LED_ON "dev.nordix.yt.domain.model.ButtonCommand.LedOn" //46 chars
#define LED_OFF "dev.nordix.yt.domain.model.ButtonCommand.LedOff" //47 chars
 
int led = 15;
int min_brightness = 32;
int brightness = min_brightness;
int fadeAmount = 5;
bool toggleState = false;
bool serviceFound = false;
bool disconnectedBlinkState = false;

unsigned long previousMillisBrowse = 0;
unsigned long previousMillisAnalog = 0;

const long interval = 30;
const long long_interval_multiplier = 50;

IPAddress host;
int port = 0;
String hostname;
WebSocketsClient webSocket;

void webSocketEvent(WStype_t type, uint8_t * payload, size_t length) {
  JsonDocument docc;
	switch(type) {
		case WStype_DISCONNECTED:
			USE_SERIAL.printf("[WSc] Disconnected!\n");
      serviceFound = false;
			break;
		case WStype_CONNECTED:
			USE_SERIAL.printf("[WSc] Connected to url: %s\n", payload);
			break;
		case WStype_TEXT:
      deserializeJson(docc, payload);
			USE_SERIAL.printf("[WSc] get text: %s\n", payload);
      if (strncmp((const char *)docc["type"], LED_OFF, 47)) {
        toggleState = false;
        analogWrite(led, min_brightness);
      }
      if (strncmp((const char *)docc["type"], LED_ON, 46)) {
        toggleState = true;
      }
			break;
		case WStype_BIN:
			USE_SERIAL.printf("[WSc] get binary length: %u\n", length);
			break;
		case WStype_ERROR:			
		case WStype_FRAGMENT_TEXT_START:
		case WStype_FRAGMENT_BIN_START:
		case WStype_FRAGMENT:
		case WStype_FRAGMENT_FIN:
			break;
	}

}

void browseService(const char *service, const char *proto) {
  brightness = min_brightness;
  analogWrite(led, brightness);
  USE_SERIAL.printf("Browsing for service _%s._%s.local. ... ", service, proto);
  int n = MDNS.queryService(service, proto);
  if (n == 0) {
    serviceFound = false;
    USE_SERIAL.println("no services found");
  } else {
    serviceFound = true;
    USE_SERIAL.print(n);
    USE_SERIAL.println(" service(s) found");
    for (int i = 0; i < n; ++i) {
      // Print details for each service found
      USE_SERIAL.print("  ");
      USE_SERIAL.print(i + 1);
      USE_SERIAL.print(": ");
      USE_SERIAL.print(MDNS.hostname(i));
      USE_SERIAL.print(" (");
      USE_SERIAL.print(MDNS.address(i));
      USE_SERIAL.print(":");
      USE_SERIAL.print(MDNS.port(i));
      USE_SERIAL.println(")");
    }
  }
  host = MDNS.address(0);
  port = MDNS.port(0);
  USE_SERIAL.printf("setting up service connection at %s \n", host.toString());

  if (serviceFound) {
    webSocket.beginSSL(host.toString(), port, WS_PATH);
    webSocket.onEvent(webSocketEvent);
    // webSocket.setAuthorization("user", "Password");
    webSocket.setReconnectInterval(5000);
  }
}

Sbutton btn (BUTTON_PIN, 0);

void setup() {
    WiFi.mode(WIFI_STA);
    USE_SERIAL.begin(115200);
	  USE_SERIAL.setDebugOutput(true);
    
    pinMode(led, OUTPUT);

    WiFiManager wm;
    // wm.resetSettings();
    bool res;
    USE_SERIAL.println("initializing AP");
    res = wm.autoConnect("Button Connect");
    USE_SERIAL.println("Waiting for setup");
 
    if(!res) {
        USE_SERIAL.println("Failed to connect, restarting");
    } 
    else {
        USE_SERIAL.println("connected:)");
        hostname = WiFi.getHostname();
    }

  if (!MDNS.begin(hostname)) {
    Serial.println("Error setting up MDNS responder!");
    while(1) {
      delay(1000);
    }
  } 
}

void ledTick() {
  unsigned long currentMillis = millis();

  if (serviceFound) {
    if (!toggleState) {
      if (currentMillis - previousMillisAnalog >= interval) {
        previousMillisAnalog = currentMillis;
        analogWrite(led, brightness);

        brightness = brightness + fadeAmount;
        if (brightness > 255) brightness = 255;
        if (brightness < min_brightness) brightness = min_brightness;
        if (brightness <= min_brightness || brightness >= 255) {
          fadeAmount = -fadeAmount;
        }
      }
    } else if (currentMillis - previousMillisAnalog >= interval * long_interval_multiplier) {
      previousMillisAnalog = currentMillis;
      if (brightness == min_brightness) {
        brightness = 0xFF;
      } else {
        brightness = min_brightness;
      }
      analogWrite(led, brightness);
    }
  } else if(currentMillis - previousMillisAnalog >= interval*5) {
      previousMillisAnalog = currentMillis;
      disconnectedBlinkState = !disconnectedBlinkState;
      if (disconnectedBlinkState) {
        analogWrite(led, 0xFF);
      } else {
        analogWrite(led, 0x00);
      }
  }
}

void browseTick() {
  unsigned long currentMillis = millis();
  if (!serviceFound && currentMillis - previousMillisBrowse >= 5000) {
    previousMillisBrowse = currentMillis;
    browseService(SERVICE, PROTOCOL);
  }
}

void loop() {
  webSocket.loop();
  ledTick();
  browseTick();
  btn.tick();
  
  if (btn.hasClicks()) {
    JsonDocument doc;
    String buf;

    if (btn.hasSingle()) {
      doc["type"] = SINGLE_CLICK;
    } else if (btn.hasDouble()) {
      doc["type"] = DOUBLE_CLICK;
    } else if (btn.isHeld()) {
      doc["type"] = LONG_CLICK;
    }

    serializeJson(doc, buf);
    webSocket.sendTXT(buf);
  }

}
