#!/usr/bin/env python2

import sys
import os
import subprocess
import tlsh
import fuzzyhashlib
from nilsimsa import Nilsimsa, compare_digests, convert_hex_to_ints
import os
import time

def build_call_graph(fn, outputDir, isApp='true'):
  basename =  os.path.basename(fn)
  output = outputDir+'/'+basename+'.data'
  command = 'java -jar -Xss64m -Xmx8G -Xms2G call_graph_builder.jar ' + fn + ' '+ output +  ' '+ isApp
#  print(command)
  try:
     out = subprocess.check_output(command,stderr=subprocess.STDOUT, shell=True)
     return '[SUCCESS]: '+fn
  except:
     return '[ERROR]: ' +fn
 
def to_hash(h1,h2, classes):
  try:
    th = tlsh.hash(h1)
  except:
    th = 'None'

  try:
    sh = fuzzyhashlib.sdhash(h1).hexdigest().rstrip()
  except:
    sh = 'None'

  try:
    nil = Nilsimsa(h1).hexdigest()
  except:
    nil = 'None'

  try:
    ss = fuzzyhashlib.ssdeep(h1).hexdigest()
  except:
    ss = 'None'

  ch = []
  if classes!=None:
    for c in classes:
      name = c[0]
      content = c[1]
      try:
        cnil = Nilsimsa(content).hexdigest()
      except:
        cnil = 'None'

      try:
        css = fuzzyhashlib.ssdeep(content).hexdigest()
      except:
        css = 'None'

      try:
        csh = 'None'
        if len(content)  >= 512:
          csh = fuzzyhashlib.sdhash(content).hexdigest().rstrip()
      except:
        csh = 'None'

      try:
        cth = 'None'
        if len(content) >= 256:
          cth = tlsh.hash(content)
      except:
        cth = 'None'
      ch.append((name,cth,csh,cnil,css))
  return th,sh,nil,ss,ch

def parse_log(fn):
  f = open(fn)
  content = f.readlines()
  if len(content)< 2:
    return None,None,None
  h1 = content[0].rstrip()
  h2 = content[1].rstrip()
  classes = []
  for c in h2.split('#'):
    if c=='':
      continue
    name = c.split('@')[1]
    h = c.split('@')[0]
    classes.append((name,h))
  return h1, h2, classes

def write_to_file(th,sh,nil,ss,ch, output):
   f = open(output,'w')
   f.write(th+'#'+sh+'#'+nil+'#'+ss)
   f.write('\n')
   for c in ch:
     f.write(c[0]+'#'+c[1]+'#'+c[2]+'#'+c[3]+'#'+c[4])
     f.write('|')
   f.close()

def build_hash(inputF, outputF):
  h1,h2,classes = parse_log(inputF)
  if h1==None or h2==None or classes==None:
    return
  th,sh,nil,ss,ch = to_hash(h1,h2,classes)
  if th==None or sh==None or ch==None:
    return
  write_to_file(th, sh,nil,ss,ch, outputF)

def gen_lib_fn(jarPath, outputDir):
  libName = os.path.basename(jarPath)
  stage1 = outputDir+'/'+libName+'.data'
  stage2 = outputDir+'/'+libName+'.result'
  
  build_call_graph(jarPath, outputDir, 'false')
  if not os.path.isfile(stage1):
    return ''

  build_hash(stage1, stage2)
  if not os.path.isfile(stage2):
    os.remove(stage1)
    return ''

  os.remove(stage1)
  print '[SUCC]:The result is in "' + os.path.abspath(outputDir)+ '"'

def parse_hash_log(fn): 
  f = open(fn)  
  content = f.readlines() 
  h = content[0].rstrip() 
 
  hashes = h.split('#') 
  try: 
    th = hashes[0] 
    sh = hashes[1] 
    if sh=='' or sh=='None': 
      sh = None 
    else: 
      sh = fuzzyhashlib.sdhash_wrapper.sdbf_from_hash(sh) 
    nil = hashes[2] 
    ss = hashes[3] 
  except: 
    return None 
 
  cc = [] 
  if len(content) > 1: 
    cls = content[1].rstrip() 
    for c in cls.split('|'): 
      c = c.strip() 
      if c =='': 
        continue 
      splits = c.split('#') 
      name = splits[0] 
      cth = splits[1] 
      csh = splits[2] 
      cnil = splits[3] 
      css = splits[4] 
      if cth=='': 
        cth='None' 
      if csh=='': 
        csh='None' 
      if cnil=='': 
        cnil='None' 
      if css=='': 
        cth='None' 
      cc.append((name,cth,csh,cnil,css)) 
    return th,sh,nil,ss,cc 
  return None 
  
def calcScore(app, lib):
   appSh = app[1]
   libSh = lib[1]
   score = appSh.compare(libSh,0)
   return score

def stage1(app,libs):
  ret = []
  for lib in libs:
    if libs[lib][1] == None:
      continue
    score = calcScore(app,libs[lib])
    if score > 0:
     ret.append((score, libs[lib]))
  return ret

def simTLSH(h1,h2):
  return tlsh.diffxlen(h1,h2)
def simSdhash(h1,h2):
  h1 =  fuzzyhashlib.sdhash_wrapper.sdbf_from_hash(h1)
  h2 =  fuzzyhashlib.sdhash_wrapper.sdbf_from_hash(h2)
  return h1.compare(h2,0)
def simNil(h1,h2):
  return compare_digests(h1, h2)
def simSsdeep(h1,h2):
  return fuzzyhashlib.libssdeep_wrapper.compare(h1,h2)

def stage2(app,libs, clsT, index):
  appCls = app[4]
  res = []
  allLibCls = []
  for lib in libs:
    libCls = lib[1][4]
    allLibCls.extend(libCls)

  return calcPairs(appCls, allLibCls, clsT, index)

def calcPairs(ac, lc, clsT, index):
   ret = []
   func = Funcs[index]
   for a in ac:
     for l in lc:
       h1 = a[index+1]
       h2 = l[index+1]
       if h1=='None' or h2=='None':
         continue
       sim = func(*(h1,h2))
       if func == simTLSH:
         shouldAdd = sim < clsT
       else:
         shouldAdd = sim > clsT
       if shouldAdd:
         ret.append((a[0],l[0], sim))
   if func == simTLSH:
     ret = sorted(ret, key=lambda x:x[2])
   else:
     ret = sorted(ret, key=lambda x:x[2], reverse=True)
   vl = set()
   va = set()
   res = {}
   for a,l,score in ret:
     if a in va or l in vl:
       continue
     va.add(a)
     vl.add(l)
     res[a] = (l,score)
   return res

def get_library_class(libs, app, clsT=0, index=3):
  ret = stage1(app, libs)
  ret = stage2(app, ret, clsT, index)
  return ret

def get_all_libs(libDir):
  libs = {}
  for lib in os.listdir(libDir):
    tmp = parse_hash_log(libDir+'/'+lib)
    if tmp == None:
      continue
    libs[lib] = tmp
  return libs

def run(Input, libs, outputDir='./'):
  appname = os.path.basename(Input)
  stage1 = outputDir+'/'+appname+'.data'
  stage2 = outputDir+'/'+appname+'.result'
  
  build_call_graph(Input, outputDir)
  if not os.path.isfile(stage1):
    return ''

  build_hash(stage1, stage2)
  if not os.path.isfile(stage2):
    os.remove(stage1)
    return ''

  h1,_, classes = parse_log(stage1)
  app = parse_hash_log(stage2)
  ret = get_library_class(libs, app)

  os.remove(stage1)
  os.remove(stage2)

  return ret

def get_fns(fn):
 f = open(fn)
 ret = []
 for line in f.readlines():
   ret.append(line.rstrip())
 f.close()
 return ret

def detect_lib_cls(apkPath, libDir):
 libs = get_all_libs(libDir)
 try:
   return run(apkPath, libs)
 except:
   print('[FAIL]: ' + apkPath)
   return None

def show_result(ret):
  for i in ret:
    print(i+' -> ' + ret[i][0])

def check_env():
  if not 'ANDROID_SDK' in os.environ:
    print("[ERROR]: ANDROID_SDK should be set, which is the directory of Android SDK!!!")
    return False
  return True


Funcs = [simTLSH, simSdhash, simNil, simSsdeep]

if __name__ == "__main__":
  if not check_env():
    sys.exit(0)
  if len(sys.argv) < 3:
    print('Arguments Number Incorrect!')
    print(' For library file generation:')
    print('  (1)The path of library jar file,')
    print('  (2)The output directory of the result file.')
    print(' For library class detection:')
    print('  (1)The path of apk file,')
    print('  (2)The library files directory for detection.') 
    sys.exit(0)

  inputFn = sys.argv[1]  
  if inputFn.endswith('.apk'):
     libDir = sys.argv[2]
     ret = detect_lib_cls(inputFn,sys.argv[2])     
     if ret != None:
       show_result(ret)
  elif inputFn.endswith('.jar'):
     gen_lib_fn(inputFn, sys.argv[2])
  else:
    print('Error In File Format. Only files with suffix ".apk" and ".jar" are supported')
