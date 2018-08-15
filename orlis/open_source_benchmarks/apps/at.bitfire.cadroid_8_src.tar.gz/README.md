
### CAdroid

CAdroid is intended to assist you in importing your own CA or self-signed certificate
to your Android device.


### Download

* [Download APK directly from Github](https://github.com/bitfireAT/cadroid/releases/latest)
* [Download from F-Droid](https://f-droid.org/app/at.bitfire.cadroid)
* [Download from Amazon App Store](http://www.amazon.com/bitfire-web-engineering-CAdroid-Certificates/dp/B00P2S3ALG)
* [Download from Google Play](https://play.google.com/store/apps/details?id=at.bitfire.cadroid)

        
### How does it work?
 
After you enter the host name of your HTTPS site, the certificate chain will be
fetched and you can select the root certificate to import. Possible import issues
will be detected and displayed.

As soon as you have verified the certificate by its details and fingerprints,
it will be exported into a .crt file. You can then simply import the certificate from the file with a few clicks.

When the certificate was imported correctly, it will be accepted in (nearly) all
apps, including [DAVdroid](https://davdroid.bitfire.at) and the Android email app
([don't choose "Accept all certificates" there](https://code.google.com/p/android/issues/detail?id=67038)).


### Donations

As this software is free and open-source software, we're asking for donations.
Please see http://davdroid.bitfire.at/donate for details.

        
### Further info

* [CAdroid home page](http://cadroid.bitfire.at)
* [Github page](https://github.com/bitfireAT/cadroid) (source code, issues)
* [SSL server test](https://www.ssllabs.com/ssltest/): check your server configuration in case of errors
 
Follow us on Twitter: [@davdroidapp](http://twitter.com/davdroidapp)
 
Copyright (c) 2013 – 2015 Ricki Hirner ([bitfire web engineering](http://www.bitfire.at)). All rights reserved.
This program and the accompanying materials are made available under the terms of the GNU Public License v3.0 which accompanies this distribution, and is available at [http://www.gnu.org/licenses/gpl.html](http://www.gnu.org/licenses/gpl.html).
