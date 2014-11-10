#!/usr/bin/env bash
source ../variables.sh
INSTANCES=$(ps aux | grep $SERVERID_P4 | grep -v grep | wc -l)

if [ "$INSTANCES" -eq "0" ]
then
java -classpath resourcemonitor-jar-with-dependencies.jar -Djava.library.path="../lib" com.resourcemonitor.daemon.MonitorDaemon $SERVERID_P4
else
echo "Server already running"
fi

