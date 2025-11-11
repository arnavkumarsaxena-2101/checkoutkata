FROM tomcat:10.1-jdk17-temurin
# Optional: remove default ROOT app so your app serves at /
RUN rm -rf /usr/local/tomcat/webapps/ROOT
# Copy your WAR as ROOT.war
COPY target/checkoutkata-1.0.0.war /usr/local/tomcat/webapps/ROOT.war

# Pass Spring profile to the JVM
ENV CATALINA_OPTS="-Dspring.profiles.active=prod"

EXPOSE 8080
