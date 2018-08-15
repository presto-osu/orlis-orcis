
<h1>Classic Monitor</h1>

<h3>Available on Google Play.</h3>

<p>
https://play.google.com/store/apps/details?id=ca.farrelltonsolar.classic
</p>

<p>Note: version 2+ is targeted to Android API level 14 (Android 4.0) and above, all features are available on API 17 (Android 4.2) and above</p>

![Alt text](https://farrelltonsolar.files.wordpress.com/2015/03/soc.png)

<p>
Classic Monitor is a free status monitor for Midnite solar 's, Classic 150, 200, 250 Charge Controller (www.midniteSolar.com). It is a Read Only Program, it does not write to the Classic.
The software is provided "AS IS", WITHOUT WARRANTY OF ANY KIND, express or implied.
Classic Monitor is NOT a product of Midnite solar, nor do they support this application!
</p>
<p>
If the app detects a Whizbang Junior current monitor, the State Of Charge will display along with a bi-directional current gauge.
Basic support for the Tristar MPPT charge controller from Morningstar is also included.

</p>

<p>
If you find issues or want new features, please open an issue on the github tracker at https://github.com/graham22/Classic/issues
</p>

<p>
Online help: http://www.skyetracker.com/classicmonitor/help_en.html
</p>


## License
```

 Copyright (c) 2014. FarrelltonSolar

  Licensed under the Apache License, Version 2.0 (the "License");
  you may not use this file except in compliance with the License.
  You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

  Unless required by applicable law or agreed to in writing, software
  distributed under the License is distributed on an "AS IS" BASIS,
  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  See the License for the specific language governing permissions and
  limitations under the License.

```


Release notes:

-----------------

version 2.4.0
<ul>
<li>Added Live Chart Tab.</li>
<li>Added Share Screen Capture.</li>
<li>Added option to disable 'System View' to support multiple independent charge controllers on separate battery banks.</li>
<li>Added support for multiple PVOutput Systems when System View is disabled.</li>
</ul>

-----------------


version 2.3.0
<ul>
<li>Added Online Help.</li>
<li>Added link to PVOutput.</li>
</ul>

-----------------

version 2.2.0
<ul>
<li>Added support for multiple classics, the new 'System' tab will display the power harvested and consumed by the system when multiple classics are in use. The WhizbangJr is required to enable this feature.</li>
<li>The Energy and Calendar views can now display system/controller values with the selection of a radio button.</li>
</ul>
-----------------

version 2.1.0

<ul>
<li>Added support for URI for remote access</li>
<li>Added capacity page to display more WhizbangJr information</li>
<li>Added German localization resources.</li>
</ul>

-----------------

version 2.0.5

<ul>
<li>Added Fahrenheit scale option</li>
</ul>

-----------------

version 2.0

<ul>
<li>Transfer to code to Github</li>
<li>Added support for multiple classics listed in a slide out navigation drawer.</li>
<li>Re-design of classic detection using UDP broadcasts.</li>
<li>Added temperature gauges.</li>
<li>Implemented calendar, day & hour chart using native code rather than using webview component.</li>
<li>Added info and message tabs.</li>
<li>Implemented new Android sliding tab view pager.</li>
<li>Implemented vertical pager for calendar view.</li>
<li>Re-design gauge component to support latest Android 5.0 API 21.</li>
<li>Added auto scale feature to gauges.</li>
<li>Updated PVOutput uploader to support multiple site IDs.</li>
<li>Implemented Android Services for Modbus, UPDListener and PVOutput uploader.</li>
<li>Updated French, Italian, Spanish localization resources.</li>
</ul>
-----------------

version 1.7

Prepare for deployment to google play


-----------------

version 1.6

Added french localization resources

-----------------

version 1.5

Added chart view.
Implemented upload to PVOutput.org.

-----------------

version 1.4

Added data feed to calendar view.
Close TCP socket when app is minimized, re-open when app is resumed.

-----------------

version 1.3

Refactores code to use BroadcastIntents.
Modbus Master now running as an Android IntentService.
Added Custom modbus read for File transfer.
Gauge scaled by touching the gauge when unlocked.
Added SOC% on main page
Added 'Countertop' view for SOC%, activated by touching SOC% on main page
Added Webview for PVOUtputs and Calendar (data feeds not complete)
Added Aux LEDs to power gauge

-----------------

version 1.2

Added basic support for WhizBangJr, Battery Gauge is bi-directional if whizbangJr is detected.
Added placeholder for WhizbangJr AH data

-----------------

version 1.1

Added tabbed views.
Added Placeholder for Calendar and Chart pages
Fixed J2Mod IsConnected, no longer sending extra blank byte over TCP.

-----------------

version 0.2

Fixed crash at first startup.
Default scan range when no gateway detected.
catch J2MOD Exception and reconnect.
Added support for API level 10+
Added Gauge scale settings 
Added IP Scan range
Now uses J2ModLite.
