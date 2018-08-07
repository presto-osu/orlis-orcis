#!/usr/bin/env python2

import os
import sys
import gen_class_feature
import preprocess
import gen_hash


def run(apkPath, outputDir, libResDir):
  # Remove libraries.
  if not os.path.exists(apkPath):
    print("[LOG]:No APK found! " + apkPath)
    return False
  tmpDir = 'tmp'
  if not os.path.exists(tmpDir): 
    os.mkdir(tmpDir)
  res = preprocess.run(apkPath, tmpDir,libResDir) 
  if res == False:
    print("[LOG]: Cannot preprocess the APK! "+apkPath)
    return False
 
  appname = os.path.basename(apkPath)
  # Generate app data object. 
  featureFn = tmpDir +'/'+appname +'.feature'
  gen_class_feature.run(tmpDir+'/'+appname+'.res', featureFn)
  if not os.path.exists(featureFn):
    print("[LOG]: Cannot generate feature of the APK! "+apkPath)
    return False

  # Generate app hash. 
  hashFn = outputDir+'/'+appname+'.hash'
  gen_hash.run(featureFn, hashFn)
  if not os.path.exists(hashFn):
    print("[LOG]: Cannot generate hash of the APK! "+apkPath)
    return False

  return True

if __name__ == "__main__":
  if len(sys.argv) < 2:
    print('Missing args, please refer to README.md')
    sys.exit(0)
  apkPath = sys.argv[1]
  outputDir = sys.argv[2] 
  libResDir = None
  if len(sys.argv) > 2:
    libResDir = sys.argv[2]
  run(apkPath, outputDir, None)
