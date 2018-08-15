# GLT Companion

This is the schedule browser for the "[Grazer Linuxtage](https://linuxtage.at)" conference in Graz, Austria. 

It's a fork of [FOSDEM companion](https://github.com/cbeyls/fosdem-companion-android) by [Christophe Beyls](https://github.com/cbeyls).

You can install the app from the [Google Play Store](https://play.google.com/store/apps/details?id=at.linuxtage.companion) or directly from the [GLT website](https://linuxtage.at/downloads/app).

Donations to Grazer Linuxtage are possible via Bitcoin: [1GLTBBirbj8GZ8uY1gwovZ1QEMjfWu3rWT](bitcoin:1GLTBBirbj8GZ8uY1gwovZ1QEMjfWu3rWT)

## How to build

All dependencies are defined in ```app/build.gradle```. Import the project in Android Studio or use Gradle in command line:

```
./gradlew assembleRelease
```

The result apk file will be placed in ```app/build/outputs/apk/```.

## License

[Apache License, Version 2.0](http://www.apache.org/licenses/LICENSE-2.0)

## Used libraries

* [Android Support Library](http://developer.android.com/tools/support-library/) by The Android Open Source Project
* [ViewPagerIndicator](http://viewpagerindicator.com/) by Jake Wharton
* [PhotoView](https://github.com/chrisbanes/PhotoView) by Chris Banes

## Contributors

* Christophe Beyls
* Florian Klien
