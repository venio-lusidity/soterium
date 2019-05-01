#!/bin/bash
# clear the repo
rm -rf ~/.m2/repository/com/lusidity
rm -rf /mnt/xdata/work/projects/rmk/repo/com/lusidity
#clear working directory
rm -rf ../target/*

function clean(){
    rm -rf ../target/classes
    rm -rf ../target/generated-sources
    rm -rf ../target/maven-archiver
}
function package(){
   cd $1
      mvn clean install -Dmaven.test.skip=true -Dmaven.repo.local=/mnt/xdata/work/projects/rmk/repo -U
      code=$?
      if [ $code != 0 ]; then
        echo "MAVEN Code is $code"
        exit $code
      fi
      clean
}

package ../parent
package ../framework
package ../client-ssl
package ../core
package ../apollo
package ../discover
package ../services
package ../athena

rm -rf ~/.m2/repository/com/lusidity

echo "==================== done"
