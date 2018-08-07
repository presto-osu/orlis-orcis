import sys
import os
from multiprocessing import Pool

def parseLog(fn, Filter=None):
  f = open(fn)
  content = f.readlines()
  ret = {}
  if len(content) < 2:
     return ret
  for c in content[1].split('#'):
    c = c.strip()
    if c == '':
      continue
    c = c.split('@')
    classname = c[1]
    feature = c[0]
    if Filter and classname in Filter:
      continue
    ret[classname] = feature
  return ret

def genFeature(ret):
  res = []
  for c in ret:
    res.append(ret[c])
  res.sort()
  ret = ''
  for i in res:
    ret = ret+i+'|'
  return ret

def writeToFile(fn, content): 
  f = open(fn,'w')
  f.write(content)
  f.close()

def run(inputF, outputF):
  if os.path.isfile(outputF): 
    return
  try:
    tmp = parseLog(inputF)
    feature = genFeature(tmp)
    writeToFile(outputF, feature)
  except:
    pass
