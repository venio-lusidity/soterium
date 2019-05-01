#!/bin/bash

echo "Setting up development environment…"

echo "Starting OrientDB…"
cd ~/work/bin/orientdb-community-1.7.9/bin
./orientdb.sh start
echo "Started OrientDB."

echo "Starting ElasticSearch…"
cd ~/work/bin/elasticsearch-1.3.2/bin
nohup ./elasticsearch &
echo "Started ElasticSearch."

# echo "Starting Jetty…"
# cd ~/work/projects/soterium/jetty
# java -jar start.jar &
# echo "Started Jetty."
