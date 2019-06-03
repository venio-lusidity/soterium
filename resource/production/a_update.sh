#!/bin/bash
ATHENA_DIR="/mnt/xdata/opt/soterium/athena"
DATE=$(date +"%Y%m%d%H%M%S")
BK_NAME=bin.old.$DATE
UH=/home/cons3rt
isRoot(){
    if [ "$(id -u)" != "0" ]; then
       echo "This script must be run as root" 1>&2
       exit 1
    fi
}

isRoot

cd $ATHENA_DIR
echo $PWD
sh app-init-d.sh stop
wget --no-check-certificate https://dev.lusidity.com/files/bin.zip -O $ATHENA_DIR/bin.zip
echo "Downloading validation file"
wget --no-check-certificate https://dev.lusidity.com/files/checksum-bin.md5 -O $ATHENA_DIR/checksum-bin.md5
echo "Validating File"
if md5sum -c $ATHENA_DIR/checksum-bin.md5; then
    echo "File is valid."
    mv bin $BK_NAME
    unzip bin.zip
    cp -rf $BK_NAME/resource bin
    echo
    read -p "Do you want to start Athena (y/n)? " -n 1 -r
    echo
    if [[ $REPLY =~ ^[Yy]$ ]]
    then
        sh app-init-d.sh start
    fi
else
    echo "File is not valid"
fi
