FROM node:18

# define workspace
ENV APP=/app
RUN mkdir $APP
# WORKDIR $APP

# installing dependencies
COPY package*.json $APP/
COPY ./public   $APP/public/
COPY ./src      $APP/src/

RUN cd $APP && npm install
COPY *.js       $APP/

# building project
RUN cd $APP && npm run build

# expose port
EXPOSE 8080

COPY ./run.sh   $APP/

ENV LAUNCH=$APP/run.sh
# enable exec
# RUN pwd
RUN chmod +x $LAUNCH

# RUN ls -al /app/

ENTRYPOINT ["./app/run.sh"]