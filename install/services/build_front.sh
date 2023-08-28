#!/bin/bash
echo $(date +"%Y-%m-%d %H:%M:%S,%3N")" [install.sh] INFO - Build RDFMiner front ..."
docker-compose build front
echo $(date +"%Y-%m-%d %H:%M:%S,%3N")" [install.sh] INFO - Done !"