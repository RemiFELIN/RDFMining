# The aim of this file is to setup an complete environment to launch RDFMiner tool
# Get maven:3-jdk-11 image from Docker Hub
FROM maven:3-jdk-11
# Setup RDFMiner version
# ENV RDFMINER_VERSION=1.4
# Setup HOME environment variable
# ENV HOME=/rdfminer/
# Update
RUN apt-get update
# Install gcc 
RUN apt install gcc -y
RUN echo "\n"$(gcc --version)"\n"
# copy all project in HOME folder in image to build
COPY . $HOME
# Compile c code by running "compile_c_code" shell code
RUN echo -n "Compile rdfminer_entity_Entity.so ... " && sh $HOME/scripts/compile_c_code.sh && echo "Done."
# Install SPIN 2.0.0 (from http://topquadrant.com/repository/spin/)
# echo "\n********** Install SPIN 2.0.0 ..." && \
RUN mvn install:install-file \
        -Dfile=/rdfminer/dep/org/topbraid/spin/2.0.0/spin-2.0.0.jar \
        -DgroupId=org.topbraid \
        -DartifactId=spin \
        -Dversion=2.0.0 \
        -Dpackaging=jar \
        -DgeneratePom=true \
        --quiet
# run command 'mvn clean install' to build RDFMiner jar 
RUN cd $HOME/code && mvn clean install
# Uninstall GCC and clean
RUN apt-get remove gcc -y
RUN apt autoremove -y
RUN apt-get clean
# chmod 777 run.sh file
RUN chmod +x $HOME/scripts/run.sh
# launch
ENTRYPOINT ["./rdfminer/scripts/run.sh"]
