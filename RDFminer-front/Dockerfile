FROM node:18

RUN mkdir $APP
# installing dependencies
COPY package*.json $APP/
COPY ./public   $APP/public/
COPY ./src      $APP/src/
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