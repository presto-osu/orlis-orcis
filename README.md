This is a tool for library class detection for Android apps.

The paper is available in [here](http://web.cse.ohio-state.edu/presto/pubs/msoft18.pdf):

## Prerequest:
 * python2.7
 * JDK1.8 or later
 * Android SDK
 * Similarity Digest libraries for Python:
   * [tlsh](https://github.com/trendmicro/tlsh)
   * [fuzzyhashlib](https://github.com/sptonkin/fuzzyhashlib)
   * [nilsimsa](https://github.com/diffeo/py-nilsimsa)

## Library File Generation:
To detect library classes, a library repository should be built using the
library jar file. To generate the library file for a particular library jar,
run command in current directory:

```
./run.py <path of library jar file>  <output directory>
```

## Library Class Detection:
To detect library classes for a given APK file, run command in current
directory:

```
./run.py <path of APK file> <path of the library repository(including all library files)>
```

## Output Format The output is a list of class pair in the format of
<app class, lib class> printed on the screen. This means the app class in the APK file.
