# Changelog
All notable changes to this project will be documented in this file. Format inspired by http://keepachangelog.com/ and this example https://github.com/olivierlacan/keep-a-changelog/blob/master/CHANGELOG.md

## [1.3] - Unreleased

## [1.2] - 2016-07-08 (actually still unreleased, there is a problem with the F-Droid build I need to fix)

### Added
 - Initial dialog explaining how to uninstall the app and warning about false battery statistics

### Removed
 - Logging options (it always logs to logcat now)
 - Write to external storage permission

## [1.1] - 2016-06-25

### Fixed
 - Issue while turning to landscape if the proximity sensor was covered just before. Now the turning off of screen is canceled.
 - If device admin rights are manually removed, the lock screen setting will turn off automatically and WaveUp will not try to lock the device.

### Changed
 - Listens for proximity sensor immediately after turning screen on.

## [1.0] - 2016-06-08

### Changed
 - Reduced the time it vibrates to notify of locki

### Fixed
 - Root permission option remains unchecked if root access not granted

### Removed
 - External SD Card permission. If log option is on, it will be written to Internal Storage/Android/data/com.jarsilio.android.waveup/files

## [0.99-2] 2016-05-23

### Fixed
 - Minor root permission reported errors (only relevant for Smart Lock workaround)
 - Suspending while ongoing call

## [0.99-1] 2016-05-21

### Added
 -  French translation. Thank you ko_lo!

### Fixed
 - SECURITY: avoid fragment injection!
 - Wave mode. Thanks again Tsuyoshi!
 - Android M read phone state permission
 - Material design for pre-lollipop devices

### Known issues
 - Suspending while ongoing call not working

## [0.99] - 2016-05-18

### Added
 - Romanian translation. Thank you so much licaon-kter!
 - Option to vibrate on lock.

### Fixed
 - Minor code improvements. Thank you so much Tsuyoshi!

### Removed
 - Auto start option

## [0.98-1] - 2016-05-15
### Fixed
 - Build issues with F-Droid
 - Minor logging changes

## [0.98] - 2016-05-12
### Improved
 - (and simplified) algorithm to switch screen on and off. The sensor is always off if the options and the phone state allow it.
 This might breakt some stuff but I really hope it doesn't :)

### Fixed
 - Crash while receiving a call if log to file activated

## [0.97] - 2016-05-11
### Added
 - Suspend WaveUp while on a phone call (needs READ_PHONE_STATE permission)

## [0.96-2] - 2016-05-05
### Fixed
- Compatibility issues: improvement in near/far measurement. Some phones report strange values and this should fix it.

## [0.96-1] - 2016-05-04
### Fixed
- Fix crash at startup on some phones (upgraded appcompat)

## [0.96] - 2016-05-04
### Added
- Japanese translation. Thank you Tsuyoshi!
- Small improvement in lock settings. Thank you for this too Tsuyoshi! :)

## [0.95] - 2016-04-30
### Added
- Switch off screen simulating power button

### Changed
- Performance improvements

### Known issues
- Lock in landscape option isn't really working

## [0.94] - 2016-04-27
### Fixed
- App crash while starting for the first time after a boot (if disabled)

## [0.93] - 2016-04-22
### Added
- Lock in landscape mode option

## [0.92] - 2016-04-18
### Added
- Logging options (included log to file)

## [0.91] - 2016-04-10
### Added
- First version of WaveUp
