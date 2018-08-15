# Berlin-Vegan Guide
A vegan guide to Berlin/Germany. This app runs on Android 4.x and up.

##master
[![Build Status](https://travis-ci.org/Berlin-Vegan/berlin-vegan-guide.svg?branch=master)](https://travis-ci.org/Berlin-Vegan/berlin-vegan-guide)

##development
[![Build Status](https://travis-ci.org/Berlin-Vegan/berlin-vegan-guide.svg?branch=development)](https://travis-ci.org/Berlin-Vegan/berlin-vegan-guide)
#build & install

```
./gradlew installDebug
```

#test
```
./gradlew test
```

# release creation

* check out master, if not already happened, and make sure you are up-to-date, e.g. `git fetch --all`, `git pull`, etc.
* merge branch development:
```
git merge --ff-only origin/development
```
* set annotated git tag for version, e.g.
```
TAG=0.1 && git tag -a ${TAG} -m ${TAG}
```
* build the release:
```
./gradlew assembleRelease
```
* sign the release, e.g.
```
jarsigner -verbose -sigalg SHA1withRSA -digestalg SHA1 -keystore my-release-key.keystore ./app/build/outputs/apk/app-release-unsigned.apk alias_name
```
* give the app a better name, e.g.
```
mv ./app/build/outputs/apk/app-release-unsigned.apk ./app/build/outputs/apk/org.berlin_vegan.app.apk
```
* install the release:
```
adb install -r ./app/build/outputs/apk/org.berlin_vegan.app.apk
```
* check is everything is fine and especially check the version string under 'About'
* if everything is fine, push to upstream:
```
git push origin master --tags
```
* switch to development branch and increase versionCode in "app/build.gradle"
