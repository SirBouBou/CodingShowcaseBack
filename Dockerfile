FROM maven:3.9.9-eclipse-temurin-17-focal AS buildstage

WORKDIR /app

COPY . .

RUN mvn package

FROM openjdk:17 AS runstage

COPY --from=buildstage /app/target/CodingShowcaseBack-1.0-SNAPSHOT.jar .

EXPOSE 9966

RUN adduser --system spring

USER spring

ENTRYPOINT ["java", "-jar", "CodingShowcaseBack-1.0-SNAPSHOT.jar"]