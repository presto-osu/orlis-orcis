#!/bin/bash

COMPILESDK_VER="23"
BUILDTOOLS_VER="23.0.1"

if [ ! -e 'libsuperuser' ]; then
    echo '>Clone libsuperuser'
    git clone https://github.com/chainfire/libsuperuser --depth=1
    mv libsuperuser/libsuperuser tmp
    rm -rf libsuperuser
    mv tmp libsuperuser
    cp ./build.gradle.libsuperuser ./libsuperuser/build.gradle
fi
