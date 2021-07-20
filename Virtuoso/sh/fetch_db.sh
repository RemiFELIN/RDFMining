#!/bin/bash
echo $(date +"%Y-%m-%d %H:%M:%S,%3N")" [fetch_db.sh] INFO - running the server ..."
# run the server
virtuoso-t -f -c $VIRTUOSO/virtuoso.ini &
sleep 20
# echo $(date +"%Y-%m-%d %H:%M:%S,%3N")" [fetch_db.sh] INFO - OK !"
echo $(date +"%Y-%m-%d %H:%M:%S,%3N")" [fetch_db.sh] INFO - status: "
isql-v 1111 dba dba VERBOSE=OFF exec="status();"
echo $(date +"%Y-%m-%d %H:%M:%S,%3N")" [fetch_db.sh] INFO - load db directory on server..."
isql-v 1111 dba dba VERBOSE=OFF exec="ld_dir('/virtuoso/data', '*.nt', 'http://dbpedia.org');"
echo $(date +"%Y-%m-%d %H:%M:%S,%3N")" [fetch_db.sh] INFO - check load_list"
# Get the number of lines which ll_state is 2 (loaded data)
# expected : 0
isql-v 1111 dba dba VERBOSE=OFF $VIRTUOSO/sql/get_len_loaded_list.sql
echo $(date +"%Y-%m-%d %H:%M:%S,%3N")" [fetch_db.sh] INFO - Launch bulkload..."
# Run bulk load without logs to speed the loading of data
# Documentation : vos.openlinksw.com/owiki/wiki/VOS/VirtBulkRDFLoader
isql-v 1111 dba dba VERBOSE=OFF exec="log_enable(3,1);"
isql-v 1111 dba dba VERBOSE=OFF exec="rdf_loader_run();"
# echo $(date +"%Y-%m-%d %H:%M:%S,%3N")" [fetch_db.sh] INFO - OK !"
echo $(date +"%Y-%m-%d %H:%M:%S,%3N")" [fetch_db.sh] INFO - check load_list"
isql-v 1111 dba dba VERBOSE=OFF $VIRTUOSO/sql/get_len_loaded_list.sql
# expected : 517
echo $(date +"%Y-%m-%d %H:%M:%S,%3N")" [fetch_db.sh] INFO - Number of triples"
isql-v 1111 dba dba VERBOSE=OFF $VIRTUOSO/sql/get_count_triples.sql
# Make a checkpoint to ensure that data is persited
echo $(date +"%Y-%m-%d %H:%M:%S,%3N")" [fetch_db.sh] INFO - starting checkpoint ..."
isql-v 1111 dba dba exec="checkpoint;"
echo $(date +"%Y-%m-%d %H:%M:%S,%3N")" [fetch_db.sh] INFO - installing DBPedia DAV and RDF Mappers DAV ..."
mv /usr/local/virtuoso-opensource/share/virtuoso/vad/* /virtuoso/vads
isql-v 1111 dba dba exec="vad_install('/virtuoso/vads/dbpedia_dav.vad', 0); vad_install('/virtuoso/vads/rdf_mappers_dav.vad', 0);"
echo $(date +"%Y-%m-%d %H:%M:%S,%3N")" [fetch_db.sh] INFO - Done"
