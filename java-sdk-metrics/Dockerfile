FROM openjdk:17-ea-3-oracle
EXPOSE 8891
COPY target/libs/*.jar /libs/
COPY target/app.jar app.jar
ENTRYPOINT ["java","-jar","/app.jar"]