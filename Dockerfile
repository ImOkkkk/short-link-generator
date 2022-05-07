FROM airdock/oraclejdk:1.8

ENV CODE /code
ENV WORK /code/work
RUN mkdir -p $CODE \
    && mkdir -p $WORK

WORKDIR $WORK
COPY ./target/*.jar app.jar

EXPOSE 6324
ENTRYPOINT ["java","-jar","app.jar"]
