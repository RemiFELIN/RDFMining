# Load the latest image of Virtuoso
FROM tenforce/virtuoso:virtuoso7.2.5

ENV VIRTUOSO=/virtuoso

RUN mkdir $VIRTUOSO 

# Copy all the project
COPY virtuoso.ini $VIRTUOSO
COPY ./db $VIRTUOSO/data
COPY ./sql $VIRTUOSO/sql
COPY ./sh $VIRTUOSO/sh
COPY ./vads $VIRTUOSO/vads

RUN chmod +x $VIRTUOSO/sh/* && \
    # Run fetch_db script
    sh $VIRTUOSO/sh/fetch_db.sh 

ENTRYPOINT ["/bin/bash", "/virtuoso/sh/run.sh"]
