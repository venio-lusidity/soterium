#!/bin/bash
# clear the repo
rm -rf ~/.m2/repository/com/lusidity
#clear working directory
rm -rf ../target/*
rm -rf ../jetty/webapps/root/files/*

sh minify.sh

# Package the Web components
cd /mnt/xdata/temp/release
zip -r root.zip root
mv root.zip /mnt/xdata/work/projects/soterium/jetty/webapps/root/files

cd /mnt/xdata/work/projects/soterium/resource

function clean(){
    rm -rf ../target/classes
    rm -rf ../target/generated-sources
    rm -rf ../target/maven-archiver
}
function package(){
   cd $1
      mvn install -Dmaven.test.skip=true
      code=$?
      clean
      if [ $code != 0 ]; then
        echo "MAVEN Code is $code"
        exit $code
      fi
}
function scope()
{
    cd $1
}

package ../parent
package ../framework
package ../client-ssl
package ../core
package ../apollo
#package ../mssql has been removed not used and has deprecated methods compilation errors.
package ../discover
package ../services
package ../[your app module name]
package ../acas-system-center
package ../athena

mkdir ../target/bin
cp ~/.m2/repository/com/lusidity/* ../target/bin/

# clear the repo
rm -rf ~/.m2/repository/com/lusidity

clean
mv ../target/*.jar ../target/bin/
mv ../target/*.pom ../target/bin/

cd ..
zip -r ../jetty/webapps/root/files/resource.zip resource
cd resource
cp -r ../lib/*.jar ../target/bin/
cp -r ../lib/em/*.jar ../target/bin/

version=1
build=$(date +%s)
timestamp=$(date +%Y-%h-%d-%H:%M:%S)
rm -f ../target/version.json
rm -f ../target/version.txt
echo "{\"version\": \"${version}-${build}\", \"build_date\": \"${timestamp}\"}" >> ../target/version.json
echo "version=${version}-${build}\nbuild.date=${timestamp}" >> ../target/version.txt

cp ../target/version.json ../jetty/webapps/root/assets/js/
mv ../target/version.json ../target/bin/
mv ../target/version.txt ../target/bin/

# Package the Athena components
cd ../target
zip -r ../jetty/webapps/root/files/bin.zip bin

DIR=../jetty/webapps/root/files
BINFILE=bin.zip
ROOTFILE=root.zip

cd $DIR
md5sum $BINFILE  > checksum-bin.md5
md5sum $ROOTFILE > checksum-root.md5
echo "==================== done"
