#!/bin/bash
echo $(date +"%Y-%m-%d %H:%M:%S,%3N")" [install.sh] INFO - Build Virtuoso ..."
docker-compose build virtuoso
echo $(date +"%Y-%m-%d %H:%M:%S,%3N")" [install.sh] INFO - Done !"