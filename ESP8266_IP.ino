#include <ESP8266WiFi.h>
#include <ESP8266WebServer.h>

const char* ssid = "EDELWEISS KOS";
const char* password = "edelweiss_kost_ump";

ESP8266WebServer server(80);

const int relayPin = 5; 
bool lampuStatus = false;

void setup() {
  Serial.begin(115200);
  pinMode(relayPin, OUTPUT);
  digitalWrite(relayPin, HIGH); 
  
  WiFi.begin(ssid, password);
  Serial.println("Menghubungkan ke WiFi...");
  
  while (WiFi.status() != WL_CONNECTED) {
    delay(500);
    Serial.print(".");
  }
  
  Serial.println("");
  Serial.println("WiFi terhubung");
  Serial.print("IP Address: ");
  Serial.println(WiFi.localIP());

  server.on("/", HTTP_GET, handleRoot);
  server.on("/on", HTTP_GET, handleOn);
  server.on("/off", HTTP_GET, handleOff);
  server.on("/status", HTTP_GET, handleStatus);
  
  server.begin();
  Serial.println("HTTP server berjalan");
}

void loop() {
  server.handleClient();
}

void handleRoot() {
  String html = "<!DOCTYPE html><html><head>";
  html += "<meta name='viewport' content='width=device-width, initial-scale=1'>";
  html += "<style>body{font-family:Arial; text-align:center; margin-top:50px;}";
  html += "button{padding:15px 32px; font-size:16px; margin:4px 2px; cursor:pointer;}";
  html += ".on{background-color:#4CAF50; color:white;}";
  html += ".off{background-color:#f44336; color:white;}</style></head>";
  html += "<body><h1>Kontrol Lampu ESP8266</h1>";
  html += "<p>Status Lampu: <strong>" + String(lampuStatus ? "HIDUP" : "MATI") + "</strong></p>";
  html += "<p><a href='/on'><button class='on'>HIDUPKAN</button></a></p>";
  html += "<p><a href='/off'><button class='off'>MATIKAN</button></a></p>";
  html += "</body></html>";
  
  server.send(200, "text/html", html);
}

void handleOn() {
  digitalWrite(relayPin, LOW);
  lampuStatus = true;
  server.send(200, "application/json", "{\"status\":\"on\"}");
}

void handleOff() {
  digitalWrite(relayPin, HIGH);
  lampuStatus = false;
  server.send(200, "application/json", "{\"status\":\"off\"}");
}

void handleStatus() {
  server.send(200, "application/json", lampuStatus ? "{\"status\":1}" : "{\"status\":0}");
}