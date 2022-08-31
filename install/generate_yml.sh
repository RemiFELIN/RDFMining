#!/bin/bash

# Automatically generate a YML file to build the whole architecture using docker-compose command
# It will scan the workspace absolute path of the current machine and avoid any modification of it by users

# Define the YML file to generate IF IT DOES NOT EXISTS
YML_FILE=docker-compose.yml

# Edit YML File
function addLineToYML() {
    if [ "$#" -eq 2 ]; then
        for ((i = 1; i <= $1; i++ )); do
            echo -n ' ' >> $YML_FILE
        done
        echo -e $2 >> $YML_FILE
    else 
        echo -e $1 >> $YML_FILE
    fi
}

if [ ! -f "$(pwd)/../$YML_FILE" ]; then 
    echo $(date +"%Y-%m-%d %H:%M:%S,%3N")" [generate_yml.sh] INFO - Generate docker-compose.yml to build RDFMining ..."
    # get the absolute path of the workspace
    cd ..
    WORKSPACE_ABSOLUTE_PATH=$(pwd)/
    cd install/
    # echo $WORKSPACE_ABSOLUTE_PATH
    touch $YML_FILE
    # Define version of each services
    RDFMINER_VERSION=1.4
    VIRTUOSO_VERSION=7.2.5
    CORESE_VERSION=4.3.1
    # 
    #   EDIT docker-compose.yml
    # 
    addLineToYML "version: '3.8'"
    addLineToYML "services:"
    #
    #   RDFMiner service
    #
    addLineToYML 3 "   # The main service : RDFMiner v$RDFMINER_VERSION"
    addLineToYML 3 "   # developed by Andrea G. B. Tettamanzi, Thu Huong Nguyen and Rémi Felin"
    addLineToYML 3 "   rdfminer:"
    addLineToYML 6 "       restart: always"
    addLineToYML 6 "       image: rdfminer:"$RDFMINER_VERSION
    addLineToYML 6 "       build:"
    addLineToYML 9 "          context: ./RDFMiner/."
    addLineToYML 6 "       depends_on:"
    addLineToYML 9 "          - virtuoso"
    addLineToYML 9 "          - corese"
    addLineToYML 6 "       command: launch"
    addLineToYML 6 "       volumes:"
    addLineToYML 9 "          - "$WORKSPACE_ABSOLUTE_PATH"IO:/rdfminer/io"
    addLineToYML 6 "       networks:"
    addLineToYML 9 "          rdfmining_network:"
    addLineToYML 12 "               ipv4_address: 172.19.0.3"
    #
    #   Virtuoso service
    #
    addLineToYML 3 "   # OpenLink Virtuoso v"$VIRTUOSO_VERSION
    addLineToYML 3 "   # Used by RDFMiner to load and query a training dataset"
    addLineToYML 3 "   virtuoso:"
    addLineToYML 6 "       restart: always"
    addLineToYML 6 "       image: virtuoso:"$VIRTUOSO_VERSION
    addLineToYML 6 "       build:"
    addLineToYML 9 "          context: ./Virtuoso/."
    addLineToYML 6 "       ports:"
    addLineToYML 9 "          - '9000:9000'"
    addLineToYML 6 "       volumes:"
    addLineToYML 9 "          - "$WORKSPACE_ABSOLUTE_PATH"Virtuoso/data:/data"
    addLineToYML 6 "       networks:"
    addLineToYML 9 "          rdfmining_network:"
    addLineToYML 12 "               ipv4_address: 172.19.0.2"
    #
    #   Corese service
    #
    addLineToYML 3 "   # Corese v"$CORESE_VERSION
    addLineToYML 3 "   # Developped by Olivier Corby et al."
    addLineToYML 3 "   # modified by Rémi Felin in order to validate a SHACL Shape using probabilistic and possibilistic validation"
    addLineToYML 3 "   corese:"
    addLineToYML 6 "       restart: always"
    addLineToYML 6 "       image: corese:"$CORESE_VERSION
    addLineToYML 6 "       build:" 
    addLineToYML 9 "          context: ./Corese/."
    addLineToYML 6 "       ports:"
    addLineToYML 9 "          - '9100:9100'"
    addLineToYML 6 "       volumes:"
    addLineToYML 9 "          - "$WORKSPACE_ABSOLUTE_PATH"Corese/log:/usr/local/corese/log"
    addLineToYML 9 "          - "$WORKSPACE_ABSOLUTE_PATH"Corese/data:/usr/local/corese/data"
    addLineToYML 9 "          - "$WORKSPACE_ABSOLUTE_PATH"Corese/config:/usr/local/corese/config"
    addLineToYML 6 "       networks:"
    addLineToYML 9 "          rdfmining_network:"
    addLineToYML 12 "               ipv4_address: 172.19.0.4"
    #
    #   Define networks architecture
    # 
    addLineToYML "networks:"
    addLineToYML 3 "   rdfmining_network:"
    addLineToYML 6 "       ipam:"
    addLineToYML 9 "          config:"
    addLineToYML 12 "               - subnet: 172.19.0.0/24"

    # move file into workspace folder
    mv $YML_FILE $WORKSPACE_ABSOLUTE_PATH
    # Finish
    echo $(date +"%Y-%m-%d %H:%M:%S,%3N")" [generate_yml.sh] INFO - Done !"
    
else
    echo $(date +"%Y-%m-%d %H:%M:%S,%3N")" [run.sh] INFO - docker-compose.yml already exists ..."
fi
