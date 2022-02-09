RDFMINING_VERSION=1.4
CORESE_VERSION=4.3.0
echo $(date +"%Y-%m-%d %H:%M:%S,%3N")" [run.sh] INFO - Installing RDFMining         v$RDFMINING_VERSION"
# Generate package from Corese 4.1.1 source code
echo $(date +"%Y-%m-%d %H:%M:%S,%3N")" [run.sh] INFO - Packaging Corese             v$CORESE_VERSION"
cd Corese/code/corese/ && mvn -Dmaven.test.skip=true package -q
# Import Corese-rdf4j and Corese-core JAR in RDFMiner project
echo $(date +"%Y-%m-%d %H:%M:%S,%3N")" [run.sh] INFO - Extract Corese-core          v$CORESE_VERSION"
mv -f corese-core/target/corese-core-$CORESE_VERSION-jar-with-dependencies.jar ../../../RDFMiner/dep/corese-jar/corese-core/$CORESE_VERSION/corese-core-$CORESE_VERSION.jar
echo $(date +"%Y-%m-%d %H:%M:%S,%3N")" [run.sh] INFO - Extract Corese-rdf4j         v$CORESE_VERSION"
mv -f corese-rdf4j/target/corese-rdf4j-$CORESE_VERSION.jar ../../../RDFMiner/dep/corese-jar/corese-rdf4j/$CORESE_VERSION/
# Import Corese-server in Corese jar folder
echo $(date +"%Y-%m-%d %H:%M:%S,%3N")" [run.sh] INFO - Extract Corese-server        v$CORESE_VERSION"
mv -f corese-server/target/corese-server-$CORESE_VERSION-jar-with-dependencies.jar ../../jar/corese-server-$CORESE_VERSION.jar
# Build architecture using docker-compose
echo $(date +"%Y-%m-%d %H:%M:%S,%3N")" [run.sh] INFO - Extract Corese-server        v$CORESE_VERSION"
cd ../../.. && \
docker-compose build