# Freifunk Auto Connect App

&copy; 2015 [Tobias Trumm](mailto:tobiastrumm@uni-muenster.de) licensed under GPLv3  
Freifunk Logo: [freifunk.net](http://freifunk.net),  [CC BY-SA 3.0](https://creativecommons.org/licenses/by-sa/3.0/)

## Info
The Freifunk Auto Connect App makes it easier to add multiple Freifunk SSIDs to the network configuration of your Android device.

## How to add missing SSIDs
Please visit the [freifunk-ssids repository](https://github.com/WIStudent/freifunk-ssids) to find out how to submit missing SSIDs.

## Where does the position and online status data about the Freifunk nodes come from?
Currently a [python script](https://github.com/WIStudent/FreifunkNodeLocationCrawler) running on a Raspberry Pi at my home is crawling Freifunk API files every hour to get position and online status of Freifunk access points and writing the data into a json file. This json file is then uploaded to a webserver. When someone wants to know the nearest Freifunk access points around them and the local json file containing the position data is older than 65 min, the app will check if a newer file is available online.

## Build status
Build status on [Travis CI](https://travis-ci.org/):
[![Build Status](https://travis-ci.org/WIStudent/FreifunkAutoConnectApp.svg?branch=master)](https://travis-ci.org/WIStudent/FreifunkAutoConnectApp)

## Availability in stores

This app is already available at F-Droid:

[![Matekarte on F-Droid](https://chart.googleapis.com/chart?chs=150x150&cht=qr&chl=https://f-droid.org/app/com.example.tobiastrumm.freifunkautoconnect&choe=UTF-8)](https://f-droid.org/app/com.example.tobiastrumm.freifunkautoconnect)
