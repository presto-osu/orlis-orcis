Orcis is a clone detection tool for Android apps

## Generate App Hash File:
To detect clones of two APKs, the first step is to obtain their similarity hash.
The hash function used in Orcis is [Sdhash](https://github.com/sptonkin/fuzzyhashlib).
The hashes are persisted in files for future use.

To generate the hash file for an app:

```
./gen_app_hash.py <path of apk file of an app>  <output directory> [<library repository directory>]
```

The third parameter is optional. It is the repository of the library used in Orlis. With this 
parameter set, all the library classes will be removed when detecting the clones. 

## Clone Detection:
To detect clone for two APKs(apk1 and apk2):

```
./clone_detection.py <path of hash file of apk1> <path of the hash file of apk2>
```

## Output Format 
The output is text message that indicates whether they are clone or not.
