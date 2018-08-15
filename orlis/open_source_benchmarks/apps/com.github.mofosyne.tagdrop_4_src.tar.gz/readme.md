# tagdrop - Tag Dead Drop

Is is a work in progress. The concept is embedding small amount of media physically on paper as 2d QR barcodes. This is unlike current usage of QR codes, where it is normally used to just store plaintext or urls or contacts.

Instead it would be nice if you can store a simple audio soundclip, or a small javascript game, etc...

1. First objective is to decode datauri sent in via intent, and display render via android web browser. e.g. from zxing QR barcode scanner app, or reading it from an NFC tag.

2. Secondary objective, is that for larger files, you want to spread it over multiple QR codes.
So will need a way to read all these tags an then join it together.

# Instructions:

Make sure you have zxing barcode reader installed, otherwise this app will just crash.

There is two ways to open a datauri:

* For datauris that fit in a single QR code, just scan directly from zxing barcode reader app, press "open browser" and you can start viewing it.
* For longer datauris split across multiple barcodes, just scan them all in seqence using the main tag dead drop app.

# Status

* V1.2 - Implemented dumb append scanning. Also added framework to make smarter structured appends, and perhaps a direct binary mode to avoid having to use base64 (with has 30% overhead).

* V1.0 - First objective is completed. Secondary objective is not completed, as there is no easy way to split a file across multiple QR codes in an easy to use manner.

## todo

* Probbly need a way to "undo" last scan, if the last scan was incorrect. This is possible since I store each barcode content seperately before joining it together for sending as datauri.

* Need some way to define a manifest QR code, so we can do more funky stuff like store QR codes as direct binary file, or implement compression (in a transparent futureproof mannner). You can think this as similar to a http header.

# Extra

Category: tools
Source: https://github.com/mofosyne/tagdrop
Licence: https://github.com/mofosyne/tagdrop/blob/master/COPYING.txt - GNU GENERAL PUBLIC LICENSE Version 3, 29 June 2007



# Test

Below is a sample of what was tested. This works with datauris that stores html, or even sound :D  (but you probbly need to split it up to multiple barcodes)!

## Try this for size:

![QR Code Of A Running Man approx 3kb](https://raw.githubusercontent.com/mofosyne/tagdrop/master/exampleBarcodes/denseRunningManQR.png)


## Source:

Can't seem to recall where this running man image came from. If anyone knows, let me know in issues and I'll add the source link. To see it in action, just copy and paste to your url field in your webbrowser.

    data:image/gif;base64,R0lGODlhEAAQAPcAAP///wAAAP8AAAD/AP//AAAA//8A/wD//wAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAACH/C05FVFNDQVBFMi4wAwEAAAAh+QQIPQAAACwAAAAAEAAQAAACI4QdecuQ0eKLtK6HK84yz7N9nzNxSndqHFVCKrJe7tZZslUAACH5BAgBAAAALAAAAAAQABAAAAIihB15y5DR4ou0sjcpxnoryX2Lx12k2GyWU1aqRbpQm0JUAQAh+QQIAQAAACwAAAAAEAAQAAACIISPqcHqHNxqsloDo81Ucu1x1QcmZImQl7ixY3YBHVIAACH5BAgBAAAALAAAAAAQABAAAAIdhI+pweoc3GqyWgOjzbry+20ZOIoQeZoUeh3rUQAAIfkECAEAAAAsAAAAABAAEAAAAh+EHXnLkNHii7S2By3OdVuHfZ4WklM3UhsnpWopskcBACH5BAgBAAAALAAAAAAQABAAAAIghB15y5DR4ou0tgctVnVn6nUhOJKTiGmbuooOZyGfUQAAIfkECAEAAAAsAAAAABAAEAAAAiOEHXnLkNHii7SypyzOcvPlOVCjhJVJYZY4adDXsS4Cn6NRAAA7
