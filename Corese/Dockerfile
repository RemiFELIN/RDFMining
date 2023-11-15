FROM openjdk:11.0.7-jdk-buster

RUN apt-get update -y
# RUN apt-get install -y wget

ENV VERSION=4.4.1
ENV CORESE=/usr/local/corese
RUN mkdir -p $CORESE
WORKDIR $CORESE

# RUN wget http://files.inria.fr/corese/distrib/corese-server-4.2.4.jar

COPY ./scripts/*.sh $CORESE/scripts/
COPY ./jar/corese-server-$VERSION.jar $CORESE/corese-server-$VERSION.jar

# Config server :
COPY ./config/log4j2.xml $CORESE/log4j2.xml
# COPY /config/corese-default-profile.ttl $CORESE/corese-default-profile.ttl
COPY ./config/corese-default-properties.ini $CORESE/corese-default-properties.ini

# chmod 777 script files
RUN chmod +x $CORESE/scripts/*.sh

# ENTRYPOINT : run a run.sh file with jar's parameters in CMD
# ENTRYPOINT ["./run.sh"]
ENV LAUNCH=$CORESE/scripts/run.sh
CMD $LAUNCH
