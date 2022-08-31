#!/bin/bash
echo $(date +"%Y-%m-%d %H:%M:%S,%3N")" [install.sh] INFO - Build RDFMiner ..."
docker-compose build rdfminer
echo $(date +"%Y-%m-%d %H:%M:%S,%3N")" [install.sh] INFO - Done !"