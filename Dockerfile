FROM openjdk:14-jdk-alpine

ENV HTTP_PORT=8080
ENV HTTPS_PORT=8443
ENV LAS2PEER_PORT=9011

RUN apk add --update bash mysql-client apache-ant tzdata curl && rm -f /var/cache/apk/*
RUN addgroup -g 1000 -S las2peer && \
    adduser -u 1000 -S las2peer -G las2peer

COPY --chown=las2peer:las2peer . /src
WORKDIR /src

# run the rest as unprivileged user
USER las2peer
RUN dos2unix gradlew
RUN dos2unix /src/docker-entrypoint.sh
RUN chmod +x gradlew && ./gradlew build --exclude-task test
RUN dos2unix /src/docker-entrypoint.sh
#RUN dos2unix /src/las2peer-akg/etc/ant_configuration/service.properties

EXPOSE $HTTP_PORT
EXPOSE $HTTPS_PORT
EXPOSE $LAS2PEER_PORT
ENTRYPOINT ["/src/docker-entrypoint.sh"]