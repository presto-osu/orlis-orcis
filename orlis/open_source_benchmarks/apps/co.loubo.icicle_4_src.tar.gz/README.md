##About
 ** A functional Freenet node is required to use this app.

Use Icicle For Freenet to connect to your Freenet node from your Android device, allowing you to:
- View the status of your node (uptime, bandwidth),
- View uploads and downloads, pause, and change priority,
- View connections to peers,
- Uploads files from your device to Freenet,
- Share your Node Reference with friends.

##Configure Freenet:

Icicle For Freenet uses FCPv2 to connect to your Freenet node. FCP has to be enabled and configured to get the most use out of this app:

* Switch to Advanced Mode
* Go to Configuration -> FCP,
* Make sure "Enable FCP?" is set to TRUE,
* Under "IP address to bind to", add the IP address of the computer running Freenet,
(For Example: 127.0.0.1,0:0:0:0:0:0:0:1,192.168.1.1)
* Under "Allowed hosts", add the IP address or IP address range of your Android device,
(For Example: 127.0.0.1,0:0:0:0:0:0:0:1,192.168.1.0/255.255.255.0)
* In order to enable more advanced functionality (more node statistics,peer connection information, and node reference sharing):
* Under "Hosts allowed full access", enter the same IP address information as "Allowed hosts" above.

* **Note**: If you do not add your Android device's IP address in "Hosts allowed full access," you will still be able to view your downloads and uploads, as well as upload files from your Android device.

##Permissions:

**android.permission.ACCESS_NETWORK_STATE:** Required to determine whether you're on Mobile or WiFi, and prevent trying to connect while on Mobile (configurable).
**android.permission.INTERNET:** Required to connect to the Freenet Node.
**android.permission.NFC:** Required to share Node References over NFC.
**android.permission.READ_EXTERNAL_STORAGE** and
**android.permission.WRITE_EXTERNAL_STORAGE:** Required to write your Node Reference temporarily to external storage when sharing via another app (such as email)

##Links

[![Get it on Google Play](https://developer.android.com/images/brand/en_generic_rgb_wo_60.png)](https://play.google.com/store/apps/details?id=co.loubo.icicle)

[Developer Website](http://loubo.co/icicle)
