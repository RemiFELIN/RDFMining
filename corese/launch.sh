#!/bin/bash
###############################################
# prepare and launch corese server 
###############################################
echo $(pwd)/corese-server/corese-default-properties.ini
java -jar corese-server/target/corese-server-4.5.0.jar \
     -init $(pwd)/corese-server/build-docker/corese/corese-default-properties.properties \
     -p ${CORESE_PORT} \
     -su 
