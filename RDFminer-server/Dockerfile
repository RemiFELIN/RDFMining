FROM node:18

# define workspace
ENV APP=/app
RUN mkdir $APP

# installing dependencies
COPY package*.json $APP/
COPY settings.json $APP/
COPY swagger.js $APP/
COPY app.js $APP/
COPY ./run.sh   $APP/

# copy code
COPY ./auth $APP/auth/
COPY ./data $APP/data/
COPY ./docker $APP/docker/
COPY ./logs $APP/logs/
COPY ./model $APP/model/
COPY ./servers $APP/servers/
COPY ./services $APP/services/
COPY ./tools $APP/tools/

# install
RUN cd $APP && npm install
RUN apt-get update && apt-get install redis -y

ENV LAUNCH=$APP/run.sh
# enable exec
RUN chmod +x $LAUNCH

RUN ls -al /app/

ENTRYPOINT ["./app/run.sh"]
