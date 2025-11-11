FROM maven:3.9.9-eclipse-temurin-17 AS build
WORKDIR /workspace

COPY pom.xml ./

RUN mvn -B -q -DskipTests dependency:go-offline

COPY src ./src
RUN mvn -B -DskipTests package

FROM tomcat:10.1-jdk17-temurin
RUN rm -rf /usr/local/tomcat/webapps/ROOT

COPY --from=build /workspace/target/*.war /usr/local/tomcat/webapps/ROOT.war

ENV CATALINA_OPTS="-Dspring.profiles.active=prod"
ENV PORT=8080
EXPOSE 8080

HEALTHCHECK --interval=30s --timeout=5s --start-period=20s CMD \
  wget -qO- http://localhost:8080/actuator/health | grep '"status":"UP"' || exit 1
