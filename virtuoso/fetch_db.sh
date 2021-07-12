#!/bin/bash
echo $(date +"%Y-%m-%d %H:%M:%S,%3N")" [fetch_db.sh] INFO - running the server ..."
# run the server
virtuoso-t -f -c /usr/local/virtuoso-opensource/var/lib/virtuoso/db/virtuoso.ini &
sleep 20
echo $(date +"%Y-%m-%d %H:%M:%S,%3N")" [fetch_db.sh] INFO - OK !"
echo $(date +"%Y-%m-%d %H:%M:%S,%3N")" [fetch_db.sh] INFO - status: "
isql-v 1111 dba dba VERBOSE=OFF 'EXEC=status()'
echo $(date +"%Y-%m-%d %H:%M:%S,%3N")" [fetch_db.sh] INFO - load db directory on server..."
isql-v 1111 dba dba VERBOSE=OFF "EXEC=ld_dir('${DBPEDIA}db', '*.nt', 'http://dbpedia.org')"
echo $(date +"%Y-%m-%d %H:%M:%S,%3N")" [fetch_db.sh] INFO - check load_list"
isql-v 1111 dba dba VERBOSE=OFF $DBPEDIA/sql/get_len_loaded_list.sql
echo $(date +"%Y-%m-%d %H:%M:%S,%3N")" [fetch_db.sh] INFO - Launch bulkload..."
isql-v 1111 dba dba VERBOSE=OFF "EXEC=rdf_loader_run()"
echo $(date +"%Y-%m-%d %H:%M:%S,%3N")" [fetch_db.sh] INFO - OK !"
echo $(date +"%Y-%m-%d %H:%M:%S,%3N")" [fetch_db.sh] INFO - check load_list"
isql-v 1111 dba dba VERBOSE=OFF $DBPEDIA/sql/get_len_loaded_list.sql