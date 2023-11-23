#!/bin/bash

VERSION=4.4.1
CORESE=/usr/local/corese
JAR=$CORESE/corese-server-$VERSION.jar
PROFILE=$CORESE/config/corese-profile.ttl
PROPERTIES=$CORESE/config/corese-properties.ini
DEFAULT_PROFILE=$CORESE/config/corese-default-profile.ttl
SPARQL_ENDPOINTS=$CORESE/config/endpoints.txt

LOG4J=file://$CORESE/log4j2.xml
DATA=$CORESE/data
LOG=$CORESE/log/corese-server.log

mkdir -p $DATA $CORESE/log $CORESE/config


# Generate the instructions for loading all RDF files from folder "data"
# Supported extensions: .ttl .jsonld .rdf .csv .tsv .html (for rdfa)
# Parameters:
#   $1: absolute path where to look for files
function genLoadData() {
	path=$1
    cd $path
    echo "Looking for data files in: $path" >> $LOG
    for file in $(ls *); do
        echo "  [ a sw:Load; sw:path <$path/$file> ]" >> $PROFILE
    done
}

echo "======================================================================" >> $LOG

# function genCoreseDefaultProfile() {
echo "Generate corese-default-profile.ttl ..." >> $LOG
if [ -f "$PROFILE" ]; then
    rm $PROFILE
fi
touch $PROFILE
echo "# Content available in the default dataset at /sparql" > $PROFILE
echo "st:user a st:Server; st:content st:loadcontent ." >> $PROFILE
# echo "# List endpoints allowed in federated queries + where STTL is allowed to get html templates" >> $PROFILE
# echo -n "st:access st:namespace <http://localhost:9200/sparql> , <http://134.59.130.136:8890/sparql> , <http://corese:9100/sparql>" >> $PROFILE
if [ -f "$SPARQL_ENDPOINTS" ]; then 
    echo "SPARQL Endpoint provided ! set SPARQL Endpoint !" >> $LOG
    # read file in input
    while read endpoint || [ -n "$endpoint" ]; do
        echo -n " , $endpoint" >> $PROFILE
    done < $SPARQL_ENDPOINTS
    # for endpoint in $@; do
    #     echo -n " , <$endpoint>" >> $DEFAULT_PROFILE
    # done
    # echo " ." >> $PROFILE
else
    echo "No additionnal SPARQL Endpoint provided ! set default SPARQL Endpoint ..." >> $LOG
    # echo " ." >> $PROFILE
fi
# }

# genCoreseDefaultProfile

# Check if JVM heap space if given in the env
# if [ -z "$JVM_XMX" ]; then
#     XMX=
# else
XMX=-Xmx12G
# fi
echo "JVM heap space option: $XMX" >> $LOG

# Check existing profile or create a new one
# if [ -f "$PROFILE" ]; then
#     echo "Using user-defined profile." >> $LOG
# else
# Prepare the Corese profile for loading "data/*"
echo "Creating new profile." >> $LOG
# cat $DEFAULT_PROFILE > $PROFILE
echo "st:loadcontent a sw:Workflow; sw:body (" >> $PROFILE
genLoadData "$DATA"
echo ').' >> $PROFILE
echo '' >> $PROFILE
# fi
echo "Corese profile:" >> $LOG
cat $PROFILE  >> $LOG


# Check existing properties file or create a new one
if [ -f "$PROPERTIES" ]; then
    echo "Using user-defined properties file." >> $LOG
else
    echo "Creating new properties file." >> $LOG
    cp $CORESE/corese-default-properties.ini $PROPERTIES
fi


# Start Corese with the profile
cd $CORESE
java \
    $XMX \
    -Dfile.encoding="UTF-8" \
    -Dlog4j.configurationFile=$LOG4J \
    -jar $JAR \
    -p 9100 \
    -lp \
    -pp file://$PROFILE \
    -init $PROPERTIES \
    # -su # super user

