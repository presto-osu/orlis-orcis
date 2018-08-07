This is a tool for library class detection for Android apps.

1. Prerequest:
 (1) python2.7
 (2) JDK1.8 or later
 (3) Android SDK
 (4) Similarity Digest libraries for Python:
   (a) tlsh(https://github.com/trendmicro/tlsh)
   (b) fuzzyhashlib(https://github.com/sptonkin/fuzzyhashlib)
   (c) nilsimsa(https://github.com/diffeo/py-nilsimsa)

2. Library File Generation:
To detect library classes, a library repository should be built using the
library jar file. To generate the library file for a particular library jar,
run command in current directory:

```
./run.py <path of library jar file>  <output directory>
```

3. Library Class Detection:
To detect library classes for a given APK file, run command in current
directory:

```
./run.py <path of APK file> <path of the library repository(including all library files)>
```

4. Output Format The output is a list of class pair in the format of
<app class, lib class> printed on the screen. This means the app class in the APK file.
