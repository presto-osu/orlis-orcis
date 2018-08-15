# Change Log
##[0.7] - 2016-06-03
### Added
- User will be notified if a network cannot be removed.
- Outdated SSIDs will not be shown anymore per default. The user can change this in the settings. Outdated SSIDs that are saved in the network configuration will be marked as outdated.

### Changed
- Improved UI
- Improved compatability
- Code improvements
- Retrive Freifunk node locations in a compressed file to save traffic.

## [0.6.1] - 2015-10-31
### Changed
- Google Play services location API was replaced with [LOST](https://github.com/mapzen/LOST).

### Fixed
- Fixed crash that occurred if location service was disabled or airplane mode was enabled.

## [0.6] - 2015-10-24
### Added
- The app can now show the nearest Freifunk access points to the location of the device.

### Changed
- The UI was improved.

### Fixed
- Fixed a problem where new SSIDs weren't shown when the filter function was used right after the list of SSIDs was updated.

### Removed
- The feature to add own SSIDs was removed. There should be a folder named "freifunkautoconnect" containing the file "user_ssids.csv" on your SD card if version <= 0.5 was installed on your device. You can remove this folder, it is no longer needed.

## [0.5] - 2015-08-31
### Added
- The app will check on start up, if an updated list of SSIDs is available. If the check didn't fail, the next check will not be started until 24 hours have passed.

### Changed
- Notifications will also be displayed on the lock screen.

## [0.4] - 2015-08-15
### Changed
- Improved notification settings.

### Fixed
- Restart NotificationService if device was rebooted.
- Don't create a new SettingsFragment on top the existing SettingsFragment if the screen is rotated.

## [0.3] - 2015-07-30
### Added
- New SSIDs.
- Option to notify the user if a freifunk network is available.

### Changed
- SSIDs will now be sorted by the app on start up.
- Rewrote some loops to be more efficient.

### Fixed
- App won't crash anymore if screen is rotated while process dialog is shown.

## [0.2] - 2015-05-24
### Added
- More SSIDs.
- Option to filter shown SSIDs.
- A progress bar is shown while all SSIDs are added or removed.
- Info page with some information about the app.

### Changed
- Changed design to look more like the material design