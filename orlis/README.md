Orlis library class detection tool for Android apps.

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

## Output Format 
The output is a list of class pair in the format of
<app class, lib class> printed on the screen. This means the app class in the APK file.


## Open-Source Apps Benchmarks
### Apps:
The open-source apps are from F-droid. They are in the tar.gz file
"./open_source_benchmarks/apps.tar.gz".

### Libraries:
The library jar files in this benchmarks is in zip file
"./open_source_benchmarks/libraries.zip".

### Obfuscators:
The obfuscators used in our evaluation are 
* ProGuard 5.2.1
* Allatori Obfuscator v5.9 Educational
* DashO 8.0.0

The configuration file of each obfuscator is shown as below:
* ProGuard: ./open_source_benchmarks/obfuscator_configs/proguard_config.txt
* Allatori: ./open_source_benchmarks/obfuscator_configs/allatori_config.xml
* DashO   : ./open_source_benchmarks/obfuscator_configs/dasho_config.dox

### Ground Truth:
 The ground truth for unobfuscated apps is in file
"./open_source_benchmarks/ground_truth.txt". Each line is in the
format of "<app>: <lib jar>, <lib jar>, ...".  The "lib jar" is the
library jar used in the app. Because the apps are not obfuscated,
the classes in the library jars also exist in the app.
   
 As for obfuscated apps, applying the obfusctor and a mapping file
will be generated. In the mapping file, the class signature before
and after obfuscation will be shown. For example:
"org.apache.commons.codec.net.URLCodec -> wy.ael" is a case in the
mapping file of ProGuard. It means
"org.apache.commons.codec.net.URLCodec" is renamed to "wy.ael" in
the obfuscated app. Both Allatori and DashO have a similar mapping
file. Based on the mapping file and the ground truth for
unobfsucated apps, the ground truth for obfuscated apps can be
built. Please refer to the document of each obfuscator for the
details about how to get the mapping files. The links of the
documents are shown as below: 

* ProGuard: https://www.guardsquare.com/en/proguard
* Allatori: http://www.allatori.com/doc.html
* DashO   : https://www.preemptive.com/products/dasho/videos-a-resources(need request)

### Experiment Replica:
* Obfuscate the apps using the obfuscators with the config files and get the mapping files for each app.
* Build the ground truth for obfuscated apps using ground truth for unobfuscated apps and the mapping files.
* Build library repository using the library jar files.
* Run Orlis on each obfuscated app.
* Measure the performance using the result of Orlis and ground truth.

## Closed-Source Apps Benchmarks
The libraries and apps in closed-source apps benchmarks are from the
website of
[CodeMatch](http://www.st.informatik.tu-darmstadt.de/artifacts/codematch/).
The links to the lists of libraries and apps are shown as below:
* Libraries : https://github.com/stg-tud/CodeMatch-LibDetect/blob/master/CodeMatch/LibDetectDatabase.txt
* Apps      : http://www.st.informatik.tu-darmstadt.de/artifacts/codematch/LibDetectionEvaluation.txt

To run the experiment in the paper, build the library repository
using the library jars and run Orlis on each app. The ground truth
can be obtained from the authors of CodeMatch. For more details,
please refer to the
[website](http://www.st.informatik.tu-darmstadt.de/artifacts/codematch/).
