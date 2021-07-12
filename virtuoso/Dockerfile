# Load the latest image of Virtuoso
FROM tenforce/virtuoso:latest

ENV DBPEDIA=/dbpedia_minimized/

# Copy all the project on 'dbpedia_minimized' folder
COPY . $DBPEDIA
COPY virtuoso.ini /usr/local/virtuoso-opensource/var/lib/virtuoso/db/

RUN chmod +x $DBPEDIA/fetch_db.sh && \
    # Run fetch_db script
    sh $DBPEDIA/fetch_db.sh

# ENTRYPOINT ["bin/bash", "isql-v"]
