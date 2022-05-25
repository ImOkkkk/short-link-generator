FROM airdock/oraclejdk:1.8

ENV WORK /data
RUN mkdir -p $WORK

WORKDIR $WORK
COPY ./short-link-web/target/*.jar app.jar

EXPOSE 6324
ENTRYPOINT ["java","-jar","app.jar"]
