FROM openjdk:17-jdk-alpine
COPY . .
RUN ./gradlew build -x test --no-daemon

FROM openjdk:17-jdk-alpine
MAINTAINER example.com
COPY build/libs/demo-0.0.1-SNAPSHOT.jar demo-0.0.1-SNAPSHOT.jar
ENTRYPOINT ["java","-jar","demo-0.0.1-SNAPSHOT.jar"]
