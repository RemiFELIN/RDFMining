FROM node:18

# Import env variables
ARG APP
ARG RDFMINER_FRONT_PORT
ARG RDFMINER_FRONT_ENDPOINT
ARG RDFMINER_FRONT_SOCKET_ENDPOINT
ENV RDFMINER_FRONT_PORT=$RDFMINER_FRONT_PORT
ENV RDFMINER_FRONT_ENDPOINT=$RDFMINER_FRONT_ENDPOINT
ENV RDFMINER_FRONT_SOCKET_ENDPOINT=$RDFMINER_FRONT_SOCKET_ENDPOINT
# build app...
RUN mkdir $APP
# installing dependencies
COPY package*.json $APP/
COPY ./public   $APP/public/
COPY ./src      $APP/src/
COPY ./node_modules $APP/node_modules/
RUN cd $APP && npm install
COPY *.js       $APP/
# expose port
EXPOSE $RDFMINER_FRONT_PORT
# copy run.sh
COPY ./run.sh   $APP/
ENV LAUNCH=$APP/run.sh
# enable exec
# RUN pwd
RUN chmod +x $LAUNCH
# launcher
ENTRYPOINT ["./app/run.sh"]