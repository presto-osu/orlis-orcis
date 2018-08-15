#!/bin/bash

for i in GrazAP_gap*svg; do 
n=${i/svg/png}; n=${n/GrazAP/room}; echo "$i --> $n"; 
inkscape -e $n -C -w 700 $i
done
rm room_gesamt.png 
rm room_gesamt_leer.png
inkscape -e campusmap.png -C -w 1800 GrazAP_gesamt.svg

