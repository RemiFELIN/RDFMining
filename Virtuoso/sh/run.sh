#!/bin/bash
echo $(date +"%Y-%m-%d %H:%M:%S,%3N")" [run.sh] INFO - running the server ..."
# run the server
virtuoso-t -f -c $VIRTUOSO/virtuoso.ini +wait +foreground
