FROM eclipse-temurin:17-jre-alpine

ENV BASE_DIR=/opt/skltp \
    USER=ind-app \
    LOG_DIR=/var/log/ \
    TZ=Europe/Stockholm

ADD target/skltp-ei-backend-*-exec.jar ${BASE_DIR}/app.jar
RUN mkdir -p ${LOG_DIR} \
 && adduser -DH --home ${BASE_DIR} -u 1000 ${USER} \
 && chown ${USER}:${USER} -R ${BASE_DIR} ${LOG_DIR}

WORKDIR ${BASE_DIR}
USER ${USER}
CMD java -XX:MaxRAMPercentage=75 ${JAVA_OPTS} -jar app.jar