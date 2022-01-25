#!/bin/sh
if [[ -z "$DATABASE_URL" ]]; then
   echo "No values passed for database url"
else
    JAVA_OPTS="$JAVA_OPTS -Dspring.datasource.url=$DATABASE_URL"
fi
if [[ -z "$DATABASE_USERNAME" ]]; then
   echo "No values passed for database username"
else
    JAVA_OPTS="$JAVA_OPTS -Dspring.datasource.username=$DATABASE_USERNAME"
fi
if [[ -z "$DATABASE_PASSWORD" ]]; then
   echo "No values passed for database password"
else
    JAVA_OPTS="$JAVA_OPTS -Dspring.datasource.password=$DATABASE_PASSWORD"
fi
exec java ${JAVA_OPTS} \
    -Djava.security.egd=file:/dev/./urandom \
    -jar \
    -stock-financing-app.jar
