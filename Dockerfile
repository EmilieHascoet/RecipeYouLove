FROM eclipse-temurin:21-jre-alpine AS runtime

LABEL maintainer="SmartDish Team"
LABEL description="Application permettant de recommander des recettes à l'utilisateur"

RUN addgroup -S spring && adduser -S spring -G spring

WORKDIR /app

ARG JAR_FILE=target/*.jar

COPY ${JAR_FILE} app.jar

RUN chown spring:spring app.jar

USER spring:spring

EXPOSE 8090

ENV JAVA_OPTS="-XX:+UseContainerSupport -XX:MaxRAMPercentage=75.0 -Djava.security.egd=file:/dev/./urandom"

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]

HEALTHCHECK --interval=30s --timeout=3s --start-period=40s --retries=3 \
  CMD wget --no-verbose --tries=1 --spider http://localhost:8090/health || exit 1
