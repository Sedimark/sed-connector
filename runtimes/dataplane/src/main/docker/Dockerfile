# Lightweight base image with JRE
FROM eclipse-temurin:24-jre-alpine-3.20

# Optional JVM arguments, such as memory settings
ARG JVM_ARGS=""
ARG JAR

# Install curl for healthcheck
RUN apk --no-cache add curl postgresql-client

# Set working directory
WORKDIR /app

# Copy the pre-built JAR
COPY ${JAR} sed-conn-dataplane.jar

# Copy DB cleanup script
COPY db/clear-data-plane.sh clear-data-plane.sh
RUN chmod +x clear-data-plane.sh

# Expose HTTP port
EXPOSE 8080

# Environment variables
ENV WEB_HTTP_PORT="8080"
ENV WEB_HTTP_PATH="/api"
ENV LOG_LEVEL=info

HEALTHCHECK --interval=5s --timeout=5s --retries=10 CMD curl --fail http://localhost:${WEB_HTTP_PORT}${WEB_HTTP_PATH}/check/health

# Use "exec" for graceful termination (SIGINT) to reach JVM.
# ARG can not be used in ENTRYPOINT so storing value in an ENV variable
ENV ENV_JVM_ARGS=$JVM_ARGS
# need the sh -c syntax so that the SECRETS variable gets expanded
# use the "exec" syntax so that SIGINT reaches the JVM -> graceful termination
#CMD ["sh", "-c", "exec java -Dedc.fs.config=/app/configuration.properties -Djava.util.logging.config.file=/app/logging.properties -Djava.security.egd=file:/dev/urandom -jar sed-conn-dataplane.jar --log-level=debug"]
CMD ["sh", "-c", "./clear-data-plane.sh && exec java ${ENV_JVM_ARGS} -Djava.security.egd=file:/dev/urandom -jar sed-conn-controlplane.jar --log-level=${LOG_LEVEL}"]
