# Load the latest image of Virtuoso
FROM tenforce/virtuoso:virtuoso7.2.5

ENV VIRTUOSO=/virtuoso

RUN mkdir $VIRTUOSO 

# Copy all the project
COPY virtuoso.ini $VIRTUOSO
COPY ./db $VIRTUOSO/data
COPY ./sql $VIRTUOSO/sql
COPY ./scripts $VIRTUOSO/scripts
COPY ./vads $VIRTUOSO/vads

RUN chmod +x $VIRTUOSO/scripts/* && \
    # Run fetch_db script
    sh $VIRTUOSO/scripts/fetch_db.sh 

ENTRYPOINT ["/bin/bash", "/virtuoso/scripts/run.sh"]
