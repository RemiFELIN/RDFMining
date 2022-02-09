# The aim of this file is to setup an complete environment to launch RDFMiner tool

# Get Debian image from Docker Hub
FROM debian:stretch

# Setup RDFMiner version
ENV RDFMINER_VERSION=1.4
ENV CORESE_VERSION=4.3.0

# Setup HOME environment variable
ENV HOME=/rdfminer/

# copy all project in HOME folder in image to build
COPY . $HOME

# Install all dependencies
RUN echo "\n********** START: download dependencies \n" && \
    # Update
    apt-get update && \
    # Install gcc 
    echo "\n********** 1/3: GCC" && \
    apt install gcc -y && \
    # the -y option allow to accept all additionnals dependencies without tap "yes" on console
    # and catch errors while building phasis
    # Install Openjdk 8 : we use this jdk to run the maven build phasis and run rdfminer jar
    echo "\n********** 2/3: OPENJDK" && \
    apt-get install openjdk-8-jdk -y && \
    # Install maven
    echo "\n********** 3/3: MAVEN" && \
    apt-get --no-install-recommends install maven -y && \
    # Check all dependencies versions
    echo "\n********** FINISH: version control phase \n" && \
    echo "\n"$(gcc --version)"\n" && \
    echo "\n"$(java -version) && \
    echo $(mvn -v)"\n"

# define JAVA_HOME
ENV JAVA_HOME=/usr/lib/jvm/java-8-openjdk-amd64/

# Compile c code by running "compile_c_code" shell code
RUN echo "\n********** compile rdfminer_RDFMiner.so" && \
    sh $HOME/scripts/compile_c_code.sh && \
    echo "Done.\n"

# Install Corese-core and Corese-rdf4j on .m2 folder 
# Allow us to import customized JAR locally
RUN echo "\n********** Install Corese-core ..." && \
    mvn install:install-file \
        -Dfile=/rdfminer/dep/corese-jar/corese-core/$CORESE_VERSION/corese-core-$CORESE_VERSION.jar \
        -DgroupId=fr.inria.corese \
        -DartifactId=corese-core \
        -Dversion=$CORESE_VERSION \
        -Dpackaging=jar \
        -DgeneratePom=true \
        --quiet && \
    echo "Done.\n"

RUN echo "\n********** Install Corese-rdf4j ..." && \
    mvn install:install-file \
        -Dfile=/rdfminer/dep/corese-jar/corese-rdf4j/$CORESE_VERSION/corese-rdf4j-$CORESE_VERSION.jar \
        -DgroupId=fr.inria.corese \
        -DartifactId=corese-rdf4j \
        -Dversion=$CORESE_VERSION \
        -Dpackaging=jar \
        -DgeneratePom=true \
        --quiet && \
    echo "Done.\n"

# run command 'mvn clean install' to build jar of RDFMiner
RUN echo "\n********** MAVEN: install RDFMiner $RDFMINER_VERSION\n" && \
    cd $HOME/code && \
    mvn clean install
    
# Uninstall GCC and Maven
RUN apt-get remove gcc maven -y && \
    apt autoremove -y && \
    apt-get clean

# chmod 777 run.sh file
RUN chmod +x $HOME/scripts/run.sh

# ENTRYPOINT : run a run.sh file with jar's parameters in CMD
# ENTRYPOINT ["./run.sh"]
ENTRYPOINT ["./rdfminer/scripts/run.sh"]
