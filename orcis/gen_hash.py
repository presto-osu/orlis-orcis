import sys
import os
import subprocess
import tlsh
import fuzzyhashlib
from nilsimsa import Nilsimsa, compare_digests, convert_hex_to_ints
import os
from multiprocessing import Pool

def to_hash(content):
  try:
    sh = fuzzyhashlib.sdhash(content).hexdigest().rstrip()
  except:
    sh = 'None'
  return sh


def writeToFile(output, content):
  f= open(output, 'w')
  f.write(content)
  f.close()
  

def run(fn, outputFn):
  f = open(fn)
  content = f.readlines()
  if len(content)<1:
    return
  content = content[0].rstrip().strip()
  ss = to_hash(content)
  write = ss
  writeToFile(outputFn, write)
  f.close()
   
