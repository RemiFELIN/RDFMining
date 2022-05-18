#!/bin/bash
CORESE=/usr/local/corese
PROFILE=$CORESE/config/corese-profile.ttl
# remove profile
echo -n $(date +"%Y-%m-%d %H:%M:%S,%3N")" [run.sh] INFO - remove corese-profile ..."
rm -f $PROFILE
echo " Done !"
