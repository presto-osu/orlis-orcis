#!/usr/bin/env python2

import sys
import os
import fuzzyhashlib

def simSdhash(h1,h2):
  try:
    h1 =  fuzzyhashlib.sdhash_wrapper.sdbf_from_hash(h1)
    h2 =  fuzzyhashlib.sdhash_wrapper.sdbf_from_hash(h2)
    res= h1.compare(h2,0)
    return res
  except:
    return None

def getHash(hashFn):
  f = open(hashFn)
  content = f.readlines()
  if len(content) < 1:
    return None
  h = content[0].rstrip() 
  return h
  

def run(hashFn1, hashFn2):
 h1 = getHash(hashFn1) 
 h2 = getHash(hashFn2) 
 if h1 == None or h2 == None:
    return False
 try:
   sim = simSdhash(h1,h2)
   return sim>0
 except:
   return False



if __name__ == "__main__":
  if run(sys.argv[1], sys.argv[2]):
     print('[RESULT]: They are clones')
  else:
     print('[RESULT]: They are not clones')
    
