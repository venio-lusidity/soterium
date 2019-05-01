#!/bin/bash
WEB_DIR="/mnt/xdata/opt/soterium/web/jetty"
DATE=$(date +"%Y%m%d%H%M%S")
BK_NAME=bin.old.$DATE
UH=/home/cons3rt
cd $UH
echo $PWD
rm -rf root.zip
wget --no-check-certificate https://dev.lusidity.com/files/root.zip -O $UH/root.zip
echo "Downloading validation file"
wget --no-check-certificate https://dev.lusidity.com/files/checksum-root.md5 -O $UH/checksum-root.md5
echo "Validating File"
if md5sum -c $UH/checksum-root.md5; then
    echo "File is valid."
    if [ -d "root" ]; then
    echo "removing root folder"
    rm -rf root
    fi
    unzip root.zip
    rm -rf $WEB_DIR/webapps/root/*
    cp -r root/* $WEB_DIR/webapps/root
else
    echo "File is not valid"
fi