Orcis is a clone detection tool for Android apps.

## App Hash File Generation
To detect clones of two APKs, the first step is to obtain their similarity hash.
The hash function used in Orcis is [Sdhash](https://github.com/sptonkin/fuzzyhashlib).
The hashes are persisted in files for future use.

To generate the hash file for an app:

```
./gen_app_hash.py <path of apk file of an app>  <output directory>
```

The third parameter is optional. It is the repository of the library used in Orlis. With this 
parameter set, all the library classes will be removed when detecting the clones. 

## Clone Detection:
To detect clone for two APKs (apk1 and apk2):

```
./clone_detection.py <path of hash file of apk1> <path of the hash file of apk2>
```

## Output Format 
The output is a text message shown on the screen that indicates whether they are clone or not.


## Benchmarks
* [Piggyback Data](https://github.com/serval-snt-uni-lu/Piggybacking)
* [AndroZoo Data](https://androzoo.uni.lu/repackaging)
* [CodeMatch Data](http://www.st.informatik.tu-darmstadt.de/artifacts/codematch/)

The links of Piggyback Data and AndroZoo data contain all necessary
information of apps and ground truth. All these apps can be downloaded from the app
repository of [AndroZoo](https://androzoo.uni.lu/).

The CodeMatch Data link contains app names and the ground truth.
We were able to download 1589 apps from those listed in the CodeMatch link. In our evaluation, we found 41 possible
mistakes in this ground truth and fixed them; details are described in Yan Wang's Ph.D. dissertation. 
The list of apps used in our experiments is shown in
"benchmarks/apps_list.txt" while the modified ground truth used in our evaluation is shown
in "benchmarks/ground_truth_codematch.txt", restricted to only the apps we used.
In the list of apps and in the ground
truth, the app name is changed to its hash value that we used to download the app from AndroZoo.
The boolean value at the end of each line in the ground truth is "True" if these two apps are clones, and "False" otherwise.
