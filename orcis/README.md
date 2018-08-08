Orcis is a clone detection tool for Android apps.

## App Hash File Generation:
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
The output is a text message that indicates whether they are clone or not.


## Benchmarks
* [Piggyback Data](https://github.com/serval-snt-uni-lu/Piggybacking)
* [AndroZoo Data](https://androzoo.uni.lu/repackaging)
* [CodeMatch Data](http://www.st.informatik.tu-darmstadt.de/artifacts/codematch/)

The links of Piggyback Data and AndroZoo data contain all necessary
information of apps and ground truth. All these apps can be downloaded from the app
repository of [AndroZoo](https://androzoo.uni.lu/).

The link of CodeMatch Data contains the name of apps and the ground truth.
We were able to downloaded 1589 apps. In our evaluation, we found 41 possible
mistakes in the ground truth. These 41 pairs seem to be clones while they are marked
as not clones in the ground truth of CodeMatch Data. The list of apps we have is shown in
"benchmarks/apps_list.txt" while the ground truth used in our evaluation is shown
in "benchmarks/ground_truth_codematch.txt", which only contains apps we used and
the ground truth of the 41 pairs are changed to clones. In the list of apps and ground
truth, the app name is changed to its hash value that we used to download from AndroZoo,
because AndroZoo creates a hash for each app and one could only downloaded an app based on
its hash. The boolean value in the end of each line in the ground truth
indicates the cloneness. "True" means these two apps are clone, while "False"
means they are not.
