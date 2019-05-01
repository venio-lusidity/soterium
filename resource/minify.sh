#!/bin/bash
WEB_APP="/mnt/xdata/work/projects/soterium/jetty/webapps/root"
CP_DIR="/mnt/xdata/temp/release"
R_DIR="/mnt/xdata/temp/release/root"
SKIP=("charts.min.js" "chart.bundle.min.js")
JS_DIRS=("pages" "assets/js" "assets/lusidity")
CSS_DIRS=("pages" "assets/js/form/css" "assets/js/loaders/css" "assets/lusidity/css")
YUI_DIR="/mnt/xdata/work/projects/soterium/tools"
process=0

rm -rf $CP_DIR/*
cp -Rf $WEB_APP $CP_DIR

echo
echo "minifying JavaScript files"
echo
cd $R_DIR
for i in "${JS_DIRS[@]}"
do
    echo
    echo "********************************************"
    echo "minifying JavaScript files in $i"
    for f in $(find $i -name '*.js')
    do
        process=0
        echo "processing $f"
        for s in "${SKIP[@]}"
        do
            if [[ $f == *$s ]]
            then
              echo "setting file for skip $f"
              process=1
              break
             fi
        done
        if [ $process = 0 ]
        then
            echo $f
            mv -f $f $f.tmp.js
            rm -f $f
            java -jar $YUI_DIR/yuicompressor-2.4.8.jar $f.tmp.js -o $f --charset utf-8 --nomunge --preserve-semi --disable-optimizations
            rm -f $f.tmp.js
        else
            echo "keeping file as is $f"
        fi
    done
done

echo
for i in "${JS_DIRS[@]}"
do
    echo
    echo "********************************************"
    echo "minifying CSS files in $i"
    for f in $(find $i -name '*.css')
    do
        echo $f
        mv -f $f $f.tmp.css
        rm -f $f
        java -jar $YUI_DIR/yuicompressor-2.4.8.jar $f.tmp.css -o $f --charset utf-8
        rm -f $f.tmp.css
    done
done

#./minify.sh > minify.out.txt
