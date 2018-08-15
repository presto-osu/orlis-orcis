#!/bin/bash
INKSCAPE="/c/Program\ Files/Inkscape/inkscape.exe"
MDPI=./res/drawable-mdpi/
HDPI=./res/drawable-hdpi/
XHDPI=./res/drawable-xhdpi/
XXHDPI=./res/drawable-xxhdpi/
XXXHDPI=./res/drawable-xxxhdpi/
DPI=($MDPI $HDPI $XHDPI $XXHDPI $XXXHDPI)
WIDTH=(48 72 96 144  192)

SVG=.svg
PNG=.png

FILE=$(basename $1 $SVG)

echo Generating $FILE drawables...

for ((i=0;i<${#DPI[@]};++i))
do
    DIR=${DPI[i]}
    W=${WIDTH[i]}
    mkdir -p $DIR
    eval $INKSCAPE -e $DIR$FILE$PNG -w $W -f $FILE$SVG
    echo -e '  ' - $(basename ${DPI[i]} /)
done
echo Done!
