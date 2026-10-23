# Stage 1: Build the WAR
FROM maven:3.9-eclipse-temurin-25 AS build
WORKDIR /app
COPY pom.xml .
COPY Servelet1ano/src/main ./src/main
RUN mvn clean package -DskipTests

# Stage 2: Run on Tomcat (Jakarta Servlet), served at the root context
FROM tomcat:11.0-jre25-temurin
RUN rm -rf /usr/local/tomcat/webapps/*
COPY --from=build /app/target/*.war /usr/local/tomcat/webapps/ROOT.war
EXPOSE 8080
CMD ["catalina.sh", "run"]
