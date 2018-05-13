FROM openjdk:8

USER root
WORKDIR /root

RUN mkdir /root/storage
COPY target/scala-2.12/simplexportal-0.1.0-SNAPSHOT.jar /root
EXPOSE 8080

VOLUME ["/root/storage"]

ENTRYPOINT ["java", "-Dsimplex.core.storage=/root/storage", "-jar", "/root/simplexportal-0.1.0-SNAPSHOT.jar"]