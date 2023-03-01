#!/bin/sh

# Current version of tools used
RDFMINING_VERSION=1.4
CORESE_VERSION=4.3.0
SPIN_VERSION=2.0.0

# Clean shell files
echo $(date +"%Y-%m-%d %H:%M:%S,%3N")" [install.sh] INFO - Formatting of shell scripts useful for the installation of the software ..."
echo $(date +"%Y-%m-%d %H:%M:%S,%3N")" [install.sh] INFO - 'Init' and 'Final' correspond to the number of lines of current file before and after formatting"
./clean_scripts.sh | column -t
# Install RDFMining architecture
echo $(date +"%Y-%m-%d %H:%M:%S,%3N")" [install.sh] INFO - Installing RDFMining         v$RDFMINING_VERSION"
# Auto-generate Dockerfile YML file
./generate_yml.sh
echo $(date +"%Y-%m-%d %H:%M:%S,%3N")" [install.sh] INFO - Done !"
# Generate package from Corese 4.1.1 source code
echo $(date +"%Y-%m-%d %H:%M:%S,%3N")" [install.sh] INFO - Clean and packaging Corese             v$CORESE_VERSION"
cd ./../Corese/src/corese/ && mvn clean && mvn -Dmaven.test.skip=true package
# Import Corese-core JAR in RDFMiner project
echo $(date +"%Y-%m-%d %H:%M:%S,%3N")" [install.sh] INFO - Extract Corese-core          v$CORESE_VERSION"
mkdir -p ../../../RDFMiner/dep/corese-jar/corese-core/$CORESE_VERSION/
mv -f corese-core/target/corese-core-$CORESE_VERSION-jar-with-dependencies.jar ../../../RDFMiner/dep/corese-jar/corese-core/$CORESE_VERSION/corese-core-$CORESE_VERSION.jar
# Import Corese-rdf4j JAR in RDFMiner project
echo $(date +"%Y-%m-%d %H:%M:%S,%3N")" [install.sh] INFO - Extract Corese-rdf4j         v$CORESE_VERSION"
mkdir -p ../../../RDFMiner/dep/corese-jar/corese-rdf4j/$CORESE_VERSION/
mv -f corese-rdf4j/target/corese-rdf4j-$CORESE_VERSION.jar ../../../RDFMiner/dep/corese-jar/corese-rdf4j/$CORESE_VERSION/corese-rdf4j-$CORESE_VERSION.jar
# Import Corese-server in Corese jar folder
echo $(date +"%Y-%m-%d %H:%M:%S,%3N")" [install.sh] INFO - Extract Corese-server        v$CORESE_VERSION"
mkdir -p ../../jar/
mv -f corese-server/target/corese-server-$CORESE_VERSION-jar-with-dependencies.jar ../../jar/corese-server-$CORESE_VERSION.jar
cd ../../..
# Download SPIN 2.0.0 from http://topquadrant.com/repository/spin/
SPIN_PATH="./RDFMiner/dep/org/topbraid/spin/2.0.0/"
if [ ! -d "$SPIN_PATH" ]; then 
    echo $(date +"%Y-%m-%d %H:%M:%S,%3N")" [install.sh] INFO - Download SPIN                v$SPIN_VERSION"
    mkdir -p "$SPIN_PATH"
    wget -P "$SPIN_PATH" https://archive.topquadrant.com/repository/spin/org/topbraid/spin/2.0.0/spin-2.0.0.jar
fi
# prepare shared folder
echo $(date +"%Y-%m-%d %H:%M:%S,%3N")" [install.sh] INFO - Create shared folders"
mkdir -p ./Corese/data/
mkdir -p ./Corese/log/
mkdir -p ./Virtuoso/data/
mkdir -p ./RDFMiner/caches/
# Build services
echo $(date +"%Y-%m-%d %H:%M:%S,%3N")" [install.sh] INFO - Build services ..."
# Read params provided by user
# if [ $# -eq 0 ]; then 
#     echo $(date +"%Y-%m-%d %H:%M:%S,%3N")" [install.sh] INFO - No parameters provided ..."
# else
#     params=("$@")
#     for index in ${!params[@]}; do
#         # Set SPARQL Endpoint provided by user to allow federated queries (using SERVICE)
#         if [ "${params[index]}" == "-e" ] || [ "${params[index]}" == "--endpoints" ]; then 
#             # Copy the file (which contains SPARQL Endpoints) into config/ folder from Corese
#             echo $(date +"%Y-%m-%d %H:%M:%S,%3N")" [install.sh] INFO - SPARQL Endpoint(s) provided ! Creating endpoints.txt ..."
#             cp ${params[index + 1]} Corese/config/endpoints.txt
#         fi
#     done
# fi
./install/services/build_rdfminer.sh
./install/services/build_corese.sh
./install/services/build_virtuoso.sh
