# oxybeats_app

\simple App made to read parameters of biometric sensor and calculate statistics

Android app made for an elective subject called "Application Development on Mobile Devices" of the Electronic Engineering Carrer on Universidad Tecnológica Nacional Facultad Regional Buenos Aires (UTN-FRBA). 

This app communicates through Bluetooth (RN4871) to a SAMG55J19 board which contains a MAX30102 sensor, giving the measures of cardiac frequency, oxygen saturation and amount of hours sleept from the user. These measurements are given every half-minute, set by the board, and are viewwd through graphics by the user. The user information is saved on a Firebase Database, same as the measurements. All was done in Java programming language.

I developed another app, DocApp, which goal is to combine with "oxybeats" app, since this one is used by the common user, and DocApp is used by the doctors of these users. 

For this project, I used different external libraries:
- Firebase Authenthicator
- Firebase Database
- MPAndroidChart (https://github.com/PhilJay/MPAndroidChart)
- Green Robot's EventBus (https://github.com/greenrobot/EventBus)
- MldpBluetooth (given by Microchip, cause of RN4871)
