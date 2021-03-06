FROM adoptopenjdk/openjdk12:jdk-12.0.2_10-alpine
EXPOSE 8080
VOLUME /tmp
ADD target/*.jar app.jar
COPY target/classes/saml/idp-metadata.xml idp-metadata.xml
ENV JAVA_OPTS=""
ENTRYPOINT [ "sh", "-c", "java $JAVA_OPTS -Djava.security.egd=file:/dev/./urandom -jar /app.jar" ]