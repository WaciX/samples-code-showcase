FROM adoptopenjdk/openjdk11:alpine-jre
VOLUME /tmp
ADD /build/libs/get-quote-for-loan-on-36-months-period-0.0.1-SNAPSHOT.jar app.jar
ENTRYPOINT ["java","-jar","app.jar"]