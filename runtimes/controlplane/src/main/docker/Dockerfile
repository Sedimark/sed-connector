# Lightweight base image with JRE
FROM eclipse-temurin:24-jre-alpine-3.20

# Optional JVM arguments, such as memory settings
ARG JVM_ARGS=""
ARG JAR

# Install curl for healthcheck
RUN apk --no-cache add curl

# Set working directory
WORKDIR /app

# Copy the pre-built JAR
COPY ${JAR} sed-conn-controlplane.jar

# Expose HTTP port
EXPOSE 8080

ENV WEB_HTTP_PORT="8080" \
    WEB_HTTP_PATH="/api" \
    LOG_LEVEL=info \
    ENV_JVM_ARGS=$JVM_ARGS

HEALTHCHECK --interval=5s --timeout=5s --retries=10 CMD curl --fail http://localhost:${WEB_HTTP_PORT}${WEB_HTTP_PATH}/check/health

# need the sh -c syntax so that the SECRETS variable gets expanded
# use the "exec" syntax so that SIGINT reaches the JVM -> graceful termination
#CMD ["sh", "-c", "exec java -Dedc.fs.config=/app/configuration.properties -Djava.util.logging.config.file=/app/logging.properties -Djava.security.egd=file:/dev/urandom -jar sed-conn-controlplane.jar --log-level=debug"]
CMD ["sh", "-c", "exec java ${ENV_JVM_ARGS} -Djava.security.egd=file:/dev/urandom -jar sed-conn-controlplane.jar --log-level=${LOG_LEVEL}"]
