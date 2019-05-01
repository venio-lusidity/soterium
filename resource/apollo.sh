#!/bin/bash
# chkconfig: 2345 20 80
# description: Start one or all of the following Jetty, ElasticSearch and/or OrientDB.

# Source function library.
. /etc/init.d/functions

# Place this file in /etc/init.d
# run the below commands

# copy script
# $ sudo cp apollo.sh /etc/init.d/soterium_apollo

# Enable the script
# $ cd /etc/init.d
# $ sudo chkconfig --add soterium_apollo
# $ sudo chkconfig --level 2345 soterium_apollo on

# Check the script is indeed enabled - you should see "on" for the levels you selected.
# $ chkconfig --list | grep soterium_apollo

# 0 = true; 1 = false;

REDIS_DIR="/mnt/xdata/datastores/redis_cache/data";

ES_BK_DIR="/mnt/xdata/datastores/backup/apollo_i"
ES_DIR="/mnt/xdata/datastores/elasticsearch-2.4.1"
ES_NAME=$ES_DIR.jar
ES_ON=0

export JETTY_HOME="/mnt/xdata/work/projects/soterium/jetty"
JETTY_ON=1

running=1

restore(){
    clear

    if [ $ES_ON = 0 ]; then
        echo -e "\nRestoring ElasticSearch backup..."
        cp -r  $ES_BK_DIR/* $ES_DIR/data/
    fi

    echo -e "\nRestore completed."
}

backup(){
    clearBackups

    if [ $ES_ON = 0 ]; then
        echo -e "\nBacking up ElasticSearch..."
        cp -r  $ES_DIR/data/* $ES_BK_DIR/
    fi

    echo -e "\nBackup completed."
}

clear(){
    stop

    echo -e "Apollo is no longer running."

    if [ $ES_ON = 0 ]; then
        echo -e "\nDeleting ElasticSearch databases..."
        rm -rf $ES_DIR/data/*
    fi

    echo -e "\nOrientDB and ElasticSearch data stores have been deleted."
}

clearBackups(){
    stop

    echo -e "Apollo is no longer running."

    if [ $ES_ON = 0 ]; then
        echo -e "\nDeleting ElasticSearch backups..."
        rm -rf $ES_BK_DIR/*
    fi

    echo -e "\nOrientDB and ElasticSearch backups have been deleted."
}

start(){
    echo -e "\n"

    if [ $ES_ON = 0 ]; then
        echo
        echo -e "Clearing logs for ElasticSearch..."
        rm -rf $ES_DIR/logs/*
        rm -rf $ES_DIR/bin/nohup.out

        echo
        echo -e "Starting ElasticSearch..."
        cd $ES_DIR/bin
        sh elasticsearch -d

        while [ ${running} = 1  ]
        do
             isRunning $ES_DIR
        done

        echo -e "\nElasticsearch is running\n"
    fi

    if [ $JETTY_ON = 0 ]; then
        echo -e "\nStarting Jetty..."
        cd $JETTY_HOME/bin
        sh jetty.sh start
    fi

    echo -e "\nApollo has been started."
}

stop(){

    if [ $ES_ON = 0 ]; then
        echo
        isRunning $ES_DIR
        if [ ${running} = 0 ]; then
            echo -e "Flushing the translog..."
            echo
            timeOut=10;
            while [[ "$(curl -s -o /dev/null -w ''%{http_code}'' -XPOST http://es-1.lusidity.com:9222/_flush?wait_for_ongoing)" != "200" ]]
            do
                if [ ${timeOut} -lt 1 ]
                then
                    break;
                fi
                timeOut=$(($timeOut-5))
                echo "Timeout in $timeOut"
                sleep 5
            done
            echo
            echo -e "Stopping ElasticSearch"
            pkill -ef "Elasticsearch start"
            while [ ${running} = 0 ]
            do
                isRunning $ES_DIR
            done
            echo
            echo -e "Elasticsearch is no longer running\n"
         fi
    fi

    if [ $JETTY_ON = 0 ]; then
        cd $JETTY_HOME/bin
        sh jetty.sh stop
    fi

    echo -e "\nApollo has been stopped"
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

status(){

    isRunning $ES_DIR
    if [ $ES_ON = 0 ]; then
        echo -e "\nElasticSearch is enabled\n"
    else
        echo -e "\nElasticSearch is not enabled\n"
    fi
    isRunning jetty
    if [ $JETTY_ON = 0 ]; then
        echo -e "\nJetty is enabled\n"
    else
        echo -e "\nJetty is not enabled\n"
    fi
}

case "$1" in
    backup)
       backup
       ;;
    restore)
       restore
       ;;
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