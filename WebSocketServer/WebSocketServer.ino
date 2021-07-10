#include <ESP8266WiFi.h>
#include <ESP8266WiFiMulti.h>
#include <WebSocketsServer.h>
#include <ESP8266WebServer.h>

const int LEDPIN = D4; 
const char ssid[] = "REPLACE_WITH_SSID";
const char password[] = "REPLACE_WITH_PASSWORD";
const String WiFiHostname = "WebSocketServer";

ESP8266WebServer server(80); //port 80
WebSocketsServer webSocket = WebSocketsServer(81); //port 81

void webSocketEvent(uint8_t num, WStype_t type, uint8_t * payload, size_t length) {
  switch (type) {
    case WStype_DISCONNECTED:
      Serial.printf("Client %d was disconnected!\r\n", num);
      break;
    case WStype_CONNECTED:
      {
        IPAddress ip = webSocket.remoteIP(num);
        Serial.printf("Client %d (%d.%d.%d.%d) was connected\r\n", num, ip[0], ip[1], ip[2], ip[3]);

        String message;

        message = "Hi, client " + String(num) + "!";
        webSocket.sendTXT(num, message );

        message = "This is WebSocketServer on " + String(WiFi.localIP().toString()) + "!";
        webSocket.sendTXT(num, message );
        
      }
      break;
    case WStype_TEXT:
      webSocket.sendTXT(num, payload); //this will echo the message (payload) to the client it came from (the num parameter contains the id of the client which triggered the action)
      Serial.printf("Recieved data \"%s\" was sent back to client %d!\r\n", (const char*)(payload), num); 
      //webSocket.broadcastTXT(payload); //This can be used instead of the sendTXT() method to broadcast the recieved message to all connected clients
      break;
    case WStype_PING:
      digitalWrite(LEDPIN, 0); //this will blink the onboard led when revieving a ping
      delay(100); //delay(); is not recommendet in the webSocketEvent() method, but always worked for me
      digitalWrite(LEDPIN, 1);
      break;
    case WStype_PONG:
      Serial.printf("Client %d is available!\r\n", num); //this is always triggered after connecting because the websocketserver sends a ping while connecting -> gets a pong from the client
      break;
    default:
      Serial.printf("Invalid WebSocket: %d\r\n", type);
      break;
  }
}

void handleRoot() {
  server.send(200, "text/html", "<!DOCTYPE html><html><head><title>WebSocketServer</title><style>body {font-family: sans-serif;color: #444;background-color: #BDBDBD;text-align: center;}</style></head><body><p>Looks like everything's running smoothly. <br>Download the WebSocketClient App for Android <a href=\"https://github.com/DeeBuggerTech/002_WebSocket_Test\" target=\"_blank\">here</a> and enjoy messing around with the system.</p></body></html>");
}

void setup() {
  pinMode(LEDPIN, OUTPUT);
  digitalWrite(LEDPIN, 0);
  Serial.begin(115200);
  delay(200);
  Serial.println();
  Serial.println();
  Serial.println();
  Serial.println();

  Serial.println("Setup:");
  
  WiFi.mode(WIFI_STA); 
  WiFi.hostname(WiFiHostname);
  WiFi.begin(ssid, password);
  Serial.print("  Connecting to WiFi..");
  while (WiFi.status() != WL_CONNECTED) {
    delay(500);
    Serial.print('.');
  }
  
  Serial.println();
  Serial.print("  Connected to ");
  Serial.print(WiFi.SSID());              
  Serial.print(" with IP ");
  Serial.print(WiFi.localIP());
  Serial.println(" and Hostname \""+WiFiHostname+"\"!");

  Serial.print("  Setting up HTTP webserver...");
  server.on("/", handleRoot);
  server.begin();
  Serial.println("  Done!");

  Serial.print("  Setting up WebSocketServer...");
  webSocket.disableHeartbeat();
  webSocket.begin();
  webSocket.onEvent(webSocketEvent);
  Serial.println("  Done!");
  
  Serial.println("Setup finished!");

  Serial.println();
  Serial.println();

  digitalWrite(LEDPIN, 1);
}

void loop() {
  webSocket.loop();
  server.handleClient();
}
