#!/bin/bash
echo $(date +"%Y-%m-%d %H:%M:%S,%3N")" [install.sh] INFO - Build Corese ..."
docker-compose build corese
echo $(date +"%Y-%m-%d %H:%M:%S,%3N")" [install.sh] INFO - Done !"