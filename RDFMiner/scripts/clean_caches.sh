#!/bin/bash
echo -n $(date +"%Y-%m-%d %H:%M:%S,%3N")" [run.sh] INFO - clean caches folder ..."
rm -f /rdfminer/caches/*
echo " Done !"
