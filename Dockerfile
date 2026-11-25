# Imagen de la API para Render: se compila con Maven (JDK 21) y se ejecuta solo con el JRE 21
FROM maven:3.9.11-eclipse-temurin-21 AS compilacion
WORKDIR /app
# Agente de OpenTelemetry (trazas, métricas y logs hacia Grafana Cloud), verificado con su SHA-256
ARG OTEL_AGENT_VERSION=2.33.1
ARG OTEL_AGENT_SHA256=e43a270df53c6f423e0ab157be9c27ccc01d5dc3232e162fe54777573db2f149
RUN curl --proto '=https' --tlsv1.2 -fsSL -o opentelemetry-javaagent.jar \
      "https://github.com/open-telemetry/opentelemetry-java-instrumentation/releases/download/v${OTEL_AGENT_VERSION}/opentelemetry-javaagent.jar" \
 && echo "${OTEL_AGENT_SHA256}  opentelemetry-javaagent.jar" | sha256sum -c -
COPY pom.xml .
COPY src ./src
# El jar se extrae (app.jar + lib/) para poder usar CDS en la imagen final
RUN mvn -B -q package -Dmaven.test.skip=true \
 && cp target/*.jar app.jar \
 && java -Djarmode=tools -jar app.jar extract --destination extraido

FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
# Se ejecuta con un usuario sin privilegios (hallazgo Trivy DS-0002)
RUN addgroup -S gestor && adduser -S gestor -G gestor
COPY --from=compilacion /app/extraido/ ./
COPY --from=compilacion /app/opentelemetry-javaagent.jar ./
# El plan gratuito de Render da 512 MB y 0.1 CPU: el heap usa hasta el 75 % de la memoria del
# contenedor, SerialGC consume menos memoria y TieredStopAtLevel=1 acelera el arranque en frío
# (Render duerme el servicio tras 15 minutos sin tráfico).
ENV JAVA_TOOL_OPTIONS="-XX:MaxRAMPercentage=75 -XX:+UseSerialGC -XX:TieredStopAtLevel=1 -Xss512k"
# CDS (Class Data Sharing): se arranca el contexto una vez, sin conectarse a la base, y se guardan
# las clases ya cargadas en app.jsa; en Render el arranque las reutiliza y tarda mucho menos.
# (El entrenamiento corre sin el agente de OpenTelemetry: la JVM no permite generar el archivo
# CDS con un agente Java cargado.)
RUN java -XX:ArchiveClassesAtExit=app.jsa -Dspring.context.exit=onRefresh \
      -Dspring.jpa.properties.hibernate.boot.allow_jdbc_metadata_access=false \
      -Dspring.jpa.database-platform=org.hibernate.dialect.PostgreSQLDialect \
      -jar app.jar
# El agente de OpenTelemetry queda apagado por defecto; en Render lo enciende Terraform
# (OTEL_SDK_DISABLED=false) junto con el endpoint y la credencial de Grafana Cloud.
ENV JAVA_TOOL_OPTIONS="-XX:MaxRAMPercentage=75 -XX:+UseSerialGC -XX:TieredStopAtLevel=1 -Xss512k -javaagent:/app/opentelemetry-javaagent.jar"
ENV OTEL_SDK_DISABLED=true OTEL_SERVICE_NAME=gestor-api
USER gestor
EXPOSE 8081
HEALTHCHECK --interval=30s --timeout=5s --start-period=180s --retries=3 \
  CMD wget -qO- "http://localhost:${PORT:-8081}/actuator/health/liveness" || exit 1
ENTRYPOINT ["java", "-XX:SharedArchiveFile=app.jsa", "-jar", "app.jar"]
