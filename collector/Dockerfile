FROM gradle:4.10.0-jdk8-alpine AS build
COPY --chown=gradle:gradle ./src /home/gradle/src
COPY --chown=gradle:gradle ./lib /home/gradle/lib
COPY  --chown=gradle:gradle ./build.gradle /home/gradle/build.gradle
WORKDIR /home/gradle

RUN mv src/main/resources/application.docker.properties   src/main/resources/application.properties
# change config to use docker config ( not best practice as spring cloud support external config server)
RUN gradle build --no-daemon

FROM openjdk:8-jre-slim


RUN mkdir /app
RUN apt-get update
RUN apt-get install -y iputils-ping


COPY --from=build /home/gradle/build/libs/*.jar /app/spring-boot-application.jar
COPY --from=build /home/gradle/src/main/resources/application.properties /app/application.properties

ENTRYPOINT ["java", "-XX:+UnlockExperimentalVMOptions", "-XX:+UseCGroupMemoryLimitForHeap", "-Djava.security.egd=file:/dev/./urandom","-jar","/app/spring-boot-application.jar"]