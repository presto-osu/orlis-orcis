<img align="right" height="256" src="https://lut.im/3IqLwsAZWH/piFLRMOgNLWmiqB8.png">
# Red Moon 

Red Moon is a screen filter app for night time phone use. When the
lowest brightness setting of your phone won't do the trick, Red Moon
makes your screen even darker. With Red Moon, using your phone in the
dark won't hurt your eyes and thanks to it's red filter feature, you
will have a healthier sleep.

[![F-Droid](https://f-droid.org/wiki/images/0/06/F-Droid-button_get-it-on.png)](https://f-droid.org/repository/browse/?fdid=com.jmstudios.redmoon)

[Direct download](https://github.com/raatmarien/red-moon/releases/download/v2.7.4/red-moon-v2.7.4.apk)

[![Translation status](https://hosted.weblate.org/widgets/red-moon/-/svg-badge.svg)](https://hosted.weblate.org/engage/red-moon/?utm_source=widget)

## Features

* Control the filter with separate color, intensity and dim settings
* Use the default profiles or create your own to quickly choose the
right filter settings
* Schedule automatic turn on and off times, so you don't have to worry
about turning the filter on at night
* Use the persistent notification to pause or stop the filter without
leaving the application you're using
* Control the filter with a widget or a shortcut, right from your
homescreen

## Screenshots

<img src="https://lut.im/OiN16OuV2i/8Q3EWSlBTusiIYy6.png" width="180" height="320" />
<img src="https://lut.im/jtnrvFKcg7/P2qXtOeIFehZiquu.png" width="180" height="320" />
<img src="https://lut.im/Wfol7UhVJc/GQ7MkuNhg5mKjcxg.png" width="180" height="320" />
<img src="https://lut.im/5bxAfD7nwF/x4K5KR9yBTNqqxC5.png" width="180" height="320" />
<img src="https://lut.im/MpAf2riWO1/evmCy7ZdnLq5ol3w.png" width="180" height="320" />
<img src="https://lut.im/fmyFJXPj7h/iPi9QoFhjZATSqzH.png" width="180" height="320" />
<img src="https://lut.im/n43HkAuKDH/hQHSONsBjJRRZKDE.png" width="180" height="320" />

## License

"Red Moon" is a derivative of
"[Shades](https://github.com/cngu/shades)" by
[Chris Nguyen](https://github.com/cngu) used under the
[MIT License](https://github.com/cngu/shades/blob/e240edc1df3e6dd319cd475a739570ff8367d7f8/LICENSE). "Red
Moon" is licensed under the
[GNU General Public License version 3](https://www.gnu.org/licenses/gpl-3.0.html),
or (at your option) any later version by Marien Raat.

[![GNU GPLv3 Image](https://www.gnu.org/graphics/gplv3-127x51.png)](http://www.gnu.org/licenses/gpl-3.0.en.html)

All used artwork is released into the public domain. Some of the icons
use cliparts from [openclipart.org](https://openclipart.org/), which
are all released in the public domain, namely:
* https://openclipart.org/detail/121903/full-moon
* https://openclipart.org/detail/219211/option-button-symbol-minimal-svg-markup
* https://openclipart.org/detail/20806/wolf-head-howl-1
* https://openclipart.org/detail/213998/nexus-5-flat
* https://openclipart.org/detail/192689/press-button

## Building

To build the app on GNU+Linux, clone the repository and run

``` ./gradlew build ```

in the root directory.

Use

``` ./gradlew installDebug ```

to install the app on a connected device.

## How it works

Red Moon displays a constant transparant overlay to dim and color the
screen when the filter is on. If, for example, you have the intensity
set to 0%, then the overlay will be black with a transparency equal to
the dim level you selected. If you choose a higher intensity, the
color will be saturated with the selected color.

## Contributing

All help is welcome! If you have found a bug or have an idea for a new
feature, just
[open a new issue](https://github.com/raatmarien/red-moon/issues/new). If
you can implement it yourself, simply fork this repository, make your
changes and open a pull request.

## Translating

Translating is very much appreciated! Just go to
[Weblate](https://hosted.weblate.org/projects/red-moon/strings/), create
an account and start a new translation or update an existing one.
