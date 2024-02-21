FROM openjdk:17-jdk-slim
MAINTAINER SUMIT CHOUKSEY "sumit@planbow.com"
COPY /target/planbow-api.jar /app.jar
EXPOSE 80
ENTRYPOINT ["java","-Djava.security.egd=file:/dev/./urandom","-jar","app.jar"]
