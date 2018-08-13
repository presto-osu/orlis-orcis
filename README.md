Orlis is a tool for obfuscation-resilient detection of third-party libraries in Android apps, using interprocedural code features and similarity digests. The related Orcis tool can be used for obfuscation-resilient detection of app clones. The library detection tool is in the folder ‘orlis’, while the app clone detection tool is in the folder ‘orcis’.

Detailed usage instructions for both tools can be found in the ‘README.md’ files in their corresponding directories.

Orlis is described in the paper “Orlis: Obfuscation-Resilient Library Detection for Android” by Yan Wang, Haowei Wu, Hailong Zhang, and Atanas Rountev, which appeared at the IEEE/ACM International Conference on Mobile Software Engineering and Systems (MOBILESoft'18) [PDF](http://web.cse.ohio-state.edu/presto/pubs/msoft18.pdf) [BibTeX](http://web.cse.ohio-state.edu/presto/pubs/msoft18.bib) (here put links to the PDF and the BibTex files on the web page]. Orcis is described in Yan Wang’s Ph.D. dissertation \[[PDF]\](http://web.cse.ohio-state.edu/presto/pubs/wang_phd18.pdf)[BibTeX](http://web.cse.ohio-state.edu/presto/pubs/wang_phd18.bib) 

## Prerequisites:
 * python2.7
 * JDK1.8 or later
 * Android SDK (To run the tools, please set environment variable ANDROID_SDK=/path/to/Android SDK)
 * Similarity Digest libraries for Python:
   * [tlsh](https://github.com/trendmicro/tlsh)
   * [fuzzyhashlib](https://github.com/sptonkin/fuzzyhashlib)
   * [nilsimsa](https://github.com/diffeo/py-nilsimsa)
