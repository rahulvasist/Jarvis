# Jarvis

This project is my attempt (successful) to develop a wifi connected power switch.
The basic high level block diagram is given below:

```
    ______________                      __________________             ___________       
   |              |                    |                  |           |           |
   |  Android App | ----- WiFi ------- |      ESP8266     | --------->|   Relay   |
   |______________|                    |__________________|           |___________|
  
```

## ESP8266
This is a Wifi enabled microcontroller. A GPIO pin from this controller is used to drive the relay.
The code for this is in `src/jarvis.c`. The code itself is very simple. It starts up a TCP server and waits for command to power on/off the GPIO port. The 'ON' command has been implemented with a kind of watchdog timer which needs to be patted every 10 mins. If a subsequent ON command is not received within that time, the GPIO port will be turned off. This feature allows me to leave the house without having to worry about switching my light off, as it will auto switch off in 10 mins :-)

## Android App
A simple anroid app to send commands to the ESP8266 TCP server
- Ability to manually send 'ON', 'OFF' commands with the press of a button
- Once a 'ON' command is sent, it keeps repeating this every 5 mins automatically until a 'OFF' command is sent. This way the light remains on without any interruption
- If the Wifi connects to my Home network, **AND** the sun has set, then automatically send the 'ON' command. With this feature, I can come home and my lights will go on automatically
