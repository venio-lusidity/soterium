#!/bin/bash
WEB_APP="/mnt/xdata/work/projects/soterium/jetty/webapps/root"
CP_DIR="/mnt/xdata/temp/release"
R_DIR="/mnt/xdata/temp/release/root"
YUI_DIR="/mnt/xdata/work/projects/soterium/tools"
f=/mnt/xdata/work/projects/soterium/jetty/webapps/root/pages/test/table/js/start.js
process=0

rm -rf $CP_DIR/*
cp -Rf $WEB_APP $CP_DIR

echo
echo "minifying JavaScript files"
echo
echo $f
cd $R_DIR
java -jar $YUI_DIR/yuicompressor-2.4.8.jar $f -o test.js --charset utf-8 --nomunge --preserve-semi --disable-optimizations

#resource/minify_one.sh
#https://www.w3schools.com/js/js_reserved.asp