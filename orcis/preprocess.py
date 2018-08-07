import sys
import os
import subprocess
import tlsh
import fuzzyhashlib
from nilsimsa import Nilsimsa, compare_digests, convert_hex_to_ints
import os
import time
from multiprocessing import Pool

###############################################

def BuildCallGraph(fn, outputDir, isApp='true'):
  basename =  os.path.basename(fn)
  output = outputDir+'/'+basename+'.data'
  command = 'java -jar -Xss64m -Xmx16G -Xms2G dataGenWithSCV2.jar ' + fn + ' '+ output +  ' '+ isApp
  print(command)
  try:
     out = subprocess.call(command,stderr=subprocess.STDOUT, shell=True)
     return '[SUCCESS]: '+fn
  except:
     return '[ERROR]: ' +fn
 
#############################################

def ToHash(h1,h2, classes):
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

def ParseLog(fn):
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

def WriteToFile(th,sh,nil,ss,ch, output):
   f = open(output,'w')
   f.write(th+'#'+sh+'#'+nil+'#'+ss)
   f.write('\n')
   for c in ch:
     f.write(c[0]+'#'+c[1]+'#'+c[2]+'#'+c[3]+'#'+c[4])
     f.write('|')
   f.close()

def BuildHash(inputF, outputF):
  h1,h2,classes = ParseLog(inputF)
  if h1==None or h2==None or classes==None:
    return
  th,sh,nil,ss,ch = ToHash(h1,h2,classes)
  if th==None or sh==None or ch==None:
    return
  WriteToFile(th, sh,nil,ss,ch, outputF)

#############################################

def parseLog(fn): 
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
       #print a[0]+'->'+l[1]+ ' : ' + str(sim)
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

Funcs = [simTLSH, simSdhash, simNil, simSsdeep]
def GetLibraryClass(libs, app, clsT=0, index=3):
  ret = stage1(app, libs)
  ret2 = stage2(app, ret, clsT, index)
  return ret2

def GetLibraryClassFromCodeMatch(app):
  libFn = ''

def GetAllLibs(libDir):
  libs = {}
  if libDir == None:
    return libs
  for lib in os.listdir(libDir):
    tmp = parseLog(libDir+'/'+lib)
    if tmp == None:
      continue
    libs[lib] = tmp
  return libs


#############################################

def WriteToFileOriginal(h1, cc , fn):
  secondline = ''
  for name,h in cc:
    secondline=secondline+h+'@'+name+'#'
  f = open(fn,'w')
  f.write(h1+'\n'+secondline)
  f.close()

def WriteToFileAgain(app,fn):
  th = str(app[0])
  sh = str(app[1].to_string()).rstrip()
  nil = str(app[2])
  ss = str(app[3])
  firstline = th+'#'+sh+'#'+nil+'#'+ss
  secondline = ''
  for c in app[4]:
    tmp = c[0]+'#'+str(c[1])+'#'+str(c[2])+'#'+str(c[3])+'#'+str(c[4])+'|'
    secondline = secondline+tmp
  f = open(fn,'w')
  f.write(firstline+'\n'+secondline)
  f.close()

def RemoveLibClasses(cc, res):
  i =0
  while i < len(cc):
     name = cc[i][0]
     if name in res:
       del cc[i]
     else:
        i = i+1

def Run(Input, outputDir, libs):
  appname = os.path.basename(Input)
  stage1res = outputDir+'/'+appname+'.data'
  stage2res = outputDir+'/'+appname+'.2.data'
  stage3res = outputDir+'/'+appname+'.res'
  

  if os.path.isfile(stage3res): 
    return True

  BuildCallGraph(Input, outputDir)
  if not os.path.isfile(stage1res):
    return False
  
  if len(libs) > 0:
    BuildHash(stage1res, stage2res)
    if not os.path.isfile(stage2res):
      os.remove(stage1res)
      return False
    h1,_, classes = ParseLog(stage1res)
    app = parseLog(stage2res)
    res = GetLibraryClass(libs, app)
#  ret = get_lib_class(Input, res)
    if libs and appname in libs:
      RemoveLibClasses(classes, libs[appname])
    RemoveLibClasses(classes, res)  
    WriteToFileOriginal(h1, classes, stage3res)
  else:
    h1,_, classes = ParseLog(stage1res)
    WriteToFileOriginal(h1, classes, stage3res)

  if os.path.exists(stage1res): 
    os.remove(stage1res)
  if os.path.exists(stage2res): 
    os.remove(stage2res)
  return True

def get_lib_class(apk, res):
  output = apk +':'
  for libCls in res:
    output = output+libCls+';'
  return output+'\n'

def run(apk, outputDir, libResDir):
  libs = GetAllLibs(libResDir)
  try:
    res = Run(apk,outputDir, libs)
    return res
  except:
    return False

#print(run('1.apk', 'tmp', 'libs'))

