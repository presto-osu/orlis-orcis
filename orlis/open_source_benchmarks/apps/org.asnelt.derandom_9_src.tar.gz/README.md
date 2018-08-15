Derandom
========

Predicts pseudo random numbers based on a sequence of observed numbers.


Usage
-----

Enter a sequence of numbers that you obtained from a pseudo random number
generator like, for instance, the Java standard pseudo random number
generator or the Mersenne Twister MT19937.  The app will then try to
predict following numbers from the generator.

Three input modes are supported:

1. *Text field* lets you enter the numbers directly on the device.
2. *File* lets you choose a file with newline separated number strings.
3. *Socket* opens a server socket on the device.  You can then connect
with a custom client by means of a client socket and send newline
separated number strings to the server.  After each number the server
will send back the next newline separated predictions.  Each block of
predictions is separated by an additional newline.

The app expects all numbers to be in a signed integer format.


Building from source
--------------------

Define SDK location with sdk.dir in the local.properties file or with an
ANDROID_HOME environment variable.  Then type the following command to
build in release mode:
```
./gradlew assembleRelease
```


License
-------

```text
Copyright (C) 2015 Arno Onken

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
