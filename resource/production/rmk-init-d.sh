#!/bin/bash
# chkconfig: 2345 20 80
# description: Description comes here....

#!/bin/bash
# chkconfig: 2345 20 80
# description: Start one or all of the following Jetty, ElasticSearch and/or OrientDB.

# Source function library.
. /etc/init.d/functions

# Place this file in /etc/init.d
# run the below commands

# copy script
# $ cp my_script.sh /etc/init.d/my_script

# Enable the script
# $ cd /etc/init.d
# $ chkconfig --add my_script
# $ chkconfig --level 2345 my_script on

# Check the script is indeed enabled - you should see "on" for the levels you selected.
# $ chkconfig --list | grep my_script


ATHENA_CONFIG="/mnt/xdata/opt/soterium/athena/bin/resource/config/rmk.json"
ATHENA_BIN_DIR="/mnt/xdata/opt/soterium/athena/bin"
ATHENA_JAR="lusidity-athena-2.0.jar"
ATHENA_NAME="lusidity-athena"
ATHENA_XS="2g"
ATHENA_XX="6g"
ATHENA_SERVER=true
ATHENA_CLEAR=false
running=1

usage() {
	echo -e "\nUsage: `basename $0`: <start|stop|update>"
	exit 1
}

isRoot(){
    if [ "$(id -u)" != "0" ]; then
       echo "This script must be run as root" 1>&2
       exit 1
    fi
}

isRunning(){
   running=1
   if ps ax | grep -v grep | grep $1 > /dev/null
    then
        echo "$1 service running, everything is fine"
        running=0
    else
        echo "$1 is not running"
        running=1
    fi
}

update(){
    isRoot
    echo "Type the full path to the update folder, followed by [ENTER]:"
    read DIRECTORY
    if [[ -d $DIRECTORY ]]; then
        echo "\nUpdating Athena..."
        stop
        cd $DIRECTORY
        cp -rf * $ATHENA_BIN_DIR
        start
    elif [[ -f $DIRECTORY ]]; then
        echo "$DIRECTORY is a file and a directory is required."
        exit 1
    else
        echo "$DIRECTORY is not valid directory."
        exit 1
    fi
}

status(){
   isRunning $ATHENA_NAME
}
start() {
    isRoot
	isRunning $ATHENA_NAME

    if [ ${running} = 0 ]
	then
	    echo -e "\nAthena is already running"
	    exit 1
    fi

    echo -e "\nStarting Athena..."
    cd $ATHENA_BIN_DIR
    if $ATHENA_SERVER;then
        echo "server mode"
        java -Xms$ATHENA_XS -Xmx$ATHENA_XX -jar $ATHENA_JAR -config $ATHENA_CONFIG -server $ATHENA_SERVER -clear $ATHENA_CLEAR &
    else
        echo "console mode"
        java -Xms$ATHENA_XS -Xmx$ATHENA_XX -jar $ATHENA_JAR -config $ATHENA_CONFIG -server $ATHENA_SERVER -clear $ATHENA_CLEAR
    fi

    echo -e "\Athena has been started."
}

stop() {
    isRoot
    echo -e "\nStopping Athena..."
    pkill -f $ATHENA_NAME
    while [ ${running} = 0 ]
	do
	     isRunning elasticsearch
    done
    echo -e "\nAthena has been stopped."
}

case "$1" in 
    start)
       start
       ;;
    stop)
       stop
       ;;
    restart)
       stop
       start
       ;;
    status)
       status
       ;;
    *)
       echo "Usage: $0 {start|stop|status|restart}"
esac

exit 0 
