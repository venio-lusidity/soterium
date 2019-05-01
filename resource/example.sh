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

start() {
    # code to start app comes here 
    # example: daemon program_name &
}

stop() {
    # code to stop app comes here 
    # example: killproc program_name
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
       # code to check status of app comes here 
       # example: status program_name
       ;;
    *)
       echo "Usage: $0 {start|stop|status|restart}"
esac

exit 0 